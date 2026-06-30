package com.processmap.user.controller;

import com.processmap.common.ApiResponse;
import com.processmap.dto.UpdateProfileRequest;
import com.processmap.dto.UserResponseDTO;
import com.processmap.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<UserResponseDTO> getProfile(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(userService.getProfile(userId));
    }

    @PatchMapping
    public ApiResponse<UserResponseDTO> updateProfile(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.of(userService.updateProfile(userId, request));
    }
}
