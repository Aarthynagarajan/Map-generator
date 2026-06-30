package com.processmap.symbol.repository;

import com.processmap.symbol.entity.Synonym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SynonymRepository extends JpaRepository<Synonym, UUID> {
    Optional<Synonym> findByTermIgnoreCase(String term);
}
