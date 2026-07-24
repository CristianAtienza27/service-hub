package com.servicehub.auth.domain;

import com.servicehub.user.domain.UserRole;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        UUID tenantId,
        String email,
        UserRole role
) {
}