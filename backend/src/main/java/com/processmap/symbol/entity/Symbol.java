package com.processmap.symbol.entity;

import com.processmap.project.entity.Domain;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "symbols")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Symbol {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "symbol_id", nullable = false, unique = true, length = 100)
    private String symbolId;

    @Column(name = "entity_class", nullable = false, length = 100)
    private String entityClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Domain domain;

    @Column(name = "svg_path", nullable = false, columnDefinition = "TEXT")
    private String svgPath;

    @Column(name = "default_tag_prefix", length = 20)
    private String defaultTagPrefix;

    @Column(columnDefinition = "TEXT")
    private String description;
}
