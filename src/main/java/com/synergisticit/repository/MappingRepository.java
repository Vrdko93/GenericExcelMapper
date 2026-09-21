package com.synergisticit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.synergisticit.entity.Mapping;

public interface MappingRepository extends JpaRepository<Mapping, Long> {

    boolean existsBySourceColumnId(Long sourceColumnId);

    boolean existsByTargetFieldId(Long targetFieldId);
    
    List<Mapping> findBySourceColumnSourceProfileId(Long sourceProfileId);
    
    boolean existsByAiRecommendedTargetFieldId(Long targetFieldId);
}
