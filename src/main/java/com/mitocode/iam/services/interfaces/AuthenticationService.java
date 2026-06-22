package com.mitocode.iam.services.interfaces;

import com.mitocode.iam.controllers.dtos.AuthResponse;
import com.mitocode.iam.controllers.dtos.LoginRequest;
import com.mitocode.iam.controllers.dtos.RegisterRequest;

public interface AuthenticationService {
    AuthResponse signIn(LoginRequest request);
    AuthResponse signUp(RegisterRequest request);
}
