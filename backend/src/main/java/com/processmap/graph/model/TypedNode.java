package com.processmap.graph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypedNode {
    private String id;
    private String label;
    private String entityClass;
    private String symbolId;
    private double x;
    private double y;
    private double width;
    private double height;
    private String orientation; // horizontal, vertical
    private boolean locked;
    private String state; // open, closed, on, off, partial
    private double confidence;
    private String medium; // liquid, gas, electrical, hydraulic
    private String tag;
    private Map<String, Object> anchors;
    private List<String> aliases;
    private boolean userConfirmRequired;
}
