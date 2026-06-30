package com.processmap.layout.service;

import com.processmap.dto.GenerationConstraints;
import com.processmap.dto.SymbolGraph;
import com.processmap.dto.SymbolNode;
import com.processmap.layout.model.LayoutGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LayoutServiceTest {

    private LayoutService layoutService;

    @BeforeEach
    void setUp() {
        layoutService = new LayoutService();
    }

    @Test
    void computeLayout_5NodeGraph_success() {
        List<SymbolNode> nodes = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            nodes.add(new SymbolNode(
                    "n" + i, "Node " + i, "RESERVOIR", 0.95,
                    Collections.emptyList(), "liquid", false,
                    "RESERVOIR", "/assets/res.svg", "RESERVOIR",
                    "TK-" + i, "Node " + i
            ));
        }

        SymbolGraph symbolGraph = new SymbolGraph(nodes, Collections.emptyList(), Collections.emptyList(), "industrial");
        GenerationConstraints constraints = new GenerationConstraints("LR", "ISA", "medium");

        LayoutGraph result = layoutService.computeLayout(symbolGraph, constraints);

        assertNotNull(result);
        assertEquals(5, result.getNodes().size());
        // Verify all nodes have assigned valid non-zero x and y coordinates
        result.getNodes().forEach(n -> {
            assertTrue(n.getX() >= 100);
            assertTrue(n.getY() >= 100);
        });
    }
}
