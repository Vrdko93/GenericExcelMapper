package com.synergisticit.service;

import com.synergisticit.dto.MappingSuggestion;
import com.synergisticit.entity.SourceColumn;
import com.synergisticit.entity.TargetField;
import com.synergisticit.repository.SourceColumnRepository;
import com.synergisticit.repository.TargetFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MappingSuggestionService {

    private final SourceColumnRepository sourceColumnRepository;
    private final TargetFieldRepository targetFieldRepository;
    private static final Set<String> GENERIC_TOKENS = Set.of("name", "date", "number", "id", "code", "value", "type", "field");

    public List<MappingSuggestion> suggestMappings(Long sourceProfileId, Long templateVersionId) {

        List<SourceColumn> sourceColumns = sourceColumnRepository.findBySourceProfileId(sourceProfileId);
        List<TargetField> targetFields = targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);
        List<MappingSuggestion> suggestions = new ArrayList<>();

        for (SourceColumn sourceColumn : sourceColumns) {

            TargetField bestTarget = null;
            int bestConfidence = 0;

            for (TargetField targetField : targetFields) {

                int confidence = calculateConfidence(sourceColumn, targetField);

                if (confidence > bestConfidence) {
                    bestConfidence = confidence;
                    bestTarget = targetField;
                }
            }

            if (bestTarget != null && bestConfidence > 0) {

                suggestions.add(
                        new MappingSuggestion(
                                sourceColumn.getId(),
                                sourceColumn.getColumnName(),
                                sourceColumn.getDataType(),
                                bestTarget.getId(),
                                bestTarget.getName(),
                                bestTarget.getKey(),
                                bestTarget.getDataType(),
                                bestConfidence
                        )
                );
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
