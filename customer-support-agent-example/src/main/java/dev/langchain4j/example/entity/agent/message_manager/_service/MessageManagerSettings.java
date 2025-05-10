package dev.langchain4j.example.entity.agent.message_manager._service;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MessageManagerSettings {
    @Builder.Default
    private int maxInputTokens = 128000;
    @Builder.Default
    private int estimatedCharactersPerToken = 3;
    @Builder.Default
    private int imageTokens = 800;
    @Builder.Default
    private List<String> includeAttributes = new ArrayList<>();
    private String messageContext;
    private Map<String, String> sensitiveData;
    private List<String> availableFilePaths;
}
