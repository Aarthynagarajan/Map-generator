package com.processmap.auth.service;

import com.processmap.auth.entity.RefreshToken;
import com.processmap.auth.repository.RefreshTokenRepository;
import com.processmap.dto.LoginRequest;
import com.processmap.dto.RegisterRequest;
import com.processmap.dto.TokenResponse;
import com.processmap.dto.UserResponseDTO;
import com.processmap.exception.AppException;
import com.processmap.security.JwtTokenProvider;
import com.processmap.user.entity.Role;
import com.processmap.user.entity.User;
import com.processmap.user.mapper.UserMapper;
import com.processmap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@processpro.io")
                .passwordHash("hashedPassword")
                .displayName("Test User")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("test@processpro.io", "password123", "Test User");
        UserResponseDTO responseDTO = new UserResponseDTO(userId, "test@processpro.io", "Test User", "USER", null, null);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = authService.register(request);

        assertNotNull(result);
        assertEquals("test@processpro.io", result.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest("test@processpro.io", "password123", "Test User");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(AppException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("test@processpro.io", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user.getId(), "USER", user.getEmail())).thenReturn("accessToken");

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void login_invalidPassword_throwsException() {
        LoginRequest request = new LoginRequest("test@processpro.io", "wrongpassword");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        assertThrows(AppException.class, () -> authService.login(request));
    }
}
