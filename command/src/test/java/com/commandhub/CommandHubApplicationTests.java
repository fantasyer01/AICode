package com.commandhub;

import com.commandhub.controller.AuthController;
import com.commandhub.controller.CommandController;
import com.commandhub.model.ApiResponse;
import com.commandhub.model.Command;
import com.commandhub.model.PageResult;
import com.commandhub.service.CommandService;
import com.commandhub.service.JsonStorageService;
import com.commandhub.config.AppProperties;
import com.commandhub.interceptor.AuthInterceptor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "app.data-path=./data/test-commands.json",
        "app.admin-password=testpass123"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommandHubApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() { return "http://localhost:" + port; }

    @Test
    @Order(1)
    void contextLoads() {
        // Spring context should load without errors
    }

    @Test
    @Order(2)
    void testGetTagsPublic() {
        ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl() + "/api/tags", String.class);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains("\"code\":200"));
    }

    @Test
    @Order(3)
    void testGetCommandsPublic() {
        ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl() + "/api/commands?page=1&size=10", String.class);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains("\"code\":200"));
        assertTrue(resp.getBody().contains("\"content\""));
    }

    @Test
    @Order(4)
    void testLoginWithWrongPassword() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"password\":\"wrongpass\"}", headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(baseUrl() + "/api/auth/login", entity, String.class);
        assertEquals(200, resp.getStatusCode().value());
        assertTrue(resp.getBody().contains("401"));
    }

    @Test
    @Order(5)
    void testLoginWithCorrectPassword() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"password\":\"testpass123\"}", headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(baseUrl() + "/api/auth/login", entity, String.class);
        assertEquals(200, resp.getStatusCode().value());
        assertTrue(resp.getBody().contains("\"token\""));
    }

    @Test
    @Order(6)
    void testCreateCommandWithoutAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"title\":\"test\",\"command\":\"echo hello\",\"platform\":\"linux\",\"tags\":[\"test\"],\"description\":\"test\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(baseUrl() + "/api/commands", HttpMethod.POST, entity, String.class);
        assertEquals(403, resp.getStatusCode().value());
    }

    @Test
    @Order(7)
    void testCrudWithAuth() {
        // 1. Login
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> loginEntity = new HttpEntity<>("{\"password\":\"testpass123\"}", loginHeaders);
        ResponseEntity<String> loginResp = restTemplate.postForEntity(baseUrl() + "/api/auth/login", loginEntity, String.class);
        String body = loginResp.getBody();
        // extract token from response
        int tokenStart = body.indexOf("\"token\":\"") + 9;
        int tokenEnd = body.indexOf("\"", tokenStart);
        String token = body.substring(tokenStart, tokenEnd);
        assertFalse(token.isEmpty());

        // 2. Create command
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.set("Authorization", "Bearer " + token);

        String createBody = "{\"title\":\"Test Command\",\"command\":\"echo test\",\"platform\":\"linux\",\"tags\":[\"test\"],\"description\":\"A test command\"}";
        HttpEntity<String> createEntity = new HttpEntity<>(createBody, authHeaders);
        ResponseEntity<String> createResp = restTemplate.postForEntity(baseUrl() + "/api/commands", createEntity, String.class);
        assertEquals(200, createResp.getStatusCode().value());
        assertTrue(createResp.getBody().contains("Test Command"));

        // Extract created command id
        String createRespBody = createResp.getBody();
        int idStart = createRespBody.indexOf("\"id\":\"") + 6;
        int idEnd = createRespBody.indexOf("\"", idStart);
        String cmdId = createRespBody.substring(idStart, idEnd);

        // 3. Get command by id
        ResponseEntity<String> getResp = restTemplate.getForEntity(baseUrl() + "/api/commands/" + cmdId, String.class);
        assertEquals(200, getResp.getStatusCode().value());
        assertTrue(getResp.getBody().contains("Test Command"));

        // 4. Update command
        String updateBody = "{\"title\":\"Updated Test Command\",\"command\":\"echo updated\",\"platform\":\"windows\",\"tags\":[\"test\",\"updated\"],\"description\":\"Updated description\"}";
        HttpEntity<String> updateEntity = new HttpEntity<>(updateBody, authHeaders);
        ResponseEntity<String> updateResp = restTemplate.exchange(baseUrl() + "/api/commands/" + cmdId, HttpMethod.PUT, updateEntity, String.class);
        assertEquals(200, updateResp.getStatusCode().value());
        assertTrue(updateResp.getBody().contains("Updated Test Command"));

        // 5. Search by keyword
        ResponseEntity<String> searchResp = restTemplate.getForEntity(baseUrl() + "/api/commands?keyword=Updated", String.class);
        assertEquals(200, searchResp.getStatusCode().value());
        assertTrue(searchResp.getBody().contains("Updated Test Command"));

        // 6. Search by platform
        ResponseEntity<String> platformResp = restTemplate.getForEntity(baseUrl() + "/api/commands?platform=windows", String.class);
        assertEquals(200, platformResp.getStatusCode().value());
        assertTrue(platformResp.getBody().contains("Updated Test Command"));

        // 7. Search by tags
        ResponseEntity<String> tagsResp = restTemplate.getForEntity(baseUrl() + "/api/commands?tags=test,updated", String.class);
        assertEquals(200, tagsResp.getStatusCode().value());
        assertTrue(tagsResp.getBody().contains("Updated Test Command"));

        // 8. Delete command
        HttpEntity<Void> deleteEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<String> deleteResp = restTemplate.exchange(baseUrl() + "/api/commands/" + cmdId, HttpMethod.DELETE, deleteEntity, String.class);
        assertEquals(200, deleteResp.getStatusCode().value());

        // 9. Verify deleted
        ResponseEntity<String> verifyResp = restTemplate.getForEntity(baseUrl() + "/api/commands/" + cmdId, String.class);
        assertTrue(verifyResp.getBody().contains("404"));
    }

    @Test
    @Order(8)
    void testAuthStatusCheck() {
        // Without token
        ResponseEntity<String> noAuthResp = restTemplate.getForEntity(baseUrl() + "/api/auth/status", String.class);
        assertEquals(200, noAuthResp.getStatusCode().value());
        assertTrue(noAuthResp.getBody().contains("false"));

        // With valid token
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> loginEntity = new HttpEntity<>("{\"password\":\"testpass123\"}", loginHeaders);
        ResponseEntity<String> loginResp = restTemplate.postForEntity(baseUrl() + "/api/auth/login", loginEntity, String.class);
        String body = loginResp.getBody();
        int tokenStart = body.indexOf("\"token\":\"") + 9;
        int tokenEnd = body.indexOf("\"", tokenStart);
        String token = body.substring(tokenStart, tokenEnd);

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.set("Authorization", "Bearer " + token);
        HttpEntity<Void> authEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<String> authResp = restTemplate.exchange(baseUrl() + "/api/auth/status", HttpMethod.GET, authEntity, String.class);
        assertEquals(200, authResp.getStatusCode().value());
        assertTrue(authResp.getBody().contains("true"));
    }

    @AfterAll
    static void cleanup() {
        new File("./data/test-commands.json").delete();
    }
}
