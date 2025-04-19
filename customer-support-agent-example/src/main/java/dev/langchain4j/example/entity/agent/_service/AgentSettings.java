package dev.langchain4j.example.entity.agent._service;

import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class AgentSettings {
    private boolean useVision = true;
    private boolean useVisionForPlanner = false;
    private String saveConversationPath;
    private String saveConversationPathEncoding = "utf-8";
    private int maxFailures = 3;
    private int retryDelay = 10;
    private String overrideSystemMessage;
    private String extendSystemMessage;
    private int maxInputTokens = 128000;
    private boolean validateOutput = false;
    private String messageContext;
    private Object generateGif = false;
    private List<String> availableFilePaths;
    private List<String> includeAttributes = Arrays.asList(
            "title", "type", "name", "role", "aria-label",
            "placeholder", "value", "alt", "aria-expanded", "data-date-format"
    );
    private int maxActionsPerStep = 10;
    private String toolCallingMethod = "auto";
    private Object pageExtractionLlm;
    private Object plannerLlm;
    private int plannerInterval = 1;
    private boolean isPlannerReasoning = false;
    private boolean enableMemory = false;
    private int memoryInterval = 10;
    private Map<String, Object> memoryConfig;
}
