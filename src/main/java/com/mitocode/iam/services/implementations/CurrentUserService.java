package com.mitocode.iam.services.implementations;

import com.mitocode.exception.ResourceNotFoundException;
import com.mitocode.iam.persistence.entities.Role;
import com.mitocode.iam.persistence.entities.UserEntity;
import com.mitocode.iam.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public UserEntity requireUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Usuario autenticado no encontrado");
        }
        return userRepository.findByEmail(authentication.getName())
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }

    public boolean isAdmin() { return requireUser().getRole() == Role.ADMIN; }
}
