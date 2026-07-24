package com.servicehub.auth.infrastructure.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicehub.auth.infrastructure.rest.LoginRequest;
import com.servicehub.auth.infrastructure.rest.RegisterBusinessRequest;
import com.servicehub.business.infrastructure.persistence.BusinessRepository;
import com.servicehub.tenant.infrastructure.persistence.TenantRepository;
import com.servicehub.user.infrastructure.persistence.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("service_hub_auth_test")
                    .withUsername("service_hub_test")
                    .withPassword("service_hub_test");

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );

        registry.add(
                "security.jwt.secret",
                () -> "integration-test-secret-key-with-at-least-32-bytes"
        );

        registry.add(
                "security.jwt.expiration",
                () -> "3600"
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @BeforeEach
    void setUp() throws Exception {
        appUserRepository.deleteAll();
        businessRepository.deleteAll();
        tenantRepository.deleteAll();

        registerBusiness();
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest(
                "PABLO@example.com",
                "Password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.tenantId").isNotEmpty())
                .andExpect(jsonPath("$.email")
                        .value("pablo@example.com"))
                .andExpect(jsonPath("$.role")
                        .value("BUSINESS_OWNER"));
    }

    @Test
    void shouldRejectIncorrectPassword() throws Exception {
        LoginRequest request = new LoginRequest(
                "pablo@example.com",
                "IncorrectPassword"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }

    @Test
    void shouldRejectUnknownEmail() throws Exception {
        LoginRequest request = new LoginRequest(
                "unknown@example.com",
                "Password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }

    @Test
    void shouldAccessProtectedEndpointWithValidToken()
            throws Exception {

        String token = loginAndGetToken();

        mockMvc.perform(get("/api/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.tenantId").isNotEmpty())
                .andExpect(jsonPath("$.email")
                        .value("pablo@example.com"))
                .andExpect(jsonPath("$.role")
                        .value("BUSINESS_OWNER"));
    }

    @Test
    void shouldRejectProtectedEndpointWithoutToken()
            throws Exception {

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProtectedEndpointWithInvalidToken()
            throws Exception {

        mockMvc.perform(get("/api/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer invalid-token"
                        ))
                .andExpect(status().isUnauthorized());
    }

    private void registerBusiness() throws Exception {
        RegisterBusinessRequest request =
                new RegisterBusinessRequest(
                        "Pablo Tattoo Studio",
                        "pablo-tattoo",
                        "pablo@example.com",
                        "Password123"
                );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private String loginAndGetToken() throws Exception {
        LoginRequest request = new LoginRequest(
                "pablo@example.com",
                "Password123"
        );

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String token = json.get("accessToken").asText();

        assertThat(token).isNotBlank();

        return token;
    }
}