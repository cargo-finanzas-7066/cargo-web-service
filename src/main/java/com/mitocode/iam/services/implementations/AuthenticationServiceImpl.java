package com.mitocode.iam.services.implementations;

import com.mitocode.iam.controllers.dtos.AuthResponse;
import com.mitocode.iam.controllers.dtos.LoginRequest;
import com.mitocode.iam.controllers.dtos.RegisterRequest;
import com.mitocode.iam.controllers.dtos.UserResource;
import com.mitocode.iam.persistence.entities.UserEntity;
import com.mitocode.iam.persistence.entities.Role;
import com.mitocode.exception.ConflictException;
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
        var user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos"));

        if (!Boolean.TRUE.equals(user.getActive()) || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }

        return toAuthResponse(user);
    }

    @Override
    public AuthResponse signUp(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("El correo ya está registrado");
        }

        var user = new UserEntity();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADVISOR);

        return toAuthResponse(userRepository.save(user));
    }

    private AuthResponse toAuthResponse(UserEntity user) {
        var token = jwtUtil.generate(user);
        var resource = new UserResource(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, resource);
    }
}
