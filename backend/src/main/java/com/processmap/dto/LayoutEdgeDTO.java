package com.processmap.dto;

import java.util.List;

public record LayoutEdgeDTO(
    String id,
    String from,
    String to,
    String medium,
    String direction,
    String label,
    String branchCondition,
    List<PointDTO> routePoints,
    PointDTO labelPosition
) {}
