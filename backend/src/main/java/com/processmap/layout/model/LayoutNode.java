package com.processmap.layout.model;

import com.processmap.dto.SymbolNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayoutNode {
    private SymbolNode node;
    private double x;
    private double y;
    private double width;
    private double height;
    private String orientation;
}
