package com.processmap.ai.model;

import java.util.List;

public record EntityGraph(
    List<EntityNode> nodes,
    List<EntityEdge> edges,
    List<BranchCondition> branches,
    String domain
) {}
