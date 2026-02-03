# Spring AI Alibaba Dify Integration

这是一个使用 Spring Boot 和 Spring AI Alibaba 框架构建的后端应用，集成了 Dify AI 服务。

## 🚀 特性

- **技术栈**: Java 17 + Spring Boot 3.2
- **AI 集成**: 与 Dify 平台无缝对接
- **REST API**: 提供完整的聊天接口
- **生产级日志**: 结构化日志配置，便于问题排查
- **异常处理**: 全局异常处理器，统一错误响应格式
- **配置管理**: 多环境配置支持（开发/测试/生产）
- **健康检查**: 内置健康检查和监控端点
- **前端界面**: 简单的聊天界面用于测试

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/example/aialibaba/
│   │   ├── AiAlibabaApplication.java          # 主应用类
│   │   ├── config/
│   │   │   ├── DifyConfig.java               # Dify 配置类
│   │   │   ├── RestWebConfig.java            # Web 配置类
│   │   │   └── LoggingConfig.java            # 日志配置
│   │   ├── controller/
│   │   │   └── ChatController.java           # REST 控制器
│   │   ├── service/
│   │   │   ├── ChatService.java              # 服务接口
│   │   │   └── impl/
│   │   │       └── DifyChatServiceImpl.java  # Dify 实现
│   │   ├── model/
│   │   │   └── dto/
│   │   │       ├── ChatRequestDTO.java       # 请求 DTO
│   │   │       └── ChatResponseDTO.java      # 响应 DTO
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java   # 全局异常处理
│   │       └── ServiceException.java         # 业务异常
│   └── resources/
│       ├── application.yml                   # 主配置文件
│       ├── application-dev.yml              # 开发环境配置
│       ├── application-prod.yml             # 生产环境配置
│       ├── application-test.yml             # 测试环境配置
│       ├── logback-spring.xml              # 日志配置
│       └── static/
│           └── index.html                  # 前端测试页面
└── test/
    └── java/com/example/aialibaba/
        └── service/
            ├── ChatServiceIntegrationTest.java
            └── DifyChatServiceImplTest.java
```

## 🔧 快速开始

### 1. 环境准备

- Java 17 或更高版本
- Maven 3.6+
- Dify 账户和已发布的应用

### 2. 配置 Dify

在 `src/main/resources/application-dev.yml` 中配置你的 Dify 凭证：

```yaml
dify:
  api:
    key: your-dify-api-key-here  # 替换为你的 Dify API 密钥
  app:
    id: your-dify-app-id-here    # 替换为你的 Dify 应用 ID
```

或者通过环境变量设置：
```bash
export DIFY_API_KEY=your-api-key
export DIFY_APP_ID=your-app-id
```

### 3. 构建和运行

```bash
# 清理并编译
mvn clean compile

# 运行应用
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/ai-alibaba-dify-1.0.0.jar
```

### 4. 访问应用

- **前端界面**: http://localhost:8080
- **API 文档**: http://localhost:8080/swagger-ui.html (如果启用)
- **健康检查**: http://localhost:8080/api/v1/chat/health
- **服务信息**: http://localhost:8080/api/v1/chat/info

## 📡 API 接口

### 发送消息

**POST** `/api/v1/chat/send`

请求体示例：
```json
{
  "message": "你好，介绍一下你自己",
  "userId": "user123",
  "conversationId": "conv456"  // 可选，用于会话连续性
}
```

响应示例：
```json
{
  "messageId": "msg789",
  "answer": "你好！我是基于 Dify 构建的 AI 助手...",
  "conversationId": "conv456",
  "createdAt": 1706789000000,
  "status": "success"
}
```

### 健康检查

**GET** `/api/v1/chat/health`

响应：`Chat service is operational`

## 🛠️ 开发指南

### 添加新功能

1. 在 `service` 包中添加新的服务接口
2. 在 `impl` 包中实现具体逻辑
3. 在 `controller` 中添加对应的 REST 端点
4. 更新 DTO 类以支持新的数据结构

### 日志级别

- **DEBUG**: 开发调试信息
- **INFO**: 重要业务流程和成功操作
- **WARN**: 警告和可恢复的问题
- **ERROR**: 错误和异常情况

### 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn -Dtest=ChatServiceIntegrationTest test
```

## 📊 监控和运维

### 日志文件

- `logs/application.log` - 应用程序日志
- `logs/error.log` - 错误日志
- `logs/audit.log` - 审计日志

### Actuator 端点

- `/actuator/health` - 健康状态
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标

## 🔒 安全考虑

- API 密钥应通过环境变量或配置中心管理
- 生产环境中禁用 Swagger UI
- 使用 HTTPS 进行生产部署
- 实施适当的输入验证和清理

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 📄 许可证

MIT License

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba)
- [Dify](https://dify.ai/)