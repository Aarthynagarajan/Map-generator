package com.processmap.graph.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.processmap.graph.model.DiagramGraph;
import com.processmap.graph.model.TypedEdge;
import com.processmap.graph.model.TypedNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GraphSerializer {

    private final ObjectMapper objectMapper;

    public JsonNode serialize(DiagramGraph graph) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("schemaVersion", "1.0");
        root.put("domain", graph.getDomain());
        root.set("metadata", objectMapper.valueToTree(graph.getMetadata()));

        ArrayNode nodesArray = objectMapper.valueToTree(new ArrayList<>(graph.getNodes().values()));
        root.set("nodes", nodesArray);

        ArrayNode edgesArray = objectMapper.valueToTree(new ArrayList<>(graph.getEdges().values()));
        root.set("edges", edgesArray);

        ObjectNode adjObject = objectMapper.valueToTree(graph.getAdjacency());
        root.set("adjacency", adjObject);

        return root;
    }

    public DiagramGraph deserialize(JsonNode json) {
        if (json == null || json.isNull()) {
            return new DiagramGraph();
        }

        DiagramGraph graph = new DiagramGraph();
        graph.setDomain(json.has("domain") ? json.get("domain").asText() : "");

        if (json.has("metadata")) {
            graph.setMetadata(objectMapper.convertValue(json.get("metadata"), new TypeReference<Map<String, Object>>() {}));
        }

        if (json.has("nodes")) {
            List<TypedNode> nodeList = objectMapper.convertValue(json.get("nodes"), new TypeReference<List<TypedNode>>() {});
            Map<String, TypedNode> nodesMap = new HashMap<>();
            for (TypedNode node : nodeList) {
                nodesMap.put(node.getId(), node);
            }
            graph.setNodes(nodesMap);
        }

        if (json.has("edges")) {
            List<TypedEdge> edgeList = objectMapper.convertValue(json.get("edges"), new TypeReference<List<TypedEdge>>() {});
            Map<String, TypedEdge> edgesMap = new HashMap<>();
            for (TypedEdge edge : edgeList) {
                edgesMap.put(edge.getId(), edge);
            }
            graph.setEdges(edgesMap);
        }

        if (json.has("adjacency")) {
            Map<String, List<String>> adjMap = objectMapper.convertValue(json.get("adjacency"), new TypeReference<Map<String, List<String>>>() {});
            graph.setAdjacency(adjMap);
        }

        return graph;
    }
}
