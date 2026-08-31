package com.synergisticit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(nullable = false)
    private String transformation;
}
