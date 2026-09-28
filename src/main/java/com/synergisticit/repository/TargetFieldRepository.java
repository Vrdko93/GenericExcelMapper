package com.synergisticit.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.synergisticit.entity.TargetField;

public interface TargetFieldRepository extends JpaRepository<TargetField, Long> {

	List<TargetField> findByTemplateVersionIdOrderBySequence(Long templateVersionId);

	Optional<TargetField> findTopByTemplateVersionIdOrderBySequenceDesc(Long templateVersionId);
}