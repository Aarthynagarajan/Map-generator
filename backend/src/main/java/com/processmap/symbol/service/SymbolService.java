package com.processmap.symbol.service;

import com.processmap.ai.model.EntityGraph;
import com.processmap.ai.model.EntityNode;
import com.processmap.dto.SymbolGraph;
import com.processmap.dto.SymbolNode;
import com.processmap.project.entity.Domain;
import com.processmap.symbol.entity.Symbol;
import com.processmap.symbol.entity.Synonym;
import com.processmap.symbol.repository.SymbolRepository;
import com.processmap.symbol.repository.SynonymRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SymbolService {

    private final SymbolRepository symbolRepository;
    private final SynonymRepository synonymRepository;

    @Transactional(readOnly = true)
    public SymbolGraph mapSymbols(EntityGraph entityGraph) {
        log.info("Mapping symbols for domain: {}", entityGraph.domain());

        Domain domain = Domain.fromString(entityGraph.domain());
        if (domain == null) {
            throw new IllegalArgumentException("Invalid domain: " + entityGraph.domain());
        }

        List<SymbolNode> symbolNodes = new ArrayList<>();
        Map<String, Integer> tagCounters = new HashMap<>();

        for (EntityNode en : entityGraph.nodes()) {
            Symbol resolvedSymbol = resolveSymbol(en, domain);

            // Determine tag prefix and construct tag
            String prefix = resolvedSymbol != null && resolvedSymbol.getDefaultTagPrefix() != null 
                    ? resolvedSymbol.getDefaultTagPrefix() 
                    : "EQ";
            
            int count = tagCounters.getOrDefault(prefix, 100) + 1;
            tagCounters.put(prefix, count);
            String tag = prefix + "-" + count;

            String symbolId = resolvedSymbol != null ? resolvedSymbol.getSymbolId() : "GENERIC_UNKNOWN";
            String svgPath = resolvedSymbol != null ? resolvedSymbol.getSvgPath() : "/assets/symbols/generic_unknown.svg";
            boolean confirmRequired = en.userConfirmRequired() || (resolvedSymbol == null);
            double confidence = resolvedSymbol != null ? en.confidence() : 0.4;

            SymbolNode symbolNode = new SymbolNode(
                    en.id(),
                    en.label(),
                    en.entityClass(),
                    confidence,
                    en.aliases(),
                    en.medium(),
                    confirmRequired,
                    symbolId,
                    svgPath,
                    en.entityClass(),
                    tag,
                    en.label()
            );
            symbolNodes.add(symbolNode);
        }

        return new SymbolGraph(symbolNodes, entityGraph.edges(), entityGraph.branches(), entityGraph.domain());
    }

    private Symbol resolveSymbol(EntityNode node, Domain domain) {
        // 1. Exact Match by entityClass + domain
        Optional<Symbol> symbolOpt = symbolRepository.findByDomainAndEntityClass(domain, node.entityClass().toUpperCase());
        if (symbolOpt.isPresent()) {
            return symbolOpt.get();
        }

        // 2. Synonym lookup (case-insensitive) by label
        Optional<Synonym> synonymOpt = synonymRepository.findByTermIgnoreCase(node.label().trim());
        if (synonymOpt.isPresent()) {
            String resolvedClass = synonymOpt.get().getEntityClass();
            symbolOpt = symbolRepository.findByDomainAndEntityClass(domain, resolvedClass.toUpperCase());
            if (symbolOpt.isPresent()) {
                return symbolOpt.get();
            }
        }

        // 3. Synonym lookup by entityClass
        synonymOpt = synonymRepository.findByTermIgnoreCase(node.entityClass().trim());
        if (synonymOpt.isPresent()) {
            String resolvedClass = synonymOpt.get().getEntityClass();
            symbolOpt = symbolRepository.findByDomainAndEntityClass(domain, resolvedClass.toUpperCase());
            if (symbolOpt.isPresent()) {
                return symbolOpt.get();
            }
        }

        // 4. Fuzzy fallback check for common names in labels
        String labelLower = node.label().toLowerCase();
        if (labelLower.contains("pump")) {
            return symbolRepository.findByDomainAndEntityClass(domain, "CENTRIFUGAL_PUMP")
                    .orElse(symbolRepository.findByDomainAndEntityClass(domain, "HYDRAULIC_PUMP").orElse(null));
        } else if (labelLower.contains("valve")) {
            return symbolRepository.findByDomainAndEntityClass(domain, "GATE_VALVE")
                    .orElse(symbolRepository.findByDomainAndEntityClass(domain, "HYDRAULIC_VALVE").orElse(null));
        } else if (labelLower.contains("tank") || labelLower.contains("vessel")) {
            return symbolRepository.findByDomainAndEntityClass(domain, "STORAGE_TANK").orElse(null);
        } else if (labelLower.contains("breaker") || labelLower.contains("switch")) {
            return symbolRepository.findByDomainAndEntityClass(domain, "CIRCUIT_BREAKER")
                    .orElse(symbolRepository.findByDomainAndEntityClass(domain, "SWITCH").orElse(null));
        }

        return null;
    }
}
