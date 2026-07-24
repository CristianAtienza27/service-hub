package com.servicehub.auth.infrastructure.rest;

import com.servicehub.auth.domain.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class CurrentUserController {

    @GetMapping
    public AuthenticatedUser currentUser(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return user;
    }
}