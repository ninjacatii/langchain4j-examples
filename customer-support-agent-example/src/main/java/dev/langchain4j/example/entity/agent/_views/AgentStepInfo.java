package dev.langchain4j.example.entity.agent._views;

import lombok.Data;

@Data
public class AgentStepInfo {
    private int stepNumber;
    private int maxSteps;

    public boolean isLastStep() {
        return stepNumber >= maxSteps - 1;
    }
}
