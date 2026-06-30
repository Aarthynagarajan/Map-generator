package com.processmap.symbol.entity;

import com.processmap.project.entity.Domain;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "synonyms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Synonym {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String term;

    @Column(name = "entity_class", nullable = false, length = 100)
    private String entityClass;

    @Column(nullable = false, length = 20)
    private Domain domain;
}
