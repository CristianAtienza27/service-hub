package com.servicehub.business.infrastructure.persistence;

import com.servicehub.business.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findByTenantId(UUID tenantId);
}