package com.servicehub.service_request.domain;

import com.servicehub.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "service_request")
public class ServiceRequest extends AuditableEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ServiceRequestStatus status;

    protected ServiceRequest() {
    }

    public ServiceRequest(
            UUID tenantId,
            String customerName,
            String customerEmail,
            String customerPhone,
            String description
    ) {
        this.tenantId = tenantId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.description = description;
        this.status = ServiceRequestStatus.NEW;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getDescription() {
        return description;
    }

    public ServiceRequestStatus getStatus() {
        return status;
    }

    public void changeStatus(ServiceRequestStatus status) {
        this.status = status;
    }
}