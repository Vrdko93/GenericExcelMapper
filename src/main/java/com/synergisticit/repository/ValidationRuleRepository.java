package com.synergisticit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.synergisticit.entity.ValidationRule;

public interface ValidationRuleRepository extends JpaRepository<ValidationRule, Long> {

	List<ValidationRule> findByTargetFieldIdOrderBySequence(Long targetFieldId);
}
