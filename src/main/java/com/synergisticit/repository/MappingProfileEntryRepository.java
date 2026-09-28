package com.synergisticit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.synergisticit.entity.MappingProfileEntry;

public interface MappingProfileEntryRepository extends JpaRepository<MappingProfileEntry, Long> {

	List<MappingProfileEntry> findByMappingProfileId(Long mappingProfileId);

    boolean existsByTargetFieldId(Long targetFieldId);
}