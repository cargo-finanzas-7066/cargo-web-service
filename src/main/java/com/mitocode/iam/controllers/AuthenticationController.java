package com.mitocode.iam.controllers;

import com.mitocode.iam.controllers.dtos.AuthResponse;
import com.mitocode.iam.controllers.dtos.LoginRequest;
import com.mitocode.iam.controllers.dtos.RegisterRequest;
import com.mitocode.iam.controllers.dtos.UserResource;
import com.mitocode.iam.services.interfaces.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mitocode.iam.services.implementations.CurrentUserService;
import com.mitocode.iam.services.implementations.LoginAttemptService;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final CurrentUserService currentUserService;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody LoginRequest request) {
        loginAttemptService.check(request.getEmail());
        try {
            var response = authenticationService.signIn(request);
            loginAttemptService.success(request.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            loginAttemptService.failure(request.getEmail());
            throw ex;
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(authenticationService.signUp(request));
    }

    @GetMapping("/me")
    public UserResource me() {
        var user = currentUserService.requireUser();
        return new UserResource(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
