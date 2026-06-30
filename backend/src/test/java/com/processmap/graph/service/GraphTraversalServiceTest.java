package com.processmap.graph.service;

import com.processmap.dto.PointDTO;
import com.processmap.graph.model.DiagramGraph;
import com.processmap.graph.model.TypedEdge;
import com.processmap.graph.model.TypedNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GraphTraversalServiceTest {

    private GraphTraversalService traversalService;

    @BeforeEach
    void setUp() {
        traversalService = new GraphTraversalService();
    }

    // Fixture 1: Simple linear graph
    private DiagramGraph createLinearGraph() {
        DiagramGraph graph = new DiagramGraph();
        graph.getNodes().put("n1", TypedNode.builder().id("n1").entityClass("RESERVOIR").state("open").build());
        graph.getNodes().put("n2", TypedNode.builder().id("n2").entityClass("GATE_VALVE").state("open").build());
        graph.getNodes().put("n3", TypedNode.builder().id("n3").entityClass("STORAGE_TANK").state("open").build());

        graph.getEdges().put("e1", TypedEdge.builder().id("e1").from("n1").to("n2").direction("forward").build());
        graph.getEdges().put("e2", TypedEdge.builder().id("e2").from("n2").to("n3").direction("forward").build());

        graph.getAdjacency().put("n1", List.of("e1"));
        graph.getAdjacency().put("n2", List.of("e2"));
        return graph;
    }

    // Fixture 2: Cycle graph
    private DiagramGraph createCyclicGraph() {
        DiagramGraph graph = new DiagramGraph();
        graph.getNodes().put("n1", TypedNode.builder().id("n1").entityClass("RESERVOIR").build());
        graph.getNodes().put("n2", TypedNode.builder().id("n2").entityClass("GATE_VALVE").build());
        graph.getNodes().put("n3", TypedNode.builder().id("n3").entityClass("STORAGE_TANK").build());

        graph.getEdges().put("e1", TypedEdge.builder().id("e1").from("n1").to("n2").build());
        graph.getEdges().put("e2", TypedEdge.builder().id("e2").from("n2").to("n3").build());
        graph.getEdges().put("e3", TypedEdge.builder().id("e3").from("n3").to("n1").build());

        graph.getAdjacency().put("n1", List.of("e1"));
        graph.getAdjacency().put("n2", List.of("e2"));
        graph.getAdjacency().put("n3", List.of("e3"));
        return graph;
    }

    @Test
    void testBfsActivePaths_allOpen() {
        DiagramGraph graph = createLinearGraph();
        Map<String, String> stopperStates = new HashMap<>();
        stopperStates.put("n2", "open");

        Set<String> activePaths = traversalService.bfsActivePaths(graph, "n1", stopperStates);

        assertEquals(2, activePaths.size());
        assertTrue(activePaths.contains("e1"));
        assertTrue(activePaths.contains("e2"));
    }

    @Test
    void testBfsActivePaths_closedStopper_blocksDownstream() {
        DiagramGraph graph = createLinearGraph();
        Map<String, String> stopperStates = new HashMap<>();
        stopperStates.put("n2", "closed"); // GV closed

        Set<String> activePaths = traversalService.bfsActivePaths(graph, "n1", stopperStates);

        assertEquals(0, activePaths.size());
        assertFalse(activePaths.contains("e1"));
        assertFalse(activePaths.contains("e2")); // blocked past n2
    }

    @Test
    void testCycleDetection_acyclic() {
        DiagramGraph graph = createLinearGraph();
        assertFalse(traversalService.detectCycles(graph));
    }

    @Test
    void testCycleDetection_cyclic() {
        DiagramGraph graph = createCyclicGraph();
        assertTrue(traversalService.detectCycles(graph));
    }

    @Test
    void testFindSourcesAndSinks() {
        DiagramGraph graph = createLinearGraph();
        List<String> sources = traversalService.findSourceNodes(graph);
        List<String> sinks = traversalService.findSinkNodes(graph);

        assertEquals(1, sources.size());
        assertEquals("n1", sources.get(0));

        assertEquals(1, sinks.size());
        assertEquals("n3", sinks.get(0));
    }
}
