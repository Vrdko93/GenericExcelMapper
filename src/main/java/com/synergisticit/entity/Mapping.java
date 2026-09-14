package com.synergisticit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_column_id", nullable = false)
    private SourceColumn sourceColumn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_field_id", nullable = false)
    private TargetField targetField;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransformationType transformation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_method", nullable = false)
    private MappingMethod mappingMethod;

    @Column(name = "ai_confidence")
    private Integer aiConfidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_recommended_target_field_id")
    private TargetField aiRecommendedTargetField;
    
    @Column(name = "transformation_config", length = 1000)
    private String transformationConfig;
}
