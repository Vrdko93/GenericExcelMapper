package com.synergisticit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.synergisticit.entity.SourceColumn;

public interface SourceColumnRepository extends JpaRepository<SourceColumn, Long> {

	List<SourceColumn> findBySourceProfileId(Long sourceProfileId);
}
