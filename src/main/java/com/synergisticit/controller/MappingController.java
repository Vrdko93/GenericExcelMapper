package com.synergisticit.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergisticit.dto.MappingOverrideRequest;
import com.synergisticit.dto.MappingRequest;
import com.synergisticit.entity.Mapping;
import com.synergisticit.service.MappingService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mappings")
@RequiredArgsConstructor
public class MappingController {

    private final MappingService mappingService;

    @PostMapping
    public Mapping createMapping(@RequestBody MappingRequest request) {

        return mappingService.createMapping(request);
    }
    
    @PutMapping("/{id}")
    public Mapping overrideMapping(@PathVariable Long id, @RequestBody MappingOverrideRequest request) {

        return mappingService.overrideMapping(id, request);
    }
}
