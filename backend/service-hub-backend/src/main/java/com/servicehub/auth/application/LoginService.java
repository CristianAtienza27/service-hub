package com.servicehub.auth.application;

import com.servicehub.auth.domain.AuthenticatedUser;
import com.servicehub.auth.domain.InvalidCredentialsException;
import com.servicehub.auth.infrastructure.security.JwtService;
import com.servicehub.user.domain.AppUser;
import com.servicehub.user.infrastructure.persistence.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class LoginService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResult login(String email, String rawPassword) {
        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        AppUser user = appUserRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .filter(AppUser::isActive)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                rawPassword,
                user.getPasswordHash()
        )) {
            throw new InvalidCredentialsException();
        }

        AuthenticatedUser authenticatedUser =
                new AuthenticatedUser(
                        user.getId(),
                        user.getTenantId(),
                        user.getEmail(),
                        user.getRole()
                );

        String token = jwtService.generateToken(authenticatedUser);

        return new LoginResult(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getRole()
        );
    }
}