package com.processmap.layout.service;

import com.processmap.ai.model.BranchCondition;
import com.processmap.ai.model.EntityEdge;
import com.processmap.dto.GenerationConstraints;
import com.processmap.dto.PointDTO;
import com.processmap.dto.SymbolGraph;
import com.processmap.dto.SymbolNode;
import com.processmap.layout.model.LayoutEdge;
import com.processmap.layout.model.LayoutGraph;
import com.processmap.layout.model.LayoutNode;
import com.processmap.layout.preset.LayoutPresetConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class LayoutService {

    public LayoutGraph computeLayout(SymbolGraph symbolGraph, GenerationConstraints constraints) {
        log.info("Computing layout for domain: {}", symbolGraph.domain());

        String presetName = symbolGraph.domain();
        if ("industrial".equalsIgnoreCase(presetName)) {
            presetName = "process-flow";
        } else if ("electrical".equalsIgnoreCase(presetName)) {
            presetName = "electrical-bus";
        } else if ("hydraulic".equalsIgnoreCase(presetName)) {
            presetName = "hydraulic-loop";
        }

        LayoutPresetConfig preset = LayoutPresetConfig.getPreset(presetName);
        boolean isLR = "LR".equalsIgnoreCase(preset.direction());

        List<SymbolNode> allNodes = symbolGraph.nodes();
        List<EntityEdge> allEdges = symbolGraph.edges();

        if (allNodes.isEmpty()) {
            return new LayoutGraph(new ArrayList<>(), new ArrayList<>(), symbolGraph.domain());
        }

        // Build adjacency and incoming count maps
        Map<String, List<String>> adj = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, SymbolNode> nodeLookup = new HashMap<>();

        for (SymbolNode node : allNodes) {
            nodeLookup.put(node.id(), node);
            inDegree.put(node.id(), 0);
            adj.put(node.id(), new ArrayList<>());
        }

        for (EntityEdge edge : allEdges) {
            adj.computeIfAbsent(edge.from(), k -> new ArrayList<>()).add(edge.to());
            inDegree.put(edge.to(), inDegree.getOrDefault(edge.to(), 0) + 1);
        }

        // Level assignment using BFS
        Map<String, Integer> levels = new HashMap<>();
        Queue<String> queue = new LinkedList<>();

        // Find sources (in-degree == 0)
        for (SymbolNode node : allNodes) {
            if (inDegree.get(node.id()) == 0) {
                levels.put(node.id(), 0);
                queue.add(node.id());
            }
        }

        // Fallback if graph is a cycle without standard inlet
        if (queue.isEmpty()) {
            String firstId = allNodes.get(0).id();
            levels.put(firstId, 0);
            queue.add(firstId);
        }

        while (!queue.isEmpty()) {
            String currId = queue.poll();
            int currLevel = levels.get(currId);

            List<String> children = adj.get(currId);
            if (children != null) {
                for (String childId : children) {
                    if (!levels.containsKey(childId)) {
                        levels.put(childId, currLevel + 1);
                        queue.add(childId);
                    } else {
                        // Keep max level path to avoid backward overlaps
                        levels.put(childId, Math.max(levels.get(childId), currLevel + 1));
                    }
                }
            }
        }

        // Group nodes by level index
        Map<Integer, List<SymbolNode>> nodesByLevel = new TreeMap<>();
        for (SymbolNode node : allNodes) {
            int lvl = levels.getOrDefault(node.id(), 0);
            nodesByLevel.computeIfAbsent(lvl, k -> new ArrayList<>()).add(node);
        }

        // Compute coordinate mappings
        List<LayoutNode> layoutNodes = new ArrayList<>();
        Map<String, LayoutNode> layoutNodeMap = new HashMap<>();

        int rankSpacing = isLR ? 200 : 150;
        int nodeSpacing = isLR ? 100 : 120;

        for (Map.Entry<Integer, List<SymbolNode>> entry : nodesByLevel.entrySet()) {
            int lvl = entry.getKey();
            List<SymbolNode> levelNodes = entry.getValue();

            for (int i = 0; i < levelNodes.size(); i++) {
                SymbolNode sn = levelNodes.get(i);
                double x, y;

                double nodeWidth = 80;
                double nodeHeight = 60;
                if ("STORAGE_TANK".equalsIgnoreCase(sn.entityClass())) {
                    nodeWidth = 120;
                    nodeHeight = 100;
                }

                if (isLR) {
                    x = 100 + lvl * rankSpacing;
                    y = 100 + i * (nodeHeight + nodeSpacing);
                } else {
                    x = 100 + i * (nodeWidth + nodeSpacing);
                    y = 100 + lvl * rankSpacing;
                }

                LayoutNode ln = LayoutNode.builder()
                        .node(sn)
                        .x(x)
                        .y(y)
                        .width(nodeWidth)
                        .height(nodeHeight)
                        .orientation(preset.alignment())
                        .build();

                layoutNodes.add(ln);
                layoutNodeMap.put(sn.id(), ln);
            }
        }

        // Route edges
        List<LayoutEdge> layoutEdges = new ArrayList<>();
        for (EntityEdge edge : allEdges) {
            LayoutNode fromNode = layoutNodeMap.get(edge.from());
            LayoutNode toNode = layoutNodeMap.get(edge.to());

            List<PointDTO> route = new ArrayList<>();
            PointDTO startPoint, endPoint;

            if (fromNode != null && toNode != null) {
                if (isLR) {
                    startPoint = new PointDTO(fromNode.getX() + fromNode.getWidth(), fromNode.getY() + fromNode.getHeight() / 2);
                    endPoint = new PointDTO(toNode.getX(), toNode.getY() + toNode.getHeight() / 2);
                } else {
                    startPoint = new PointDTO(fromNode.getX() + fromNode.getWidth() / 2, fromNode.getY() + fromNode.getHeight());
                    endPoint = new PointDTO(toNode.getX() + toNode.getWidth() / 2, toNode.getY());
                }
                route.add(startPoint);
                // Add midpoint bend for orthogonal look
                double midX = (startPoint.x() + endPoint.x()) / 2;
                double midY = (startPoint.y() + endPoint.y()) / 2;
                if (isLR) {
                    route.add(new PointDTO(midX, startPoint.y()));
                    route.add(new PointDTO(midX, endPoint.y()));
                } else {
                    route.add(new PointDTO(startPoint.x(), midY));
                    route.add(new PointDTO(endPoint.x(), midY));
                }
                route.add(endPoint);
            } else {
                startPoint = new PointDTO(0, 0);
                endPoint = new PointDTO(0, 0);
                route.add(startPoint);
                route.add(endPoint);
            }

            PointDTO labelPos = new PointDTO((startPoint.x() + endPoint.x()) / 2, (startPoint.y() + endPoint.y()) / 2);

            LayoutEdge le = LayoutEdge.builder()
                    .edge(edge)
                    .routePoints(route)
                    .labelPosition(labelPos)
                    .build();
            layoutEdges.add(le);
        }

        return new LayoutGraph(layoutNodes, layoutEdges, symbolGraph.domain());
    }
}
