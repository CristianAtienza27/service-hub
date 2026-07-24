package com.servicehub.auth.infrastructure.rest;

import com.servicehub.auth.application.RegistrationResult;

import java.util.UUID;

public record RegisterBusinessResponse(
        UUID tenantId,
        UUID businessId,
        UUID userId,
        String slug
) {

    public static RegisterBusinessResponse from(RegistrationResult result) {
        return new RegisterBusinessResponse(
                result.tenantId(),
                result.businessId(),
                result.userId(),
                result.slug()
        );
    }
}