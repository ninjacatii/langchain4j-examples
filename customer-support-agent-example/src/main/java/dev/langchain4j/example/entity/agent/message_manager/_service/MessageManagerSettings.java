package dev.langchain4j.example.entity.agent.message_manager._service;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MessageManagerSettings {
    private int maxInputTokens = 128000;
    private int estimatedCharactersPerToken = 3;
    private int imageTokens = 800;
    private List<String> includeAttributes = new ArrayList<>();
    private String messageContext;
    private Map<String, String> sensitiveData;
    private List<String> availableFilePaths;
}
