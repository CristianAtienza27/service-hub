package com.servicehub.auth.infrastructure.rest;

import com.servicehub.auth.application.LoginResult;
import com.servicehub.auth.application.LoginService;
import com.servicehub.auth.application.RegisterBusinessService;
import com.servicehub.auth.application.RegistrationResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final RegisterBusinessService registerBusinessService;
    private final LoginService loginService;

    public RegistrationController(
            RegisterBusinessService registerBusinessService,
            LoginService loginService
    ) {
        this.registerBusinessService = registerBusinessService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterBusinessResponse register(
            @Valid @RequestBody RegisterBusinessRequest request
    ) {
        RegistrationResult result = registerBusinessService.register(
                request.businessName(),
                request.slug(),
                request.ownerEmail(),
                request.password()
        );

        return RegisterBusinessResponse.from(result);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResult result = loginService.login(
                request.email(),
                request.password()
        );

        return LoginResponse.from(result);
    }
}