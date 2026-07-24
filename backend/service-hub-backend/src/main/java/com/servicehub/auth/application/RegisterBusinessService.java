package com.servicehub.auth.application;

import com.servicehub.auth.domain.DuplicateEmailException;
import com.servicehub.auth.domain.DuplicateSlugException;
import com.servicehub.business.domain.Business;
import com.servicehub.business.infrastructure.persistence.BusinessRepository;
import com.servicehub.tenant.domain.Tenant;
import com.servicehub.tenant.infrastructure.persistence.TenantRepository;
import com.servicehub.user.domain.AppUser;
import com.servicehub.user.domain.UserRole;
import com.servicehub.user.infrastructure.persistence.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegisterBusinessService {

    private final TenantRepository tenantRepository;
    private final BusinessRepository businessRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterBusinessService(
            TenantRepository tenantRepository,
            BusinessRepository businessRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.tenantRepository = tenantRepository;
        this.businessRepository = businessRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterBusinessResult register(
            String businessName,
            String slug,
            String ownerEmail,
            String rawPassword
    ) {
        String normalizedName = businessName.trim();
        String normalizedSlug = slug.trim().toLowerCase(Locale.ROOT);
        String normalizedEmail = ownerEmail.trim().toLowerCase(Locale.ROOT);

        if (tenantRepository.existsBySlug(normalizedSlug)) {
            throw new DuplicateSlugException(normalizedSlug);
        }

        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        Tenant tenant = new Tenant(normalizedName, normalizedSlug);
        tenantRepository.save(tenant);

        Business business = new Business(
                tenant.getId(),
                normalizedName,
                normalizedEmail
        );
        businessRepository.save(business);

        AppUser owner = new AppUser(
                tenant.getId(),
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                UserRole.BUSINESS_OWNER
        );
        appUserRepository.save(owner);

        return new RegisterBusinessResult(
                tenant.getId(),
                business.getId(),
                owner.getId(),
                tenant.getSlug()
        );
    }
}