package com.processmap.layout.preset;

public record LayoutPresetConfig(
    String name,
    String direction, // LR, TB
    int rankSpacing,   // spacing between levels
    int nodeSpacing,   // spacing between nodes in same level
    String alignment   // horizontal, vertical
) {
    public static final LayoutPresetConfig PROCESS_FLOW = new LayoutPresetConfig(
        "process-flow", "LR", 80, 40, "horizontal"
    );

    public static final LayoutPresetConfig ELECTRICAL_BUS = new LayoutPresetConfig(
        "electrical-bus", "TB", 60, 30, "vertical"
    );

    public static final LayoutPresetConfig LADDER_LOGIC = new LayoutPresetConfig(
        "ladder-logic", "LR", 50, 30, "horizontal"
    );

    public static final LayoutPresetConfig HYDRAULIC_LOOP = new LayoutPresetConfig(
        "hydraulic-loop", "TB", 80, 50, "vertical"
    );

    public static LayoutPresetConfig getPreset(String name) {
        if ("electrical-bus".equalsIgnoreCase(name)) {
            return ELECTRICAL_BUS;
        } else if ("ladder-logic".equalsIgnoreCase(name)) {
            return LADDER_LOGIC;
        } else if ("hydraulic-loop".equalsIgnoreCase(name)) {
            return HYDRAULIC_LOOP;
        }
        return PROCESS_FLOW; // default
    }
}
