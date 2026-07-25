# 审核管理系统 Demo

基于注解的通用复核功能演示项目，使用 Java + Spring Boot + MySQL + Thymeleaf 实现。

## 功能特性

- **声明式审核**: 通过 `@AuditRequired` 注解启用审核功能
- **单表审核**: 支持单表的增删改审核（用户管理演示）
- **多表审核**: 支持多表关联数据的审核（订单管理演示）
- **审核流程**: 提交 → 待审核 → 通过/驳回
- **历史追溯**: 完整的审核历史记录和数据快照

## 技术栈

- Java 11+
- Spring Boot 2.7
- MyBatis Plus 3.5
- MySQL 5.7+
- Thymeleaf
- Bootstrap 5

## 快速开始

### 1. 配置数据库

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/audit_demo?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root    # 修改为你的用户名
    password: root_password    # 修改为你的密码
```

### 2. 初始化数据库

执行 `src/main/resources/schema.sql` 脚本创建数据库和表：

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 3. 运行项目

```bash
cd audit-demo
mvn spring-boot:run
```

或者使用 IDE 运行 `AuditDemoApplication.java`

### 4. 访问系统

打开浏览器访问: http://localhost:8080

## 项目结构

```
audit-demo/
├── src/main/java/com/example/auditdemo/
│   ├── audit/                    # 审核框架核心
│   │   ├── annotation/           # @AuditRequired 注解
│   │   ├── aspect/               # AOP 切面
│   │   ├── context/              # AuditContext 上下文
│   │   ├── model/                # 数据模型
│   │   ├── entity/               # 实体类
│   │   ├── mapper/               # MyBatis Mapper
│   │   ├── service/              # 审核服务
│   │   ├── executor/             # SQL 执行器
│   │   └── controller/           # 审核管理接口
│   ├── business/                 # 业务演示模块
│   │   ├── user/                 # 用户管理（单表演示）
│   │   └── order/                # 订单管理（多表演示）
│   ├── common/                   # 公共组件
│   └── config/                   # 配置类
├── src/main/resources/
│   ├── templates/                # Thymeleaf 模板
│   ├── static/                   # 静态资源
│   ├── application.yml           # 配置文件
│   └── schema.sql                # 数据库脚本
└── pom.xml
```

## 使用方法

### 给现有方法添加审核功能

只需要在 Service 方法上添加 `@AuditRequired` 注解，并设置 `AuditContext`：

```java
@Service
public class YourService {

    @AuditRequired(businessType = "YOUR_BUSINESS", desc = "业务描述")
    public void yourMethod(YourDTO dto) {
        // 1. 组装审核数据
        AuditContext.setSingleTable(
            "your_table",           // 表名
            OperationType.INSERT,   // 操作类型
            null,                   // 主键（INSERT时为null）
            null,                   // 修改前数据
            entityToMap(dto),       // 修改后数据
            "操作说明"
        );
        
        // 2. 不执行实际操作，等待审核通过
    }
}
```

### 多表关联审核

```java
@AuditRequired(businessType = "ORDER_MGMT", desc = "新增订单")
public void createOrder(OrderDTO orderDTO) {
    // 使用流式API组装多表操作
    AuditContext.multiTable()
        .addInsert("order_main", orderData)
        .addInsertWithParent("order_item", itemData, "order_main.id", "order_id")
        .remark("新增订单")
        .submit();
}
```

### 跳过审核

某些特殊场景需要跳过审核：

```java
@AuditRequired(businessType = "USER_MGMT", desc = "修改用户")
public void updateUser(UserDTO dto, boolean isAdmin) {
    if (isAdmin) {
        // 管理员直接执行，跳过审核
        AuditContext.skip();
        userMapper.updateById(dto);
        return;
    }
    
    // 普通用户走审核流程
    AuditContext.setSingleTable(...);
}
```

## 页面说明

| 页面 | 路径 | 说明 |
|------|------|------|
| 待审核列表 | /audit/pending | 查看和处理待审核的请求 |
| 审核记录 | /audit | 查看所有审核历史 |
| 审核详情 | /audit/detail/{id} | 查看审核详情和数据对比 |
| 用户管理 | /user | 单表演示：用户的增删改 |
| 订单管理 | /order | 多表演示：订单+明细的增删改 |

## 工作流程

```
1. 用户执行业务操作（新增/修改/删除）
         ↓
2. Service 方法被 AOP 拦截
         ↓
3. 操作数据保存到 sys_audit_request 表
         ↓
4. 审核人在"待审核"页面处理
         ↓
5. 审核通过 → GenericSqlExecutor 执行实际 SQL
   审核驳回 → 记录驳回原因，数据不生效
```

## 扩展说明

### 接入新业务

1. 在 Service 方法上添加 `@AuditRequired` 注解
2. 在方法内设置 `AuditContext` 数据
3. 完成！无需修改任何其他代码

### 自定义审核人

修改 `UserContext.java` 集成你的用户系统：

```java
public class UserContext {
    public static Long getCurrentUserId() {
        // 从 Security Context 或 Session 获取当前用户
    }
}
```

## License

MIT License
