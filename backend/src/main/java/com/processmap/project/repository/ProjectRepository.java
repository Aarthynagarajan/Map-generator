package com.processmap.project.repository;

import com.processmap.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findByUserId(UUID userId, Pageable pageable);
    Optional<Project> findByIdAndUserId(UUID id, UUID userId);
}
