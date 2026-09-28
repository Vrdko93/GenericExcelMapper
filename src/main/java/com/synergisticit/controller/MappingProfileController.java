package com.synergisticit.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synergisticit.dto.MappingProfileOption;
import com.synergisticit.dto.MappingProfileRequest;
import com.synergisticit.dto.MappingProfileResponse;
import com.synergisticit.entity.MappingProfile;
import com.synergisticit.repository.MappingProfileRepository;
import com.synergisticit.service.MappingProfileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mapping-profiles")
@RequiredArgsConstructor
public class MappingProfileController {

    private final MappingProfileService mappingProfileService;
    private final MappingProfileRepository mappingProfileRepository;

    @PostMapping
    public MappingProfileResponse createProfile(@RequestBody MappingProfileRequest request) {

        MappingProfile profile = mappingProfileService.createProfile(request);

        return new MappingProfileResponse(profile.getId(), profile.getName(), profile.getTemplateVersion().getId(),
                                          profile.getTemplateVersion().getVersion(), profile.getActive());
    }
    
    @GetMapping
    public List<MappingProfileOption> getProfiles(@RequestParam Long templateVersionId) {

        return mappingProfileRepository
                .findByTemplateVersionIdAndActiveTrue(templateVersionId)
                .stream()
                .map(profile -> new MappingProfileOption(profile.getId(), profile.getName()))
                .toList();
    }
}