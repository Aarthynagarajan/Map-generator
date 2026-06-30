package com.processmap.dto;

import java.util.List;

public record SymbolNode(
    String id,
    String label,
    String entityClass,
    double confidence,
    List<String> aliases,
    String medium,
    boolean userConfirmRequired,
    String symbolId,
    String symbolSvgPath,
    String symbolSubtype,
    String tag,
    String displayLabel
) {}
