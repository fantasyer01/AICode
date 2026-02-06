# AI Agents Context for ai-alibaba-dify

## Project Overview

Spring Boot backend application integrating Dify AI platform with Enterprise WeChat (WeCom) intelligent robot. Provides AI chat capabilities through REST APIs supporting both blocking and streaming (SSE) response modes.

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| AI Integration | Spring AI Alibaba 1.1.2.0, Dify API |
| HTTP Client | OkHttp3 4.12.0 |
| WeCom SDK | weixin-java-cp 4.6.0 |
| API Docs | SpringDoc OpenAPI 2.3.0 |
| Build | Maven |
| Testing | JUnit 5, Mockito, H2 |

## Project Structure

```
src/main/java/com/example/aialibaba/
├── config/          # Configuration classes (DifyConfig, WeComConfig, HttpConfig)
├── controller/      # REST endpoints (ChatController, StreamChatController, WeComController)
├── service/         # Service interfaces
│   └── impl/        # Service implementations
├── handler/wecom/   # WeCom message handlers (TextMessageHandler, EventMessageHandler)
├── model/dto/       # Data transfer objects
│   └── wecom/       # WeCom-specific DTOs (request, response)
├── exception/       # Custom exceptions and GlobalExceptionHandler
└── aspect/          # AOP aspects (AiMonitoringAspect)
```

## Key Components

### Chat Services
- `ChatService`: Interface for chat operations (blocking/streaming)
- `DifyChatServiceImpl`: Dify platform integration
- `SpringAiChatServiceImpl`: Spring AI direct model integration
- `UnifiedChatServiceImpl`: Unified entry point routing to appropriate service

### WeCom Integration
- `WeComController`: Handles callback URL verification and message reception
- `WeComMessageHandler`: Strategy pattern for message type handling
- `WeComCryptService`: Message encryption/decryption
- `WeComMessageService`: Message processing orchestration

## Configuration

Multi-environment configuration via YAML:
- `application.yml` - Base config
- `application-dev.yml` - Development
- `application-test.yml` - Testing
- `application-prod.yml` - Production

Key config paths:
- `dify.api.*` - Dify API settings
- `dify.apps.*` - Multi-app configurations
- `wecom.bot.*` - WeCom credentials
- `spring.ai.dashscope.*` - DashScope model config

## Build & Run Commands

```bash
# Build
mvn clean compile

# Test
mvn test

# Run (dev profile)
mvn spring-boot:run

# Package
mvn clean package

# Package（SkipTest）
mvn clean package -DskipTests

# Run JAR
java -jar target/ai-alibaba-dify-1.0.0.jar

# Run JAR（prod）
java -jar target/ai-alibaba-dify-1.0.0.jar --spring.profiles.active=prod
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/chat/send` | Blocking chat |
| POST | `/api/v1/stream/chat` | Streaming chat (SSE) |
| GET/POST | `/api/wecom/callback` | WeCom callback |
| GET | `/api/v1/chat/health` | Health check |
| GET | `/actuator/health` | Actuator health |

## Dependencies Pattern

- Constructor injection preferred (avoid `@Autowired` field injection)
- Use `@RequiredArgsConstructor` with `final` fields for Lombok-based injection
- OkHttp client is injected as a bean from `HttpConfig`

## Logging

- SLF4J with Logback
- Log files: `logs/application.log`, `logs/error.log`
- Use structured logging with request IDs
- Log levels: DEBUG for dev, INFO for prod

## Testing Conventions

- Unit tests in `src/test/java` mirroring main structure
- Use `@MockitoExtension` for mocking
- Integration tests with `@SpringBootTest`
- H2 in-memory database for data layer tests
