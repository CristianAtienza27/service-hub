package com.servicehub.auth.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicehub.business.infrastructure.persistence.BusinessRepository;
import com.servicehub.tenant.infrastructure.persistence.TenantRepository;
import com.servicehub.user.domain.AppUser;
import com.servicehub.user.domain.UserRole;
import com.servicehub.user.infrastructure.persistence.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthRegistrationControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("service_hub_test")
                    .withUsername("service_hub_test")
                    .withPassword("service_hub_test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        appUserRepository.deleteAll();
        businessRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void shouldRegisterBusinessOwnerAndTenant() throws Exception {
        RegisterBusinessRequest request = new RegisterBusinessRequest(
                "Pablo Tattoo Studio",
                "pablo-tattoo",
                "PABLO@example.com",
                "Password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").isNotEmpty())
                .andExpect(jsonPath("$.businessId").isNotEmpty())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.slug").value("pablo-tattoo"));

        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(businessRepository.count()).isEqualTo(1);
        assertThat(appUserRepository.count()).isEqualTo(1);

        var tenant = tenantRepository.findBySlug("pablo-tattoo")
                .orElseThrow();

        var business = businessRepository.findByTenantId(tenant.getId())
                .orElseThrow();

        AppUser owner = appUserRepository
                .findByEmailIgnoreCase("pablo@example.com")
                .orElseThrow();

        assertThat(business.getTenantId()).isEqualTo(tenant.getId());
        assertThat(owner.getTenantId()).isEqualTo(tenant.getId());
        assertThat(owner.getEmail()).isEqualTo("pablo@example.com");
        assertThat(owner.getRole()).isEqualTo(UserRole.BUSINESS_OWNER);
        assertThat(owner.isActive()).isTrue();

        assertThat(owner.getPasswordHash())
                .isNotEqualTo("Password123");

        assertThat(passwordEncoder.matches(
                "Password123",
                owner.getPasswordHash()
        )).isTrue();
    }

    @Test
    void shouldReturnConflictWhenSlugAlreadyExists() throws Exception {
        RegisterBusinessRequest request = new RegisterBusinessRequest(
                "Pablo Tattoo Studio",
                "pablo-tattoo",
                "pablo@example.com",
                "Password123"
        );

        performRegistration(request)
                .andExpect(status().isCreated());

        RegisterBusinessRequest duplicateSlugRequest =
                new RegisterBusinessRequest(
                        "Otro estudio",
                        "pablo-tattoo",
                        "otro@example.com",
                        "Password123"
                );

        performRegistration(duplicateSlugRequest)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("A business with slug 'pablo-tattoo' already exists"));

        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(businessRepository.count()).isEqualTo(1);
        assertThat(appUserRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnConflictAndRollbackWhenEmailAlreadyExists()
            throws Exception {

        RegisterBusinessRequest firstRequest =
                new RegisterBusinessRequest(
                        "Pablo Tattoo Studio",
                        "pablo-tattoo",
                        "pablo@example.com",
                        "Password123"
                );

        performRegistration(firstRequest)
                .andExpect(status().isCreated());

        RegisterBusinessRequest duplicateEmailRequest =
                new RegisterBusinessRequest(
                        "Otro estudio",
                        "otro-estudio",
                        "PABLO@example.com",
                        "Password123"
                );

        performRegistration(duplicateEmailRequest)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A user with email 'pablo@example.com' already exists"));

        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(businessRepository.count()).isEqualTo(1);
        assertThat(appUserRepository.count()).isEqualTo(1);

        assertThat(tenantRepository.existsBySlug("otro-estudio"))
                .isFalse();
    }

    @Test
    void shouldRejectInvalidRegistrationRequest() throws Exception {
        RegisterBusinessRequest request = new RegisterBusinessRequest(
                "",
                "Slug Inválido!",
                "not-an-email",
                "123"
        );

        performRegistration(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.validationErrors.businessName")
                        .exists())
                .andExpect(jsonPath("$.validationErrors.slug")
                        .exists())
                .andExpect(jsonPath("$.validationErrors.ownerEmail")
                        .exists())
                .andExpect(jsonPath("$.validationErrors.password")
                        .exists());

        assertThat(tenantRepository.count()).isZero();
        assertThat(businessRepository.count()).isZero();
        assertThat(appUserRepository.count()).isZero();
    }

    private org.springframework.test.web.servlet.ResultActions
    performRegistration(RegisterBusinessRequest request) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}