package com.servicehub.auth.infrastructure.rest;

import com.servicehub.auth.application.RegisterBusinessService;
import com.servicehub.auth.application.RegistrationResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final RegisterBusinessService registerBusinessService;

    public RegistrationController(RegisterBusinessService registerBusinessService) {
        this.registerBusinessService = registerBusinessService;
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
}