package com.processmap.graph.service;

import com.processmap.graph.model.DiagramGraph;
import com.processmap.graph.model.TypedEdge;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GraphValidationService {

    public record ValidationResult(
        boolean isValid,
        List<String> errors
    ) {}

    public ValidationResult validate(DiagramGraph graph) {
        List<String> errors = new ArrayList<>();

        if (graph == null || graph.getNodes().isEmpty()) {
            errors.add("Graph contains no nodes");
            return new ValidationResult(false, errors);
        }

        // Validate edge references
        Set<String> nodeIds = graph.getNodes().keySet();
        for (TypedEdge edge : graph.getEdges().values()) {
            if (!nodeIds.contains(edge.getFrom())) {
                errors.add("Edge " + edge.getId() + " references non-existent source node: " + edge.getFrom());
            }
            if (!nodeIds.contains(edge.getTo())) {
                errors.add("Edge " + edge.getId() + " references non-existent target node: " + edge.getTo());
            }
        }

        // Validate source nodes presence
        Set<String> targets = new HashSet<>();
        for (TypedEdge edge : graph.getEdges().values()) {
            targets.add(edge.getTo());
        }

        boolean hasSource = false;
        for (String nodeId : nodeIds) {
            if (!targets.contains(nodeId)) {
                hasSource = true;
                break;
            }
        }

        if (!hasSource && !graph.getEdges().isEmpty()) {
            errors.add("Graph contains no source nodes (potential closed loop without inlet)");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}
