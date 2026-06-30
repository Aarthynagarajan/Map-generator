package com.processmap.user.mapper;

import com.processmap.dto.UserResponseDTO;
import com.processmap.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserResponseDTO toResponseDTO(User user);
}
