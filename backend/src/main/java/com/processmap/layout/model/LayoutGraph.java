package com.processmap.layout.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayoutGraph {
    private List<LayoutNode> nodes;
    private List<LayoutEdge> edges;
    private String domain;
}
