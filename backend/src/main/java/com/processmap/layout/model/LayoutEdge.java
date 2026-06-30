package com.processmap.layout.model;

import com.processmap.ai.model.EntityEdge;
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
public class LayoutEdge {
    private EntityEdge edge;
    private List<PointDTO> routePoints;
    private PointDTO labelPosition;
}
