package com.servicehub.auth.infrastructure.rest;

import com.servicehub.auth.application.RegisterBusinessResult;

import java.util.UUID;

public record RegisterBusinessResponse(
        UUID tenantId,
        UUID businessId,
        UUID userId,
        String slug
) {

    public static RegisterBusinessResponse from(RegisterBusinessResult result) {
        return new RegisterBusinessResponse(
                result.tenantId(),
                result.businessId(),
                result.userId(),
                result.slug()
        );
    }
}