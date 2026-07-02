package com.processmap.history.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmap.dto.*;
import com.processmap.history.entity.Diagram;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class DiagramMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "domain", expression = "java(diagram.getProject().getDomain().name())")
    @Mapping(target = "nodes", expression = "java(mapNodes(diagram.getGraphSnapshot()))")
    @Mapping(target = "edges", expression = "java(mapEdges(diagram.getGraphSnapshot()))")
    @Mapping(target = "generationMetadata", expression = "java(mapMetadata(diagram.getPromptMetadata()))")
    @Mapping(
    target = "graphSnapshot",
    expression = "java(diagram.getGraphSnapshot())"
)
    public abstract DiagramResponseDTO toResponseDTO(Diagram diagram);

    protected List<LayoutNodeDTO> mapNodes(JsonNode graphSnapshot) {
        if (graphSnapshot == null || !graphSnapshot.has("nodes")) {
            return Collections.emptyList();
        }
        try {
            JsonNode nodesNode = graphSnapshot.get("nodes");
            if (nodesNode.isArray()) {
                return objectMapper.convertValue(nodesNode, new TypeReference<List<LayoutNodeDTO>>() {});
            } else if (nodesNode.isObject()) {
                java.util.List<LayoutNodeDTO> list = new java.util.ArrayList<>();
                nodesNode.fields().forEachRemaining(entry -> {
                    try {
                        list.add(objectMapper.treeToValue(entry.getValue(), LayoutNodeDTO.class));
                    } catch (Exception ignored) {}
                });
                return list;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    protected List<LayoutEdgeDTO> mapEdges(JsonNode graphSnapshot) {
        if (graphSnapshot == null || !graphSnapshot.has("edges")) {
            return Collections.emptyList();
        }
        try {
            JsonNode edgesNode = graphSnapshot.get("edges");
            if (edgesNode.isArray()) {
                return objectMapper.convertValue(edgesNode, new TypeReference<List<LayoutEdgeDTO>>() {});
            } else if (edgesNode.isObject()) {
                java.util.List<LayoutEdgeDTO> list = new java.util.ArrayList<>();
                edgesNode.fields().forEachRemaining(entry -> {
                    try {
                        list.add(objectMapper.treeToValue(entry.getValue(), LayoutEdgeDTO.class));
                    } catch (Exception ignored) {}
                });
                return list;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    protected GenerationMetadataDTO mapMetadata(JsonNode promptMetadata) {
        if (promptMetadata == null) {
            return null;
        }
        try {
            return objectMapper.treeToValue(promptMetadata, GenerationMetadataDTO.class);
        } catch (Exception e) {
            return null;
        }
    }
}
