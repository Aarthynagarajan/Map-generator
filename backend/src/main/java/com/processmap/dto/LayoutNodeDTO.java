package com.processmap.dto;

import java.util.List;

public record LayoutNodeDTO(
    String id,
    String label,
    String entityClass,
    String symbolId,
    String symbolSvgPath,
    String symbolSubtype,
    String tag,
    String displayLabel,
    double confidence,
    List<String> aliases,
    String medium,
    boolean userConfirmRequired,
    double x,
    double y,
    double width,
    double height,
    String orientation,
    boolean locked
) {}
