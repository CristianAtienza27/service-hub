package com.servicehub.service_request.infrastructure.persistence;

import com.servicehub.service_request.domain.ServiceRequest;
import com.servicehub.service_request.domain.ServiceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, UUID> {

    List<ServiceRequest> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<ServiceRequest> findAllByTenantIdAndStatusOrderByCreatedAtDesc(
            UUID tenantId,
            ServiceRequestStatus status
    );

    Optional<ServiceRequest> findByIdAndTenantId(UUID id, UUID tenantId);
}