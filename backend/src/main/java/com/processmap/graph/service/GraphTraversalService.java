package com.processmap.graph.service;

import com.processmap.graph.model.DiagramGraph;
import com.processmap.graph.model.TypedEdge;
import com.processmap.graph.model.TypedNode;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphTraversalService {

    private static final Set<String> STOPPER_CLASSES = Set.of(
            "GATE_VALVE", "BALL_VALVE", "BUTTERFLY_VALVE", "CHECK_VALVE",
            "CIRCUIT_BREAKER", "SWITCH", "RELAY", "LIMIT_SWITCH", "PUSH_BUTTON",
            "HYDRAULIC_VALVE", "CHECK_VALVE_HYD", "FLOW_CONTROL_VALVE", "DIRECTIONAL_CONTROL_VALVE"
    );

    public Set<String> bfsActivePaths(DiagramGraph graph, String sourceId, Map<String, String> stopperStates) {
        Set<String> activeEdges = new HashSet<>();
        if (graph == null || !graph.getNodes().containsKey(sourceId)) {
            return activeEdges;
        }

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(sourceId);

        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            if (visited.contains(nodeId)) {
                continue;
            }
            visited.add(nodeId);

            // Find all outgoing edges from nodeId
            List<String> adjEdges = graph.getAdjacency().get(nodeId);
            if (adjEdges == null) {
                continue;
            }

            for (String edgeId : adjEdges) {
                TypedEdge edge = graph.getEdges().get(edgeId);
                if (edge == null) {
                    continue;
                }

                String targetNodeId = edge.getTo();
                TypedNode targetNode = graph.getNodes().get(targetNodeId);
                if (targetNode == null) {
                    continue;
                }

                // Check stopper blocking
                boolean isBlocked = false;
                if (STOPPER_CLASSES.contains(targetNode.getEntityClass())) {
                    String state = stopperStates != null ? stopperStates.get(targetNodeId) : null;
                    if (state == null) {
                        state = targetNode.getState(); // default
                    }
                    if ("closed".equalsIgnoreCase(state) || "off".equalsIgnoreCase(state)) {
                        isBlocked = true;
                    }
                }

                // Check check valve reverse blocking
                if ("CHECK_VALVE".equalsIgnoreCase(targetNode.getEntityClass()) || "CHECK_VALVE_HYD".equalsIgnoreCase(targetNode.getEntityClass())) {
                    if ("reverse".equalsIgnoreCase(edge.getDirection())) {
                        isBlocked = true;
                    }
                }

                if (!isBlocked) {
                    activeEdges.add(edgeId);
                    if (!visited.contains(targetNodeId)) {
                        queue.add(targetNodeId);
                    }
                }
            }
        }

        return activeEdges;
    }

    public boolean detectCycles(DiagramGraph graph) {
        if (graph == null || graph.getNodes().isEmpty()) {
            return false;
        }

        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();

        for (String nodeId : graph.getNodes().keySet()) {
            if (hasCycleUtil(graph, nodeId, visited, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCycleUtil(DiagramGraph graph, String nodeId, Set<String> visited, Set<String> stack) {
        if (stack.contains(nodeId)) {
            return true;
        }
        if (visited.contains(nodeId)) {
            return false;
        }

        visited.add(nodeId);
        stack.add(nodeId);

        List<String> adjEdges = graph.getAdjacency().get(nodeId);
        if (adjEdges != null) {
            for (String edgeId : adjEdges) {
                TypedEdge edge = graph.getEdges().get(edgeId);
                if (edge != null && hasCycleUtil(graph, edge.getTo(), visited, stack)) {
                    return true;
                }
            }
        }

        stack.remove(nodeId);
        return false;
    }

    public List<String> findSourceNodes(DiagramGraph graph) {
        List<String> sources = new ArrayList<>();
        if (graph == null) {
            return sources;
        }

        Set<String> targets = new HashSet<>();
        for (TypedEdge edge : graph.getEdges().values()) {
            targets.add(edge.getTo());
        }

        for (String nodeId : graph.getNodes().keySet()) {
            if (!targets.contains(nodeId)) {
                sources.add(nodeId);
            }
        }
        return sources;
    }

    public List<String> findSinkNodes(DiagramGraph graph) {
        List<String> sinks = new ArrayList<>();
        if (graph == null) {
            return sinks;
        }

        for (String nodeId : graph.getNodes().keySet()) {
            List<String> adj = graph.getAdjacency().get(nodeId);
            if (adj == null || adj.isEmpty()) {
                sinks.add(nodeId);
            }
        }
        return sinks;
    }
}
