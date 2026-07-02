package com.processmap.history.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.processmap.dto.DiagramResponseDTO;
import com.processmap.exception.AppException;
import com.processmap.exception.ErrorCode;
import com.processmap.history.entity.Diagram;
import com.processmap.history.mapper.DiagramMapper;
import com.processmap.history.repository.DiagramRepository;
import com.processmap.project.entity.Project;
import com.processmap.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiagramHistoryService {

    private final DiagramRepository diagramRepository;
    private final ProjectRepository projectRepository;
    private final DiagramMapper diagramMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public DiagramResponseDTO saveDiagram(UUID projectId, String promptText, JsonNode graphSnapshot) {
        log.info("Saving new diagram version for project: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found", HttpStatus.NOT_FOUND));

        Optional<Diagram> currentOpt = diagramRepository.findByProjectIdAndIsCurrentTrue(projectId);
        int version = 1;
        if (currentOpt.isPresent()) {
            Diagram current = currentOpt.get();
            current.setIsCurrent(false);
            diagramRepository.save(current);
            version = current.getVersion() + 1;
        }

        Diagram diagram = Diagram.builder()
                .project(project)
                .version(version)
                .promptText(promptText)
                .graphSnapshot(graphSnapshot)
                .isCurrent(true)
                .build();

        Diagram savedDiagram = diagramRepository.save(diagram);
        return diagramMapper.toResponseDTO(savedDiagram);
    }

    @Transactional(readOnly = true)
    public DiagramResponseDTO getDiagram(UUID id, UUID userId) {
        log.info("Fetching diagram ID: {}", id);
        Diagram diagram = diagramRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Diagram not found", HttpStatus.NOT_FOUND));

        validateOwnership(diagram.getProject(), userId);
        return diagramMapper.toResponseDTO(diagram);
    }

    @Transactional(readOnly = true)
    public List<DiagramResponseDTO> listVersions(UUID projectId, UUID userId) {
        log.info("Listing versions for project: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found", HttpStatus.NOT_FOUND));

        validateOwnership(project, userId);
        List<Diagram> diagrams = diagramRepository.findByProjectIdOrderByVersionDesc(projectId);
        return diagrams.stream().map(diagramMapper::toResponseDTO).toList();
    }

    @Transactional
    public DiagramResponseDTO updateDiagram(UUID id, UUID userId, JsonNode partialUpdate) {
        log.info("Merging partial update for diagram ID: {}", id);
        Diagram diagram = diagramRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Diagram not found", HttpStatus.NOT_FOUND));

        validateOwnership(diagram.getProject(), userId);

        // Perform deep merge of graph snapshot
        JsonNode mergedSnapshot = mergeSnapshot(diagram.getGraphSnapshot(), partialUpdate);

        // Mark old as not current
        diagram.setIsCurrent(false);
        diagramRepository.save(diagram);

        // Save new version
        Diagram newVersion = Diagram.builder()
                .project(diagram.getProject())
                .version(diagram.getVersion() + 1)
                .promptText(diagram.getPromptText())
                .promptMetadata(diagram.getPromptMetadata())
                .graphSnapshot(mergedSnapshot)
                .isCurrent(true)
                .build();

        Diagram saved = diagramRepository.save(newVersion);
        return diagramMapper.toResponseDTO(saved);
    }

    @Transactional
    public DiagramResponseDTO restoreVersion(UUID diagramId, int version, UUID userId) {
        log.info("Restoring project diagram version for diagram ID: {} to version: {}", diagramId, version);
        Diagram currentDiagram = diagramRepository.findById(diagramId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Diagram not found", HttpStatus.NOT_FOUND));

        validateOwnership(currentDiagram.getProject(), userId);

        List<Diagram> diagrams = diagramRepository.findByProjectIdOrderByVersionDesc(currentDiagram.getProject().getId());
        Diagram target = null;
        for (Diagram d : diagrams) {
            if (d.getVersion() == version) {
                d.setIsCurrent(true);
                target = d;
            } else {
                d.setIsCurrent(false);
            }
        }

        if (target == null) {
            throw new AppException(ErrorCode.NOT_FOUND, "Diagram version not found", HttpStatus.NOT_FOUND);
        }

        diagramRepository.saveAll(diagrams);
        return diagramMapper.toResponseDTO(target);
    }

    private void validateOwnership(Project project, UUID userId) {
        if (!project.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Access denied. You do not own this project", HttpStatus.FORBIDDEN);
        }
    }

    private JsonNode mergeSnapshot(JsonNode currentSnapshot, JsonNode partialUpdate) {
        try {
            JsonNode parsedSnapshot = currentSnapshot;
            if (currentSnapshot.isTextual()) {
                parsedSnapshot = objectMapper.readTree(currentSnapshot.asText());
            }
            ObjectNode root = (ObjectNode) parsedSnapshot.deepCopy();

            if (partialUpdate.has("nodes")) {
                JsonNode nodesNode = root.get("nodes");
                JsonNode updateNodes = partialUpdate.get("nodes");

                Map<String, ObjectNode> nodeMap = new HashMap<>();
                if (nodesNode.isArray()) {
                    for (JsonNode n : nodesNode) {
                        nodeMap.put(n.get("id").asText(), (ObjectNode) n);
                    }
                } else if (nodesNode.isObject()) {
                    nodesNode.fields().forEachRemaining(entry -> {
                        nodeMap.put(entry.getKey(), (ObjectNode) entry.getValue());
                    });
                }

                for (JsonNode un : updateNodes) {
                    String nodeId = un.get("id").asText();
                    if (nodeMap.containsKey(nodeId)) {
                        ObjectNode nodeToUpdate = nodeMap.get(nodeId);
                        if (un.has("x")) {
                            nodeToUpdate.set("x", un.get("x"));
                        }
                        if (un.has("y")) {
                            nodeToUpdate.set("y", un.get("y"));
                        }
                        if (un.has("label")) {
                            nodeToUpdate.set("label", un.get("label"));
                        }
                        if (un.has("state")) {
                            nodeToUpdate.set("state", un.get("state"));
                        }
                        if (un.has("locked")) {
                            nodeToUpdate.set("locked", un.get("locked"));
                        }
                        if (un.has("width")) {
                            nodeToUpdate.set("width", un.get("width"));
                        }
                        if (un.has("height")) {
                            nodeToUpdate.set("height", un.get("height"));
                        }
                    }
                }
            }

            if (partialUpdate.has("edges")) {
                JsonNode edgesNode = root.get("edges");
                JsonNode updateEdges = partialUpdate.get("edges");

                Map<String, ObjectNode> edgeMap = new HashMap<>();
                if (edgesNode.isArray()) {
                    for (JsonNode e : edgesNode) {
                        edgeMap.put(e.get("id").asText(), (ObjectNode) e);
                    }
                } else if (edgesNode.isObject()) {
                    edgesNode.fields().forEachRemaining(entry -> {
                        edgeMap.put(entry.getKey(), (ObjectNode) entry.getValue());
                    });
                }

                for (JsonNode ue : updateEdges) {
                    String edgeId = ue.get("id").asText();
                    if (edgeMap.containsKey(edgeId)) {
                        ObjectNode edgeToUpdate = edgeMap.get(edgeId);
                        if (ue.has("label")) {
                            edgeToUpdate.set("label", ue.get("label"));
                        }
                        if (ue.has("branchCondition")) {
                            edgeToUpdate.set("branchCondition", ue.get("branchCondition"));
                        }
                        if (ue.has("direction")) {
                            edgeToUpdate.set("direction", ue.get("direction"));
                        }
                        if (ue.has("medium")) {
                            edgeToUpdate.set("medium", ue.get("medium"));
                        }
                    }
                }
            }

            return root;
        } catch (Exception e) {
            log.error("Failed to merge graph snapshot", e);
            throw new AppException(ErrorCode.LAYOUT_FAILED, "Failed to merge graph snapshots: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
