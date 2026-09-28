package com.synergisticit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.synergisticit.entity.TemplateVersion;

public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, Long> {

	List<TemplateVersion> findByTemplateId(Long templateId);
	
	List<TemplateVersion> findByTemplateIdOrderByIdDesc(Long templateId);
	
	List<TemplateVersion> findByTemplateIdAndActiveTrue(Long templateId);
}