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
@Table(name = "mapping_profile_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MappingProfileEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mapping_profile_id", nullable = false)
    private MappingProfile mappingProfile;

    @Column(name = "source_column_name", nullable = false)
    private String sourceColumnName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_field_id", nullable = false)
    private TargetField targetField;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransformationType transformation;
}