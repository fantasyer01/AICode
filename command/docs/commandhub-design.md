# CommandHub — 命令管理工具 需求实现文档

## 1. 项目概述

### 1.1 项目定位

CommandHub 是一个轻量级的命令片段管理工具网站，用于收集、分类、检索日常在 Windows 和 Linux 中常用的命令。用户可以通过 Web 界面快速查找所需命令并一键复制使用。

### 1.2 核心目标

- **轻量化**：不依赖任何数据库，使用 JSON 文件存储数据
- **快速部署**：单个 jar 包，`java -jar` 即可在云服务器上启动
- **易于检索**：支持关键字搜索、平台筛选、标签筛选等多维度检索
- **便于维护**：提供在线 CRUD 功能，带权限控制

---

## 2. 技术选型

| 层面       | 方案                                                                 |
| ---------- | -------------------------------------------------------------------- |
| 后端框架   | Spring Boot 3.x（内嵌 Tomcat，单 jar 部署）                         |
| 前端方案   | 原生 HTML + CSS + JavaScript，放在 `src/main/resources/static` 目录  |
| UI 美化    | Tailwind CSS（CDN 引入）+ 图标库，保证美观且无需构建工具             |
| 数据存储   | JSON 文件存储（`commands.json`），服务端读写，无数据库依赖           |
| 权限控制   | 简单密码验证（配置文件中设定管理密码），保护 CRUD 操作               |
| 部署方式   | 单 jar 包，`java -jar` 一键启动                                     |

---

## 3. 数据模型设计

### 3.1 命令数据结构

每条命令的 JSON 结构：

```json
{
  "id": "cmd_1001",
  "title": "查看端口占用",
  "command": "netstat -ano | findstr :8080",
  "platform": "windows",
  "tags": ["网络", "调试", "端口"],
  "description": "查看指定端口的占用情况，找到对应的 PID 进程",
  "createTime": "2025-04-25T10:30:00",
  "updateTime": "2025-04-25T10:30:00"
}
```

### 3.2 字段说明

| 字段          | 类型     | 说明                                                       |
| ------------- | -------- | ---------------------------------------------------------- |
| `id`          | String   | 唯一标识，自动生成（UUID）                                 |
| `title`       | String   | 命令的简短描述标题，便于快速识别                           |
| `command`     | String   | 实际的命令内容，支持多行                                   |
| `platform`    | String   | 操作系统分类：`windows` / `linux` / `common`（通用）       |
| `tags`        | String[] | 自由标签，用于多维度分类和检索                             |
| `description` | String   | 详细备注说明，包括参数解释、使用场景、注意事项等           |
| `createTime`  | String   | 创建时间（ISO 8601 格式）                                  |
| `updateTime`  | String   | 最后更新时间（ISO 8601 格式）                              |

### 3.3 存储文件结构

数据文件路径：`data/commands.json`（独立于 jar 包存放）

```json
{
  "commands": [
    {
      "id": "cmd_1001",
      "title": "查看端口占用",
      "command": "netstat -ano | findstr :8080",
      "platform": "windows",
      "tags": ["网络", "调试"],
      "description": "查看指定端口的占用情况",
      "createTime": "2025-04-25T10:30:00",
      "updateTime": "2025-04-25T10:30:00"
    }
  ],
  "tags": ["网络", "文件", "系统管理", "Docker", "Git", "调试", "运维"],
  "version": 1
}
```

- `commands`：命令数组
- `tags`：全局标签列表，前端标签筛选器使用
- `version`：数据版本号，便于后续数据结构升级

---

## 4. 功能模块设计

### 4.1 命令检索模块（公开访问）

- **首页展示**：以卡片列表形式展示所有命令，支持分页
- **平台筛选**：单选按钮组（全部 / Windows / Linux / 通用）
- **标签筛选**：多选标签云，支持多标签 AND 筛选
- **关键字搜索**：实时搜索（防抖 300ms），匹配 title、command、description 字段
- **一键复制**：点击复制按钮将命令内容复制到剪贴板，并显示 "已复制" 反馈

