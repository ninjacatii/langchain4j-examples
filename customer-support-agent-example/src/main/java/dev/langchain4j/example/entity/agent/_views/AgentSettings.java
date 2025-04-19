package dev.langchain4j.example.entity.agent._views;

import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

public record AgentSettings(
        boolean useVision,
        boolean useVisionForPlanner,
        @Nullable String saveConversationPath,
        @Nullable String saveConversationPathEncoding,
        int maxFailures,
        int retryDelay,
        int maxInputTokens,
        boolean validateOutput,
        @Nullable String messageContext,
        @Nullable Object generateGif, // boolean or String
        @Nullable List<String> availableFilePaths,
        @Nullable String overrideSystemMessage,
        @Nullable String extendSystemMessage,
        List<String> includeAttributes,
        int maxActionsPerStep,
        @Nullable String toolCallingMethod,
        @Nullable Object pageExtractionLlm, // BaseChatModel
        @Nullable Object plannerLlm, // BaseChatModel
        int plannerInterval,
        boolean isPlannerReasoning,
        boolean enableMemory,
        int memoryInterval,
        @Nullable Map<String, Object> memoryConfig
) {
    public AgentSettings {
        includeAttributes = includeAttributes != null ? includeAttributes : List.of(
                "title", "type", "name", "role", "tabindex",
                "aria-label", "placeholder", "value", "alt", "aria-expanded"
        );
        saveConversationPathEncoding = saveConversationPathEncoding != null ?
                saveConversationPathEncoding : "utf-8";
    }
}