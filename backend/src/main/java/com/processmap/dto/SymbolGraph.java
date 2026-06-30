package com.processmap.dto;

import com.processmap.ai.model.BranchCondition;
import com.processmap.ai.model.EntityEdge;
import java.util.List;

public record SymbolGraph(
    List<SymbolNode> nodes,
    List<EntityEdge> edges,
    List<BranchCondition> branches,
    String domain
) {}
