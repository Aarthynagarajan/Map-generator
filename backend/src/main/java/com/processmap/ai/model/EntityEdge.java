package com.processmap.ai.model;

public record EntityEdge(
    String id,
    String from,
    String to,
    String medium,
    String direction,
    String label,
    String branchCondition
) {}
