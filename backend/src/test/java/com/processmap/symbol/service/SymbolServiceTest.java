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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymbolServiceTest {

    @Mock
    private SymbolRepository symbolRepository;

    @Mock
    private SynonymRepository synonymRepository;

    @InjectMocks
    private SymbolService symbolService;

    private Symbol pumpSymbol;
    private Symbol valveSymbol;

    @BeforeEach
    void setUp() {
        pumpSymbol = Symbol.builder()
                .symbolId("CENTRIFUGAL_PUMP")
                .entityClass("CENTRIFUGAL_PUMP")
                .domain(Domain.INDUSTRIAL)
                .svgPath("/assets/pump.svg")
                .defaultTagPrefix("P")
                .build();

        valveSymbol = Symbol.builder()
                .symbolId("GATE_VALVE")
                .entityClass("GATE_VALVE")
                .domain(Domain.INDUSTRIAL)
                .svgPath("/assets/valve.svg")
                .defaultTagPrefix("V")
                .build();
    }

    @Test
    void mapSymbols_exactMatch() {
        EntityNode node = new EntityNode("n1", "P-101", "CENTRIFUGAL_PUMP", 0.95, Collections.emptyList(), "liquid", false);
        EntityGraph graph = new EntityGraph(Collections.singletonList(node), Collections.emptyList(), Collections.emptyList(), "industrial");

        when(symbolRepository.findByDomainAndEntityClass(Domain.INDUSTRIAL, "CENTRIFUGAL_PUMP"))
                .thenReturn(Optional.of(pumpSymbol));

        SymbolGraph result = symbolService.mapSymbols(graph);

        assertNotNull(result);
        assertEquals(1, result.nodes().size());
        SymbolNode mappedNode = result.nodes().get(0);
        assertEquals("CENTRIFUGAL_PUMP", mappedNode.symbolId());
        assertEquals("P-101", mappedNode.tag()); // Tag counts from P-101
    }

    @Test
    void mapSymbols_synonymMatch() {
        EntityNode node = new EntityNode("n1", "shutoff valve", "VALVE", 0.8, Collections.emptyList(), "liquid", false);
        EntityGraph graph = new EntityGraph(Collections.singletonList(node), Collections.emptyList(), Collections.emptyList(), "industrial");

        Synonym synonym = Synonym.builder().term("shutoff valve").entityClass("GATE_VALVE").domain(Domain.INDUSTRIAL).build();

        when(symbolRepository.findByDomainAndEntityClass(Domain.INDUSTRIAL, "VALVE")).thenReturn(Optional.empty());
        when(synonymRepository.findByTermIgnoreCase("shutoff valve")).thenReturn(Optional.of(synonym));
        when(symbolRepository.findByDomainAndEntityClass(Domain.INDUSTRIAL, "GATE_VALVE")).thenReturn(Optional.of(valveSymbol));

        SymbolGraph result = symbolService.mapSymbols(graph);

        assertNotNull(result);
        assertEquals("GATE_VALVE", result.nodes().get(0).symbolId());
    }

    @Test
    void mapSymbols_fallbackToGenericUnknown() {
        EntityNode node = new EntityNode("n1", "Exotic Device", "EXOTIC", 0.5, Collections.emptyList(), "liquid", false);
        EntityGraph graph = new EntityGraph(Collections.singletonList(node), Collections.emptyList(), Collections.emptyList(), "industrial");

        when(symbolRepository.findByDomainAndEntityClass(Domain.INDUSTRIAL, "EXOTIC")).thenReturn(Optional.empty());
        when(synonymRepository.findByTermIgnoreCase("Exotic Device")).thenReturn(Optional.empty());
        when(synonymRepository.findByTermIgnoreCase("EXOTIC")).thenReturn(Optional.empty());

        SymbolGraph result = symbolService.mapSymbols(graph);

        assertNotNull(result);
        SymbolNode mappedNode = result.nodes().get(0);
        assertEquals("GENERIC_UNKNOWN", mappedNode.symbolId());
        assertTrue(mappedNode.userConfirmRequired());
    }
}
