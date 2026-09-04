package com.synergisticit.controller;

import com.synergisticit.dto.AiMappingResponse;
import com.synergisticit.service.OpenAiMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/openai")
@RequiredArgsConstructor
public class OpenAiTestController {

    private final OpenAiMappingService openAiMappingService;

    @GetMapping("/mapping-test")
    public AiMappingResponse testMapping(@RequestParam Long sourceProfileId, @RequestParam Long templateVersionId) {

        return openAiMappingService.suggestMappings(sourceProfileId, templateVersionId);
    }
}