### 4.2 命令管理模块（需要认证）

- **新增命令**：模态弹窗填写标题、命令内容、平台、标签、说明
- **编辑命令**：模态弹窗修改已有命令的任意字段
- **删除命令**：带确认提示的删除操作
- **标签管理**：通过新增命令时的标签输入自动维护全局标签列表

### 4.3 认证模块

- 简单密码认证，管理员输入预设密码后获得操作权限
- 密码在 `application.yml` 中配置
- 后端生成简单 Token，前端存储在 localStorage，请求时通过 Header 携带
- Token 有效期可配置（默认 24 小时）

---

## 5. REST API 设计

### 5.1 命令接口

| 方法     | 路径                 | 说明                                   | 认证 |
| -------- | -------------------- | -------------------------------------- | ---- |
| `GET`    | `/api/commands`      | 查询命令列表（支持搜索/筛选/分页）     | 否   |
| `GET`    | `/api/commands/{id}` | 获取单条命令详情                       | 否   |
| `POST`   | `/api/commands`      | 新增命令                               | 是   |
| `PUT`    | `/api/commands/{id}` | 更新命令                               | 是   |
| `DELETE` | `/api/commands/{id}` | 删除命令                               | 是   |

### 5.2 标签接口

| 方法  | 路径        | 说明         | 认证 |
| ----- | ----------- | ------------ | ---- |
| `GET` | `/api/tags` | 获取所有标签 | 否   |

### 5.3 认证接口

| 方法  | 路径               | 说明         | 认证 |
| ----- | ------------------ | ------------ | ---- |
| `POST`| `/api/auth/login`  | 密码登录     | 否   |
| `GET` | `/api/auth/status` | 检查登录状态 | 否   |

### 5.4 查询参数

```
GET /api/commands?keyword=端口&platform=windows&tags=网络,调试&page=1&size=20
```

| 参数       | 类型   | 说明                                         |
| ---------- | ------ | -------------------------------------------- |
| `keyword`  | String | 关键字，模糊匹配 title/command/description   |
| `platform` | String | 平台筛选：windows / linux / common           |
| `tags`     | String | 标签筛选，逗号分隔，AND 关系                 |
| `page`     | int    | 页码，从 1 开始，默认 1                      |
| `size`     | int    | 每页数量，默认 20                            |

### 5.5 响应格式

统一响应结构：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

