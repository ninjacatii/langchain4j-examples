package dev.langchain4j.example.entity.agent._prompts;


import dev.langchain4j.data.message.SystemMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class SystemPrompt {
    private String defaultActionDescription;
    private int maxActionsPerStep;
    private String promptTemplate;
    @Getter
    private SystemMessage systemMessage;

    public SystemPrompt(
            String actionDescription,
            int maxActionsPerStep,
            @Nullable String overrideSystemMessage,
            @Nullable String extendSystemMessage
    ) {
        this.defaultActionDescription = actionDescription;
        this.maxActionsPerStep = maxActionsPerStep;
        String prompt = "";
        if (overrideSystemMessage != null) {
            prompt = overrideSystemMessage;
        } else {
            this.loadPromptTemplate();
            prompt = this.promptTemplate.replace("{max_actions}", String.valueOf(this.maxActionsPerStep));
        }

        if (extendSystemMessage != null) {
            prompt += "\n" + extendSystemMessage;
        }

        this.systemMessage = new SystemMessage(prompt);
    }

    private void loadPromptTemplate() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("system_prompt.md")) {
            this.promptTemplate = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
