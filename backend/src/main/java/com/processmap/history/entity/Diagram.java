package com.processmap.history.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.processmap.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "diagrams")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Diagram {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "prompt_text", nullable = false, columnDefinition = "TEXT")
    private String promptText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prompt_metadata", columnDefinition = "jsonb")
    private JsonNode promptMetadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "graph_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode graphSnapshot;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
