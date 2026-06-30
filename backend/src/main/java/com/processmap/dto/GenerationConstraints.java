package com.processmap.dto;

public record GenerationConstraints(
    String layoutDirection, // LR, TB
    String tagScheme,       // ISA, IEC, custom
    String levelOfDetail    // high, medium, low
) {}
