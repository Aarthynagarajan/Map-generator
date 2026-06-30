package com.processmap.project.mapper;

import com.processmap.dto.ProjectResponseDTO;
import com.processmap.project.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "domain", expression = "java(project.getDomain().name())")
    ProjectResponseDTO toResponseDTO(Project project);
}
