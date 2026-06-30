package com.processmap.simulation.repository;

import com.processmap.simulation.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, UUID> {
    List<Scenario> findByDiagramId(UUID diagramId);
    Optional<Scenario> findByDiagramIdAndIsDefaultTrue(UUID diagramId);
}
