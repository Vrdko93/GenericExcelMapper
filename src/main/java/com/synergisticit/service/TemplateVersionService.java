package com.synergisticit.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.synergisticit.entity.TemplateVersion;
import com.synergisticit.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateVersionService {

    private final TemplateVersionRepository templateVersionRepository;

    @Transactional
    public TemplateVersion activateVersion(Long versionId) {

        TemplateVersion version = templateVersionRepository.findById(versionId)
                        .orElseThrow(() -> new IllegalArgumentException("Template version not found: " + versionId));

        Long templateId = version.getTemplate().getId();
        List<TemplateVersion> activeVersions = templateVersionRepository.findByTemplateIdAndActiveTrue(templateId);

        for (TemplateVersion activeVersion : activeVersions) {

            if (!activeVersion.getId().equals(versionId)) {
                activeVersion.setActive(false);
            }
        }

        templateVersionRepository.saveAll(activeVersions);
        version.setActive(true);

        return templateVersionRepository.save(version);
    }
}
