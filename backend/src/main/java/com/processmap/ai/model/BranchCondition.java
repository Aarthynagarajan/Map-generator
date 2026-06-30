package com.processmap.ai.model;

import java.util.List;

public record BranchCondition(
    String condition,
    List<String> paths
) {}
