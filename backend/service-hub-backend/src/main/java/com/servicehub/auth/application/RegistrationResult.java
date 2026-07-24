package com.servicehub.auth.application;

import java.util.UUID;

public record RegistrationResult(
        UUID tenantId,
        UUID businessId,
        UUID userId,
        String slug
) {
}