package com.processmap.auth.service;

import com.processmap.auth.entity.RefreshToken;
import com.processmap.auth.repository.RefreshTokenRepository;
import com.processmap.dto.*;
import com.processmap.exception.AppException;
import com.processmap.exception.ErrorCode;
import com.processmap.security.JwtTokenProvider;
import com.processmap.user.entity.Role;
import com.processmap.user.entity.User;
import com.processmap.user.mapper.UserMapper;
import com.processmap.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDTO register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.DUPLICATE_EMAIL, "Email is already registered", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name(), user.getEmail());
        String rawRefreshToken = UUID.randomUUID().toString();

        // Save refresh token
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawRefreshToken))
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        // Revoke old tokens before saving new one
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenResponse(accessToken, rawRefreshToken);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        log.info("Refreshing token");
        String rawRefreshToken = request.refreshToken();
        String hashedToken = hashToken(rawRefreshToken);

        RefreshToken t = refreshTokenRepository.findByTokenHashAndRevokedFalse(hashedToken)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED));

        if (t.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }

        User user = t.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name(), user.getEmail());
        String newRawRefreshToken = UUID.randomUUID().toString();

        t.setRevoked(true);
        refreshTokenRepository.save(t);

        RefreshToken newEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(newRawRefreshToken))
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newEntity);

        return new TokenResponse(newAccessToken, newRawRefreshToken);
    }

    @Transactional
    public void logout(UUID userId) {
        log.info("Logging out user: {}", userId);
        refreshTokenRepository.deleteByUserId(userId);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
