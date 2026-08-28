package com.synergisticit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.synergisticit.entity.Template;

public interface TemplateRepository extends JpaRepository<Template, Long> {
}
