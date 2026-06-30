package com.processmap.history.repository;

import com.processmap.history.entity.Diagram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiagramRepository extends JpaRepository<Diagram, UUID> {
    Optional<Diagram> findByProjectIdAndIsCurrentTrue(UUID projectId);
    List<Diagram> findByProjectIdOrderByVersionDesc(UUID projectId);
    Optional<Diagram> findByIdAndProjectUserId(UUID id, UUID userId);
}
