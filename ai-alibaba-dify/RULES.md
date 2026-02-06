# Coding Rules for ai-alibaba-dify

## Code Style

### Java Conventions
- Use Java 17 features (records, pattern matching, text blocks where appropriate)
- Follow standard Java naming: `camelCase` for methods/variables, `PascalCase` for classes
- Package names: `com.example.aialibaba.<module>`
- Max line length: 120 characters

### Class Organization
```java
// 1. Static fields
// 2. Instance fields (final first)
// 3. Constructor(s)
// 4. Public methods
// 5. Package-private methods
// 6. Private methods
```

### Lombok Usage
- `@Slf4j` for logging (or manual SLF4J Logger)
- `@Data` / `@Getter` / `@Setter` for DTOs
- `@Builder` for complex object construction
- `@RequiredArgsConstructor` for dependency injection
- Avoid `@AllArgsConstructor` on service classes

## Architecture Rules

### Layered Architecture
```
Controller → Service Interface → Service Implementation
                              ↓
                         External APIs (Dify, WeCom)
```

### Service Layer
- Define interface in `service/` package
- Implementation in `service/impl/` package
- One primary service per external integration
- Use `@Service` annotation on implementations

### Controller Layer
- Use `@RestController` and `@RequestMapping`
- Return DTOs, never entities
- Use `@Operation` and `@ApiResponse` for OpenAPI documentation
- Handle validation with `@Valid` and `@Validated`

### Handler Pattern (WeCom)
- Implement `WeComMessageHandler` interface
- Use `supports(String msgType)` for routing
- Define priority via `getPriority()` method
- Register as `@Component`

## Error Handling

### Exceptions
- Throw `ServiceException` for business errors
- Include error code and descriptive message
- Let `GlobalExceptionHandler` handle REST responses
- Log errors with context (user ID, request ID)

```java
throw new ServiceException("ERROR_CODE", "Human readable message");
throw new ServiceException("ERROR_CODE", "Message", cause);
```

### Response Format
- Success: Return DTO directly or wrapped in standard response
- Error: `{"error": "CODE", "message": "description", "timestamp": ...}`

## Configuration

### Adding New Config
1. Add properties to `application.yml`
2. Create `@ConfigurationProperties` class in `config/`
3. Use `@Value` only for simple cases
4. Support environment variable overrides: `${ENV_VAR:default}`

### Profiles
- `dev`: Local development, debug logging
- `test`: CI/CD testing
- `prod`: Production, minimal logging

## Testing

### Test Naming
- `*Test.java` for unit tests
- `*IntegrationTest.java` for integration tests

### Test Structure
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock private Dependency dependency;
    @InjectMocks private MyService service;
    
    @Test
    void methodName_condition_expectedResult() { }
}
```

### Assertions
- Use AssertJ for fluent assertions
- Test happy path and error cases
- Mock external dependencies

## HTTP & Streaming

### OkHttp Usage
- Inject `OkHttpClient` bean from `HttpConfig`
- Use async callbacks for streaming (`enqueue`)
- Always close `Response` in finally block

### SSE (Server-Sent Events)
- Return `SseEmitter` for streaming endpoints
- Set appropriate timeout (default: 60s)
- Send `end` event on completion
- Handle errors via `emitter.completeWithError()`

## Security Guidelines

- Never log sensitive data (API keys, tokens)
- Use environment variables for secrets
- Validate all input from external sources
- Sanitize WeCom message content before processing

## Git Commit Messages

Format: `<type>: <description>`

Types:
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring
- `docs`: Documentation
- `test`: Adding tests
- `chore`: Build/config changes

## DO NOT

- Don't use `@Autowired` field injection
- Don't catch and swallow exceptions silently
- Don't hardcode configuration values
- Don't mix business logic in controllers
- Don't return raw Maps when DTOs are appropriate
- Don't create new `ObjectMapper` instances (inject or use shared)
