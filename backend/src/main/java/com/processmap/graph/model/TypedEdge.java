package com.processmap.graph.model;

import com.processmap.dto.PointDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypedEdge {
    private String id;
    private String from;
    private String to;
    private String medium; // liquid, gas, electrical, hydraulic
    private String direction; // forward, reverse, bidirectional
    private List<PointDTO> routePoints;
    private String label;
    private String branchCondition;
    private String animationClass;
    private PointDTO labelPosition;
}
