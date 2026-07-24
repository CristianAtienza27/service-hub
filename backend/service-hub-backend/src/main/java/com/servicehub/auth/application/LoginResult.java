package com.servicehub.auth.application;

import com.servicehub.user.domain.UserRole;

import java.util.UUID;

public record LoginResult(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        UUID tenantId,
        String email,
        UserRole role
) {
}