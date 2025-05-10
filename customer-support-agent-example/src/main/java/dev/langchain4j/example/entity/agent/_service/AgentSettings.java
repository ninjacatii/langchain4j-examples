package dev.langchain4j.example.entity.agent._service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AgentSettings {
    @Builder.Default
    private boolean useVision = true;
    @Builder.Default
    private boolean useVisionForPlanner = false;
    private String saveConversationPath;
    @Builder.Default
    private String saveConversationPathEncoding = "utf-8";
    @Builder.Default
    private int maxFailures = 3;
    @Builder.Default
    private int retryDelay = 10;
    private String overrideSystemMessage;
    private String extendSystemMessage;
    @Builder.Default
    private int maxInputTokens = 128000;
    @Builder.Default
    private boolean validateOutput = false;
    private String messageContext;
    @Builder.Default
    private boolean generateGif = false;
    private String generateGifPath;
    private List<String> availableFilePaths;
    @Builder.Default
    private List<String> includeAttributes = Arrays.asList(
            "title", "type", "name", "role", "aria-label",
            "placeholder", "value", "alt", "aria-expanded", "data-date-format"
    );
    @Builder.Default
    private int maxActionsPerStep = 10;
    @Builder.Default
    private String toolCallingMethod = "auto";
    private ChatLanguageModel pageExtractionLlm;
    private ChatLanguageModel plannerLlm;
    @Builder.Default
    private int plannerInterval = 1;
    @Builder.Default
    private boolean isPlannerReasoning = false;
    @Builder.Default
    private boolean enableMemory = false;
    @Builder.Default
    private int memoryInterval = 10;
    private Map<String, Object> memoryConfig;
}
