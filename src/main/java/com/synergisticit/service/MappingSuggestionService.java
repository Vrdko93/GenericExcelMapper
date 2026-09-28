package com.synergisticit.service;

import com.synergisticit.dto.AiMappingResponse;
import com.synergisticit.dto.MappingSuggestion;
import com.synergisticit.entity.MappingMethod;
import com.synergisticit.entity.MappingProfile;
import com.synergisticit.entity.MappingProfileEntry;
import com.synergisticit.entity.SourceColumn;
import com.synergisticit.entity.TargetField;
import com.synergisticit.repository.MappingProfileEntryRepository;
import com.synergisticit.repository.MappingProfileRepository;
import com.synergisticit.repository.SourceColumnRepository;
import com.synergisticit.repository.TargetFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MappingSuggestionService {

    private final SourceColumnRepository sourceColumnRepository;
    private final TargetFieldRepository targetFieldRepository;
    private final OpenAiMappingService openAiMappingService;
    private static final Set<String> GENERIC_TOKENS = Set.of("name", "date", "number", "id", "code", "value", "type", "field");
    private final MappingProfileRepository mappingProfileRepository;
    private final MappingProfileEntryRepository mappingProfileEntryRepository;
       
    public List<MappingSuggestion> suggestMappings(Long sourceProfileId, Long templateVersionId, Long mappingProfileId) {

        List<SourceColumn> sourceColumns = sourceColumnRepository.findBySourceProfileId(sourceProfileId);
        List<TargetField> targetFields = targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);

        List<MappingSuggestion> suggestions = new ArrayList<>();

        Set<Long> matchedSourceIds = new HashSet<>();
        Set<Long> matchedTargetIds = new HashSet<>();
        
     // STEP 1 - Reusable Mapping Profile

        if (mappingProfileId != null) {

            MappingProfile mappingProfile = mappingProfileRepository.findById(mappingProfileId)
                            .orElseThrow(() -> new IllegalArgumentException("Mapping profile not found: " + mappingProfileId));

            if (!mappingProfile.getActive()) {
                throw new IllegalArgumentException("Mapping profile is not active: " + mappingProfileId);
            }

            if (!mappingProfile.getTemplateVersion().getId().equals(templateVersionId)) {

                throw new IllegalArgumentException("Mapping profile does not belong to template version: " + templateVersionId);
            }

            List<MappingProfileEntry> profileEntries = mappingProfileEntryRepository.findByMappingProfileId(mappingProfileId);

            for (SourceColumn sourceColumn : sourceColumns) {

                MappingProfileEntry matchingEntry = profileEntries.stream()
                                .filter(entry -> normalize(entry.getSourceColumnName())
                                .equals(normalize(sourceColumn.getColumnName())))
                                .findFirst()
                                .orElse(null);

                if (matchingEntry == null) {
                    continue;
                }

                TargetField targetField = matchingEntry.getTargetField();

                if (matchedTargetIds.contains(targetField.getId())) {
                    continue;
                }

                suggestions.add(new MappingSuggestion(
                                sourceColumn.getId(),
                                sourceColumn.getColumnName(),
                                sourceColumn.getDataType(),
                                targetField.getId(),
                                targetField.getName(),
                                targetField.getKey(),
                                targetField.getDataType(),
                                100,
                                MappingMethod.PROFILE
                        )
                );

                matchedSourceIds.add(sourceColumn.getId());
                matchedTargetIds.add(targetField.getId());
            }
        }

        // --------------------------------------------------
        // STEP 1: Deterministic matching
        // --------------------------------------------------

        for (SourceColumn sourceColumn : sourceColumns) {
        	
            if (matchedSourceIds.contains(sourceColumn.getId())) {
                continue;
            }

            TargetField bestTarget = null;
            int bestConfidence = 0;

            for (TargetField targetField : targetFields) {

                // Don't reuse a target field
                if (matchedTargetIds.contains(targetField.getId())) {
                    continue;
                }

                int confidence = calculateConfidence(sourceColumn, targetField);

                if (confidence > bestConfidence) {
                	
                    bestConfidence = confidence;
                    bestTarget = targetField;
                }
            }

            if (bestTarget != null && bestConfidence >= 70) {

            	suggestions.add(new MappingSuggestion(
            	        sourceColumn.getId(),
            	        sourceColumn.getColumnName(),
            	        sourceColumn.getDataType(),
            	        bestTarget.getId(),
            	        bestTarget.getName(),
            	        bestTarget.getKey(),
            	        bestTarget.getDataType(),
            	        bestConfidence,
            	        MappingMethod.DETERMINISTIC
            	));

                matchedSourceIds.add(sourceColumn.getId());
                matchedTargetIds.add(bestTarget.getId());
            }
        }

        // --------------------------------------------------
        // STEP 2: Find unresolved source columns
        // --------------------------------------------------

        List<SourceColumn> unresolvedSourceColumns = sourceColumns.stream()
                        .filter(sourceColumn -> !matchedSourceIds.contains(sourceColumn.getId()))
                        .toList();

        // --------------------------------------------------
        // STEP 3: Find unused target fields
        // --------------------------------------------------

        List<TargetField> availableTargetFields =
                targetFields.stream()
                        .filter(targetField -> !matchedTargetIds.contains(targetField.getId()))
                        .toList();

        // --------------------------------------------------
        // STEP 4: Ask AI to map only unresolved fields
        // --------------------------------------------------

        if (!unresolvedSourceColumns.isEmpty() && !availableTargetFields.isEmpty()) {

            AiMappingResponse aiResponse = openAiMappingService.suggestMappings(unresolvedSourceColumns, availableTargetFields);

            // --------------------------------------------------
            // STEP 5: Convert AI results to MappingSuggestion
            // --------------------------------------------------

            for (AiMappingResponse.AiMapping aiMapping : aiResponse.getMappings()) {

                if (aiMapping.getConfidence() < 70) {
                    continue;
                }

                SourceColumn sourceColumn =
                        unresolvedSourceColumns.stream()
                                .filter(column -> column.getId().equals(aiMapping.getSourceColumnId()))
                                .findFirst()
                                .orElse(null);

                TargetField targetField =
                        availableTargetFields.stream()
                                .filter(field -> field.getId().equals(aiMapping.getTargetFieldId()))
                                .findFirst()
                                .orElse(null);

                if (sourceColumn == null || targetField == null) {
                    continue;
                }

                if (matchedSourceIds.contains(sourceColumn.getId()) || matchedTargetIds.contains(targetField.getId())) {
                    continue;
                }

                suggestions.add(new MappingSuggestion(
                        sourceColumn.getId(),
                        sourceColumn.getColumnName(),
                        sourceColumn.getDataType(),
                        targetField.getId(),
                        targetField.getName(),
                        targetField.getKey(),
                        targetField.getDataType(),
                        aiMapping.getConfidence(),
                        MappingMethod.AI
                ));

                matchedSourceIds.add(sourceColumn.getId());
                matchedTargetIds.add(targetField.getId());
            }
        }

        return suggestions;
    }

    private int calculateConfidence(SourceColumn sourceColumn, TargetField targetField) {

        String sourceName = normalize(sourceColumn.getColumnName());
        String targetName = normalize(targetField.getName());
        String targetKey = normalize(targetField.getKey());

        if (sourceName.isEmpty()) {
            return 0;
        }

        if (sourceName.equals(targetName)) {
            return 100;
        }

        if (sourceName.equals(targetKey)) {
            return 100;
        }

        int nameSimilarity = calculateNameSimilarity(sourceName, targetName, targetKey);

        if (nameSimilarity < 50) {
            return 0;
        }

        String sourceType = normalize(sourceColumn.getDataType());
        String targetType = normalize(targetField.getDataType());

        if (sourceType.equals(targetType)) {
            nameSimilarity += 10;
        }

        return Math.min(nameSimilarity, 100);
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
    
    private int calculateNameSimilarity(String sourceName, String targetName, String targetKey) {

        if (sourceName.equals(targetName) || sourceName.equals(targetKey)) {
            return 90;
        }

        Set<String> sourceTokens = tokenize(sourceName);
        Set<String> targetNameTokens = tokenize(targetName);
        Set<String> targetKeyTokens = tokenize(targetKey);

        int nameScore = tokenScore(sourceTokens, targetNameTokens);
        int keyScore = tokenScore(sourceTokens, targetKeyTokens);

        return Math.max(nameScore, keyScore);
    }
    
    private Set<String> tokenize(String value) {

        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(value.replaceAll("([a-z])([A-Z])", "$1 $2")
                                .split("[^a-zA-Z0-9]+"))
				                .map(String::toLowerCase)
				                .filter(token -> !token.isBlank())
				                .filter(token -> !GENERIC_TOKENS.contains(token))
				                .collect(Collectors.toSet());
    }
    
    private int tokenScore(Set<String> sourceTokens, Set<String> targetTokens) {

        if (sourceTokens.isEmpty() || targetTokens.isEmpty()) {
            return 0;
        }

        long matchingTokens = sourceTokens.stream().filter(targetTokens::contains).count();
        double sourceCoverage = (double) matchingTokens / sourceTokens.size();

        double targetCoverage = (double) matchingTokens / targetTokens.size();
        double score = Math.max(sourceCoverage, targetCoverage);

        if (score == 1.0) {
            return 90;
        }

        if (score >= 0.5) {
            return 60;
        }

        return 0;
    }
}