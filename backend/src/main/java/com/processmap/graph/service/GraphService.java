package com.processmap.graph.service;

import com.processmap.ai.model.EntityEdge;
import com.processmap.ai.model.EntityGraph;
import com.processmap.ai.model.EntityNode;
import com.processmap.graph.model.DiagramGraph;
import com.processmap.graph.model.TypedEdge;
import com.processmap.graph.model.TypedNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class GraphService {

    public DiagramGraph buildGraph(EntityGraph entityGraph) {
        if (entityGraph == null) {
            return new DiagramGraph();
        }

        Map<String, TypedNode> nodesMap = new HashMap<>();
        for (EntityNode en : entityGraph.nodes()) {
            TypedNode node = TypedNode.builder()
                    .id(en.id())
                    .label(en.label())
                    .entityClass(en.entityClass())
                    .symbolId("GENERIC_UNKNOWN") // default before symbol mapping
                    .x(0)
                    .y(0)
                    .width(80)
                    .height(60)
                    .orientation("horizontal")
                    .locked(false)
                    .state("open")
                    .confidence(en.confidence())
                    .medium(en.medium())
                    .aliases(en.aliases())
                    .userConfirmRequired(en.userConfirmRequired())
                    .build();
            nodesMap.put(node.getId(), node);
        }

        Map<String, TypedEdge> edgesMap = new HashMap<>();
        Map<String, List<String>> adjacency = new HashMap<>();

        // Remove duplicate edges (same from, to, medium)
        Set<String> uniqueKeys = new java.util.HashSet<>();

        for (EntityEdge ee : entityGraph.edges()) {
            String key = ee.from() + "->" + ee.to() + ":" + ee.medium();
            if (uniqueKeys.contains(key)) {
                continue;
            }
            uniqueKeys.add(key);

            TypedEdge edge = TypedEdge.builder()
                    .id(ee.id())
                    .from(ee.from())
                    .to(ee.to())
                    .medium(ee.medium())
                    .direction(ee.direction() != null ? ee.direction() : "forward")
                    .label(ee.label())
                    .branchCondition(ee.branchCondition())
                    .routePoints(new ArrayList<>())
                    .build();

            edgesMap.put(edge.getId(), edge);

            // Update adjacency
            adjacency.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(edge.getId());
        }

        return DiagramGraph.builder()
                .nodes(nodesMap)
                .edges(edgesMap)
                .adjacency(adjacency)
                .domain(entityGraph.domain())
                .build();
    }
}
