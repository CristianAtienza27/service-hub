package com.servicehub.auth.infrastructure.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterBusinessRequest(

        @NotBlank(message = "Business name is required")
        @Size(max = 150, message = "Business name cannot exceed 150 characters")
        String businessName,

        @NotBlank(message = "Slug is required")
        @Size(min = 3, max = 100, message = "Slug must contain between 3 and 100 characters")
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug can only contain lowercase letters, numbers and hyphens"
        )
        String slug,

        @NotBlank(message = "Owner email is required")
        @Email(message = "Owner email must be valid")
        @Size(max = 255)
        String ownerEmail,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must contain between 8 and 72 characters")
        String password
) {
}