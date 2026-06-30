package com.processmap.ai.model;

import java.util.List;

public record EntityNode(
    String id,
    String label,
    String entityClass,
    double confidence,
    List<String> aliases,
    String medium,
    boolean userConfirmRequired
) {}
