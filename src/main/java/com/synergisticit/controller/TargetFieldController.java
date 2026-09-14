package com.synergisticit.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.synergisticit.entity.TargetField;
import com.synergisticit.repository.TargetFieldRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/target-fields")
@RequiredArgsConstructor
public class TargetFieldController {

    private final TargetFieldRepository targetFieldRepository;

    @GetMapping
    public List<TargetField> getTargetFields(@RequestParam Long templateVersionId) {

        return targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);
    }
}
