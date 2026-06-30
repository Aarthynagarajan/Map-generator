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
                .tokenHash(passwordEncoder.encode(rawRefreshToken))
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

        // Since we store BCrypt hashes of refresh tokens, we must load active tokens and verify matches
        // For performance in MVP, we find active tokens and search.
        // Let's load all unrevoked tokens and verify.
        // Wait, to keep it highly secure and standard, we verify the hash.
        // Let's implement active refresh token scan.
        return refreshTokenRepository.findAll().stream()
                .filter(t -> !t.getRevoked() && t.getExpiresAt().isAfter(OffsetDateTime.now()))
                .filter(t -> passwordEncoder.matches(rawRefreshToken, t.getTokenHash()))
                .findFirst()
                .map(t -> {
                    User user = t.getUser();
                    String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name(), user.getEmail());
                    String newRawRefreshToken = UUID.randomUUID().toString();

                    t.setRevoked(true);
                    refreshTokenRepository.save(t);

                    RefreshToken newEntity = RefreshToken.builder()
                            .user(user)
                            .tokenHash(passwordEncoder.encode(newRawRefreshToken))
                            .expiresAt(OffsetDateTime.now().plusDays(7))
                            .revoked(false)
                            .build();
                    refreshTokenRepository.save(newEntity);

                    return new TokenResponse(newAccessToken, newRawRefreshToken);
                })
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED));
    }

    @Transactional
    public void logout(UUID userId) {
        log.info("Logging out user: {}", userId);
        refreshTokenRepository.deleteByUserId(userId);
    }
}
