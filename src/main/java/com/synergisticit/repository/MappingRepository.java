package com.synergisticit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.synergisticit.entity.Mapping;

public interface MappingRepository extends JpaRepository<Mapping, Long> {

    boolean existsBySourceColumnId(Long sourceColumnId);

    boolean existsByTargetFieldId(Long targetFieldId);
}
