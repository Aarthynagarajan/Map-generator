package com.processmap.user.service;

import com.processmap.dto.UpdateProfileRequest;
import com.processmap.dto.UserResponseDTO;
import com.processmap.exception.AppException;
import com.processmap.exception.ErrorCode;
import com.processmap.user.entity.User;
import com.processmap.user.mapper.UserMapper;
import com.processmap.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDTO getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User profile not found", HttpStatus.NOT_FOUND));
        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User profile not found", HttpStatus.NOT_FOUND));

        user.setDisplayName(request.displayName());
        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDTO(updatedUser);
    }
}
