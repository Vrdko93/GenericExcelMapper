package com.synergisticit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.synergisticit.entity.MappingProfile;

public interface MappingProfileRepository extends JpaRepository<MappingProfile, Long> {

	List<MappingProfile> findByTemplateVersionIdAndActiveTrue(Long templateVersionId);
}