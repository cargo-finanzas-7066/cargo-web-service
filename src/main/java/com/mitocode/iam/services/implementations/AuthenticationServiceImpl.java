package com.mitocode.iam.services.implementations;

import com.mitocode.iam.controllers.dtos.AuthResponse;
import com.mitocode.iam.controllers.dtos.LoginRequest;
import com.mitocode.iam.controllers.dtos.RegisterRequest;
import com.mitocode.iam.controllers.dtos.UserResource;
import com.mitocode.iam.persistence.entities.UserEntity;
import com.mitocode.iam.persistence.repositories.UserRepository;
import com.mitocode.iam.services.interfaces.AuthenticationService;
import com.mitocode.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse signIn(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }

        return toAuthResponse(user);
    }

    @Override
    public AuthResponse signUp(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        var user = new UserEntity();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return toAuthResponse(userRepository.save(user));
    }

    private AuthResponse toAuthResponse(UserEntity user) {
        var token = jwtUtil.generate(user.getEmail());
        var resource = new UserResource(user.getId(), user.getName(), user.getEmail(), user.getRole());
        return new AuthResponse(token, resource);
    }
}
