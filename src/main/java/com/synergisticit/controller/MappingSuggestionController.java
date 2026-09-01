package com.synergisticit.controller;

import com.synergisticit.dto.MappingSuggestion;
import com.synergisticit.service.MappingSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mapping-suggestions")
@RequiredArgsConstructor
public class MappingSuggestionController {

    private final MappingSuggestionService mappingSuggestionService;

    @GetMapping
    public List<MappingSuggestion> suggestMappings(@RequestParam Long sourceProfileId, @RequestParam Long templateVersionId) {

        return mappingSuggestionService.suggestMappings(sourceProfileId, templateVersionId);
    }
}
