# Java 进程被 Linux OOM-Killer 杀掉

> **归档时间**：2026-05-20
> **影响范围**：生产环境（阿里云 ECS）
> **严重等级**：高（服务不可用）
> **关键词**：OOM-Killer、JVM 堆上限、anon-rss、dnf、cgroup、SIGKILL

---

## 一、问题现象

1. 部署在阿里云 ECS 上的 Spring Boot 服务（AI Blog）会**毫无征兆地停掉**，进程从 `ps`/`pgrep` 中消失。
2. 应用日志（`logs/aiblog.log`、`logs/startup.log`）**没有任何异常堆栈**或退出信息——就像被人直接拔了电源。
3. 业务侧偶尔会看到 `MultipartException`、`ClientAbortException` 之类的异常被 `GlobalExceptionHandler` 捕获，**但这些异常和进程消失没有因果关系**，只是凑巧在同一时间窗内被观察到。
4. 进程消失的时间点有规律性——多次发生在凌晨 4:18 ~ 7:17 左右。

## 二、排查思路

### 2.1 先排除"应用内异常导致退出"

通读项目代码确认：

- 无 `System.exit(...)` 调用；
- 无 `@Async` / `@Scheduled` / 手工 `new Thread(...)` 等独立线程；
- 业务代码中所有异常都在 Servlet 请求线程内抛出，会被 Tomcat catch 住，最坏情况是该请求返回 500，**不会让 JVM 退出**。

结论：**进程退出不是应用内逻辑造成的**，必须从外部信号方向排查。

### 2.2 检查内核日志（关键步骤）

进程被外部 `SIGKILL` 杀掉的最常见原因是 Linux **OOM-Killer**。它的痕迹只会留在内核日志，应用日志里看不到。

```bash
# 方式 1：dmesg（带可读时间）
dmesg -T | grep -i -E 'killed process|out of memory'

# 方式 2：journalctl（持久化日志）
journalctl -k | grep -i oom
sudo journalctl -k --since "7 days ago" | grep -i 'killed process'

# 方式 3：直接看 messages（CentOS/RHEL 系）
sudo grep -i 'killed process' /var/log/messages
```

只要看到类似下面的行，就实锤了：

```text
[Tue May 19 04:18:13 2026] Out of memory: Killed process 3996369 (java)
    total-vm:3376784kB, anon-rss:551100kB, file-rss:0kB, shmem-rss:0kB,
    UID:1000 pgtables:1424kB oom_score_adj:0
```

### 2.3 解读 OOM 日志字段

| 字段 | 含义 | 关注点 |
|------|------|--------|
| `process xxx (java)` | 被杀的进程名 | 受害者 |
| `total-vm` | 虚拟地址空间预留量 | **数字大不代表真占用大**，JVM 通常会预留几个 GB |
| **`anon-rss`** | **真正占用的物理内存（堆/栈）** | **决定"谁最胖"的核心指标** |
| `file-rss` | 映射文件占用的物理内存（.so/.jar） | 一般较小 |
| `UID` | 进程所属用户 | `1000` 普通用户，`0` root |
| `oom_score_adj` | OOM 评分调整值 | `0` 表示没人为干预 |

> **判断口诀**：看 `anon-rss` 不看 `total-vm`。OOM-Killer 是按"当下物理内存最胖"挑人的。

### 2.4 还原现场：谁在抢内存

把所有 OOM 事件按时间排序后会发现规律：

```text
04:18 java 被杀，紧邻的几条都是 dnf（500MB+）被杀
04:19 java 被杀，又是 dnf 在跑
07:17 dnf 被杀（这次 java 不在场）
```

这说明：

