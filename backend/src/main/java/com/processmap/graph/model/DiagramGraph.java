package com.processmap.graph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagramGraph {
    @Builder.Default
    private Map<String, TypedNode> nodes = new HashMap<>();

    @Builder.Default
    private Map<String, TypedEdge> edges = new HashMap<>();

    @Builder.Default
    private Map<String, List<String>> adjacency = new HashMap<>();

    private String domain;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