分页响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [ ... ],
    "page": 1,
    "size": 20,
    "total": 56,
    "totalPages": 3
  }
}
```

---

## 6. 前端页面设计

### 6.1 整体布局

采用单页应用风格，所有操作在一个页面内完成：

```
┌─────────────────────────────────────────────────────┐
│  CommandHub — 命令管理工具               [管理登录]  │
├─────────────────────────────────────────────────────┤
│  [搜索框：输入关键字实时过滤...]                      │
│                                                     │
│  平台筛选：  [全部] [Windows] [Linux] [通用]          │
│  标签筛选：  [网络] [文件] [Docker] [Git] [系统] ...  │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─ 命令卡片 ────────────────────────────────────┐  │
│  │ 查看端口占用                 [Windows] [网络]  │  │
│  │ ┌────────────────────────────────────┐        │  │
│  │ │ netstat -ano | findstr :8080 [复制] │        │  │
│  │ └────────────────────────────────────┘        │  │
│  │ 查看指定端口的占用情况，找到对应 PID    [编辑] │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  [← 上一页]  第 1/5 页  [下一页 →]                   │
└─────────────────────────────────────────────────────┘
```

### 6.2 视觉风格

- 整体采用简洁现代风格，支持亮色/暗色主题切换
- Tailwind CSS 通过 CDN 引入，无需构建工具
- 命令代码区域：等宽字体 + 深色背景，与正文区分
- 卡片布局，阴影和圆角增强层次感
- 响应式设计，适配桌面和移动端

### 6.3 交互细节

- **搜索**：输入防抖 300ms，实时过滤结果
- **平台筛选**：单选切换按钮组，点击高亮选中
- **标签筛选**：多选标签云，点击高亮/取消
- **复制反馈**：点击复制按钮后显示 "已复制" 短暂提示（1.5s 后消失）
- **CRUD 弹窗**：模态弹窗进行新增/编辑，标签支持已有选择 + 手动新增
- **删除确认**：删除前弹出确认对话框
- **管理员模式**：登录后卡片上显示「编辑」「删除」按钮，顶部显示「新增命令」按钮

---

## 7. 项目结构

```
command/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/commandhub/
│       │   ├── CommandHubApplication.java          # Spring Boot 启动类
│       │   ├── config/
│       │   │   ├── WebConfig.java                  # Web 配置（拦截器注册）
│       │   │   └── AppProperties.java              # 自定义配置属性
│       │   ├── controller/
│       │   │   ├── CommandController.java           # 命令 CRUD REST API
│       │   │   └── AuthController.java              # 认证 API
│       │   ├── model/
│       │   │   ├── Command.java                     # 命令实体类
│       │   │   ├── CommandStore.java                 # 数据存储结构（顶层 JSON）
│       │   │   └── ApiResponse.java                 # 统一响应封装
│       │   ├── service/
│       │   │   ├── CommandService.java               # 命令业务逻辑
│       │   │   └── JsonStorageService.java           # JSON 文件读写服务
│       │   └── interceptor/
│       │       └── AuthInterceptor.java              # 认证拦截器
│       └── resources/
│           ├── application.yml                       # 应用配置
│           └── static/                               # 前端静态文件
│               ├── index.html                        # 主页面
│               ├── css/
│               │   └── style.css                     # 自定义样式
│               └── js/
│                   └── app.js                        # 前端逻辑
├── data/
│   └── commands.json                                 # 命令数据文件（运行时）
```

---

## 8. 配置说明

### 8.1 application.yml

```yaml
server:
  port: 8080

app:
  admin-password: changeme          # 管理员密码，部署时务必修改
  data-path: ./data/commands.json   # 数据文件路径
  token-expire-hours: 24            # Token 有效期（小时）
```

### 8.2 启动方式

```bash
# 基本启动
java -jar commandhub.jar

# 自定义配置启动
java -jar commandhub.jar \
  --server.port=9090 \
  --app.admin-password=my_secure_password \
  --app.data-path=/opt/commandhub/data/commands.json
```

---

## 9. 部署方案

### 9.1 构建

```bash
mvn clean package -DskipTests
# 产出：target/commandhub.jar
```

### 9.2 云服务器部署

```bash
# 1. 上传 jar 包
scp target/commandhub.jar user@server:/opt/commandhub/

# 2. 创建数据目录
ssh user@server "mkdir -p /opt/commandhub/data"

# 3. 后台启动
ssh user@server "cd /opt/commandhub && nohup java -jar commandhub.jar --app.admin-password=your_password > app.log 2>&1 &"
```

### 9.3 Systemd 服务（可选）

```ini
[Unit]
Description=CommandHub Command Management Tool
After=network.target

[Service]
Type=simple
User=commandhub
WorkingDirectory=/opt/commandhub
ExecStart=/usr/bin/java -jar commandhub.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

### 9.4 Nginx 反向代理（可选）

```nginx
server {
    listen 80;
    server_name cmd.yourdomain.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 10. 后续扩展方向（暂不实现）

以下功能作为未来可选扩展，当前版本不实现：

- **本地命令执行**：通过本地 Agent 客户端实现从网页端触发 Windows 命令执行
- **命令导入/导出**：支持批量导入导出 JSON 数据
- **命令收藏/置顶**：对高频使用的命令进行标记
- **命令执行历史**：记录命令的执行历史
- **多用户支持**：独立的用户体系和权限管理