1. **机器整体 RAM 长期紧张**，连系统自己的 `dnf`（包管理器）都会被反复杀。
2. CentOS / Aliyun Linux 默认有**凌晨自动 `dnf-makecache`** 的 systemd timer，每天凌晨 4 点左右刷新软件包索引，dnf 启动峰值 ~500MB。
3. 此时如果 Java 进程因为没设 `-Xmx`，物理占用已经悄悄涨到 500~600MB，叠加 dnf 就把机器内存压爆。
4. OOM-Killer 启动时挑出"当下最胖"的那一个杀，**Java 因为体型最大、最容易中招**。

## 三、根本原因

### 3.1 直接原因
Linux 内核检测到内存不足，调用 OOM-Killer 给 Java 进程发了 `SIGKILL`（信号 9，无法被捕获），进程**没有任何机会**执行 `finally`、`shutdown hook` 或写日志。

### 3.2 深层原因
- **JVM 启动命令没有设置 `-Xmx`**。JVM 默认使用"物理内存的 1/4"作为 `MaxHeapSize`，加上 Metaspace（~100MB）、线程栈（200×1MB）、Direct Memory、JIT Code Cache 等，单个 Java 进程**完全有条件涨到 1GB+**。
- **服务器内存太小**（约 1~2GB），且没有配置 swap。
- **没有进程守护**：`nohup ... &` 启动方式，被 `kill -9` 后不会自动拉起，造成长时间不可用。

### 3.3 为什么 `@ControllerAdvice` 全局异常处理救不了

| 维度 | `@ControllerAdvice` 能拦的 | OOM-Killer 干的 |
|------|---------------------------|------------------|
| 触发位置 | JVM 进程内 Servlet 请求线程 | 操作系统外部 |
| 通知方式 | Java 异常对象 | `SIGKILL` 信号 |
| 是否可拦截 | 是 | **否**（信号 9 不可捕获） |
| 是否有日志 | 有 | 应用层完全无感知 |

**结论**：进程被 OOM-Killer 干掉时，应用代码根本来不及反应。`@ControllerAdvice` 只能解决"业务异常导致接口报错"，**对"进程消失"这类系统级事故无能为力**。

## 四、解决方案

### 4.1 必做：限制 JVM 内存上限

在启动命令里加上 JVM 参数（修改 `deploy/deploy.ps1` 中的 `nohup java ...`）：

```bash
nohup java \
    -Xms256m -Xmx512m \
    -XX:MaxMetaspaceSize=192m \
    -XX:MaxDirectMemorySize=128m \
    -XX:+ExitOnOutOfMemoryError \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=$RemotePath/logs/ \
    -Xlog:gc*:file=$RemotePath/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=10M \
    -jar $RemoteJarPath ...
```

**参数说明**：

| 参数 | 作用 |
|------|------|
| `-Xms256m -Xmx512m` | 堆固定在 256~512MB，避免 JVM 自己长成"全场最胖" |
| `-XX:MaxMetaspaceSize=192m` | 类元数据封顶，防止 Metaspace 无界增长 |
| `-XX:MaxDirectMemorySize=128m` | 堆外内存（图片 Base64、Netty 缓冲）封顶 |
| `-XX:+ExitOnOutOfMemoryError` | JVM 自身 OOM 时**主动退出**，配合守护进程能干净重启 |
| `-XX:+HeapDumpOnOutOfMemoryError` | OOM 前留 dump 文件，便于事后定位 |
| `-Xlog:gc*` | 滚动 GC 日志，长期观测内存趋势 |

> **机器只有 1GB 内存时**：`-Xmx384m`、`MaxMetaspaceSize=128m`，更保守。

### 4.2 推荐：加 1GB swap 缓冲

```bash
sudo fallocate -l 1G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
sudo sysctl vm.swappiness=10
echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf
```

`swappiness=10` 表示"尽量少用 swap，只在内存真不够时才用"。这样 dnf 这类短时高峰能挪到 swap，不会触发 OOM-Killer。

### 4.3 推荐：用 systemd 守护进程

替换 `nohup ... &` 启动方式，进程被杀后自动拉起：

