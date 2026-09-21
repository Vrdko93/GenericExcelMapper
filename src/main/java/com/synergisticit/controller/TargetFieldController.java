package com.synergisticit.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.synergisticit.dto.TargetFieldRequest;
import com.synergisticit.entity.TargetField;
import com.synergisticit.service.TargetFieldService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/target-fields")
@RequiredArgsConstructor
public class TargetFieldController {

    private final TargetFieldService targetFieldService;

    @GetMapping
    public List<TargetField> getTargetFields(@RequestParam Long templateVersionId) {

        return targetFieldService.getTargetFields(templateVersionId);
    }

    @PostMapping
    public TargetField createTargetField(@RequestBody TargetFieldRequest request) {

        return targetFieldService.createTargetField(request);
    }
    
    @PutMapping("/{id}")
    public TargetField updateTargetField(@PathVariable Long id, @RequestBody TargetFieldRequest request) {
    	
        return targetFieldService.updateTargetField(id, request);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTargetField(@PathVariable Long id) {

        targetFieldService.deleteTargetField(id);

        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/move-up")
    public ResponseEntity<Void> moveUp(@PathVariable Long id) {

        targetFieldService.moveUp(id);

        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/move-down")
    public ResponseEntity<Void> moveDown(@PathVariable Long id) {

        targetFieldService.moveDown(id);

        return ResponseEntity.noContent().build();
    }
}
