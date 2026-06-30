package com.processmap.auth.controller;

import com.processmap.auth.service.AuthService;
import com.processmap.common.ApiResponse;
import com.processmap.dto.LoginRequest;
import com.processmap.dto.RefreshRequest;
import com.processmap.dto.RegisterRequest;
import com.processmap.dto.TokenResponse;
import com.processmap.dto.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponseDTO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.of(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.of(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.of(authService.refresh(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal UUID userId) {
        authService.logout(userId);
    }
}