```ini
# /etc/systemd/system/ai-blog.service
[Unit]
Description=AI Blog
After=network.target

[Service]
Type=simple
User=app
WorkingDirectory=/home/app/ai-blog
ExecStart=/usr/bin/java -Xms256m -Xmx512m -XX:+ExitOnOutOfMemoryError \
    -jar /home/app/ai-blog/ai-blog-1.0.0.jar \
    --spring.profiles.active=prod --server.port=9100
Restart=always
RestartSec=10
StandardOutput=append:/home/app/ai-blog/logs/startup.log
StandardError=append:/home/app/ai-blog/logs/startup.log
OOMPolicy=continue          # 被 OOM-Killer 杀后允许 systemd 重启
# OOMScoreAdjust=-300       # 可选：降低被选中的概率（不建议优先使用）

[Install]
WantedBy=multi-user.target
```

启用：

```bash
sudo systemctl daemon-reload
sudo systemctl enable ai-blog
sudo systemctl start ai-blog
sudo systemctl status ai-blog
```

### 4.4 治本：升级机器内存

如果业务还要继续扩张（更多图片上传、更长 DeepSeek 调用、更多并发），从 1~2GB 升到 4GB 以上是最干净的解法。前面那些都属于"螺蛳壳里做道场"。

### 4.5 配套：完善全局异常处理

虽然 OOM 和异常处理无关，但顺手把 [GlobalExceptionHandler](../../src/main/java/com/aiblog/exception/GlobalExceptionHandler.java) 现存的几个问题修了，能让日志更干净：

- 把 `basePackageClasses` 限制去掉，改为真正全局；
- 单独处理 `MultipartException` / `MaxUploadSizeExceededException`；
- 把 `ClientAbortException` 静默到 DEBUG 级别（客户端断开不是服务端错误）；
- 补 `MissingServletRequestParameterException` / `MethodArgumentTypeMismatchException` / `HttpRequestMethodNotSupportedException` 等常见 Web 异常的 400/405 处理。

## 五、验证方式

修复部署后，检查：

1. JVM 内存上限生效：
   ```bash
   jcmd <pid> VM.flags | grep -E 'MaxHeapSize|MaxMetaspaceSize'
   ```
2. 进程内存稳定（连跑 24 小时）：
   ```bash
   ps -o pid,rss,vsz,cmd -p $(pgrep -f ai-blog-)
   # rss 应稳定在 ~600MB 以内
   ```
3. systemd 守护生效：手动 `kill -9 $(pgrep -f ai-blog-)`，观察 10 秒内是否自动拉起。
4. 持续观察 1 周，`dmesg -T | grep -i 'killed process.*java'` 不再出现新记录。

## 六、经验教训

1. **应用日志一片寂静的进程消失，9 成是被外部信号杀掉**。第一反应应该看 `dmesg` / `journalctl -k`，而不是翻业务日志。
2. **JVM 默认堆上限是物理内存的 1/4，但实际占用远不止堆**——线程栈、Metaspace、Direct Buffer、JIT Cache 加起来能轻松翻倍。生产环境**必须**显式设 `-Xmx`，宁可保守也别裸奔。
3. **OOM-Killer 是按"当下 anon-rss 最胖"挑人**，看 `total-vm` 会被吓死也会被误导。
4. **`@ControllerAdvice` 解决不了 SIGKILL**——应用层异常处理和操作系统信号是两个层面的事，不要混淆。
5. **进程必须有守护**：生产服务用 `nohup ... &` 而不挂守护，是事故放大器。
6. **小内存机器要装 swap**——尤其是宿主机上还有 dnf/yum 这类周期性高峰任务的环境。

## 七、相关文件

- 启动脚本：[deploy/deploy.ps1](../../deploy/deploy.ps1)
- 全局异常处理：[src/main/java/com/aiblog/exception/GlobalExceptionHandler.java](../../src/main/java/com/aiblog/exception/GlobalExceptionHandler.java)
- 应用配置：[src/main/resources/application.yml](../../src/main/resources/application.yml)
