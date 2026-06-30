package com.processmap.symbol.repository;

import com.processmap.project.entity.Domain;
import com.processmap.symbol.entity.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SymbolRepository extends JpaRepository<Symbol, UUID> {
    Optional<Symbol> findBySymbolId(String symbolId);
    Optional<Symbol> findByDomainAndEntityClass(Domain domain, String entityClass);
    List<Symbol> findByDomain(Domain domain);
}
