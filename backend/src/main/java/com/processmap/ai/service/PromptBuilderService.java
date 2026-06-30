package com.processmap.ai.service;

import com.processmap.exception.AppException;
import com.processmap.exception.ErrorCode;
import com.processmap.project.entity.Domain;
import com.processmap.symbol.entity.Symbol;
import com.processmap.symbol.repository.SymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptBuilderService {

    private final SymbolRepository symbolRepository;
    private static final String SYSTEM_PROMPT_PATH = "prompts/system-prompt.txt";
    private static final String SCHEMA_JSON = """
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "properties": {
        "nodes": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "id": { "type": "string" },
              "label": { "type": "string" },
              "entityClass": { "type": "string" },
              "confidence": { "type": "number", "minimum": 0.0, "maximum": 1.0 },
              "aliases": { "type": "array", "items": { "type": "string" } },
              "medium": { "type": "string" },
              "userConfirmRequired": { "type": "boolean" }
            },
            "required": ["id", "label", "entityClass", "confidence", "aliases", "medium", "userConfirmRequired"]
          }
        },
        "edges": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "id": { "type": "string" },
              "from": { "type": "string" },
              "to": { "type": "string" },
              "medium": { "type": "string" },
              "direction": { "type": "string", "enum": ["forward", "reverse", "bidirectional"] },
              "label": { "type": "string" },
              "branchCondition": { "type": "string" }
            },
            "required": ["id", "from", "to", "medium", "direction"]
          }
        },
        "branches": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "condition": { "type": "string" },
              "paths": { "type": "array", "items": { "type": "string" } }
            },
            "required": ["condition", "paths"]
          }
        },
        "domain": { "type": "string" }
      },
      "required": ["nodes", "edges", "branches", "domain"]
    }
    """;

    public String getPromptVersion() {
        return "1.0";
    }

    public String buildSystemPrompt(String domainName) {
        Domain domain;
        try {
            domain = Domain.valueOf(domainName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Invalid domain specified", HttpStatus.BAD_REQUEST);
        }

        try {
            // Load system prompt template
            String template = loadResource(SYSTEM_PROMPT_PATH);

            // Fetch symbol entity classes for list
            List<Symbol> symbols = symbolRepository.findByDomain(domain);
            String entityClassList = symbols.stream()
                    .map(Symbol::getEntityClass)
                    .distinct()
                    .collect(Collectors.joining(", "));

            // Load few-shot examples
            String examplesPath = "prompts/" + domainName.toLowerCase() + "-examples.json";
            String examplesJson = loadResource(examplesPath);

            // Interpolate variables
            return template
                    .replace("{domain}", domainName.toLowerCase())
                    .replace("{entityClassList}", entityClassList)
                    .replace("{fewShotExamples}", examplesJson)
                    .replace("{entityGraphJsonSchema}", SCHEMA_JSON);

        } catch (Exception e) {
            log.error("Failed to construct system prompt", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build system prompt: " + e.getMessage());
        }
    }

    private String loadResource(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
