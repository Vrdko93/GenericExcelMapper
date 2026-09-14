package com.synergisticit.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.synergisticit.dto.MappingProfileRequest;
import com.synergisticit.entity.Mapping;
import com.synergisticit.entity.MappingProfile;
import com.synergisticit.entity.MappingProfileEntry;
import com.synergisticit.entity.TemplateVersion;
import com.synergisticit.repository.MappingProfileEntryRepository;
import com.synergisticit.repository.MappingProfileRepository;
import com.synergisticit.repository.MappingRepository;
import com.synergisticit.repository.TemplateVersionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MappingProfileService {

    private final MappingProfileRepository mappingProfileRepository;
    private final MappingProfileEntryRepository entryRepository;
    private final MappingRepository mappingRepository;
    private final TemplateVersionRepository templateVersionRepository;

    @Transactional
    public MappingProfile createProfile(MappingProfileRequest request) {

        TemplateVersion templateVersion = templateVersionRepository
                        .findById(request.getTemplateVersionId())
                        .orElseThrow(() -> new IllegalArgumentException("Template version not found: "));

        List<Mapping> mappings = mappingRepository.findBySourceColumnSourceProfileId(request.getSourceProfileId());

        if (mappings.isEmpty()) {
            throw new IllegalArgumentException("No mappings found for source profile: "+ request.getSourceProfileId());
        }

        MappingProfile profile = new MappingProfile();

        profile.setName(request.getName());
        profile.setTemplateVersion(templateVersion);
        profile.setActive(true);

        MappingProfile savedProfile = mappingProfileRepository.save(profile);

        for (Mapping mapping : mappings) {

            MappingProfileEntry entry = new MappingProfileEntry();

            entry.setMappingProfile(savedProfile);
            entry.setSourceColumnName(mapping.getSourceColumn().getColumnName());

            entry.setTargetField(mapping.getTargetField());
            entry.setTransformation(mapping.getTransformation());

            entryRepository.save(entry);
        }

        return savedProfile;
    }
}
