package com.servicehub.auth.infrastructure.rest;

import com.servicehub.auth.application.LoginResult;
import com.servicehub.user.domain.UserRole;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        UUID tenantId,
        String email,
        UserRole role
) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresIn(),
                result.userId(),
                result.tenantId(),
                result.email(),
                result.role()
        );
    }
}