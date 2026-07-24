package com.servicehub.business.domain;

import com.servicehub.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "business")
public class Business extends AuditableEntity {

    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    protected Business() {
    }

    public Business(UUID tenantId, String displayName, String email) {
        this.tenantId = tenantId;
        this.displayName = displayName;
        this.email = email;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void updateProfile(
            String displayName,
            String description,
            String phone,
            String email,
            String logoUrl
    ) {
        this.displayName = displayName;
        this.description = description;
        this.phone = phone;
        this.email = email;
        this.logoUrl = logoUrl;
    }
}