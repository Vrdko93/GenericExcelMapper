package com.synergisticit.service;

import com.synergisticit.dto.AiMappingResponse;
import com.synergisticit.entity.SourceColumn;
import com.synergisticit.entity.TargetField;
import com.synergisticit.repository.SourceColumnRepository;
import com.synergisticit.repository.TargetFieldRepository;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAiMappingService {

    private final OpenAIClient openAIClient;
    private final SourceColumnRepository sourceColumnRepository;
    private final TargetFieldRepository targetFieldRepository;

    public AiMappingResponse suggestMappings(String prompt) {

        StructuredChatCompletionCreateParams<AiMappingResponse> params =
                ChatCompletionCreateParams.builder()
                        .model(ChatModel.GPT_5_2)
                        .maxCompletionTokens(2048)
                        .responseFormat(AiMappingResponse.class)
                        .addUserMessage(prompt)
                        .build();

        return openAIClient.chat()
                .completions()
                .create(params)
                .choices()
                .stream()
                .flatMap(choice -> choice.message().content().stream())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("OpenAI returned no structured mapping response"));
    }
    
    public AiMappingResponse suggestMappings(Long sourceProfileId, Long templateVersionId) {

        List<SourceColumn> sourceColumns = sourceColumnRepository.findBySourceProfileId(sourceProfileId);
        List<TargetField> targetFields = targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);

        String prompt = buildPrompt(sourceColumns, targetFields);

        return suggestMappings(prompt);
    }
    
    public AiMappingResponse suggestMappings(List<SourceColumn> sourceColumns, List<TargetField> targetFields) {

        String prompt = buildPrompt(sourceColumns, targetFields);

        return suggestMappings(prompt);
    }
    
    private String buildPrompt(List<SourceColumn> sourceColumns, List<TargetField> targetFields) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are an Excel-to-database field mapping assistant.

                Your task is to match source Excel columns to target template fields.

                Match fields based primarily on business meaning and column names.
                Use the sample values and data types as additional evidence.

                Rules:
                - Match based on business meaning, not just data type.
                - Do not match fields merely because their data types are the same.
                - Use sample values to understand what the source column represents.
                - Do not invent IDs.
                - A source column can map to at most one target field.
                - A target field can be used by at most one source column.
                - Return strong and reasonably clear matches.
                - Confidence must be between 0 and 100.
                - Only return matches with confidence of 70 or higher.
                - If a source column clearly represents a target field, return the mapping.
                - Return the sourceColumnId and targetFieldId exactly as provided.

                SOURCE COLUMNS:
                """);

        for (SourceColumn sourceColumn : sourceColumns) {

            prompt.append(String.format(
                    "- ID: %d, Name: %s, Data Type: %s, Sample Values: %s%n",
                    sourceColumn.getId(),
                    sourceColumn.getColumnName(),
                    sourceColumn.getDataType(),
                    sourceColumn.getSampleValues()
            ));
        }

        prompt.append("""
                
                TARGET FIELDS:
                """);

        for (TargetField targetField : targetFields) {

            prompt.append(String.format(
                    "- ID: %d, Name: %s, Key: %s, Data Type: %s, Mandatory: %s, Description: %s, Default Value: %s%n",
                    targetField.getId(),
                    targetField.getName(),
                    targetField.getKey(),
                    targetField.getDataType(),
                    targetField.getMandatory(),
                    targetField.getDescription(),
                    targetField.getDefaultValue()
            ));
        }

        prompt.append("""
                Analyze all source columns against all target fields.
                Return every strong mapping you can identify.
                """);

        return prompt.toString();
    }
}
