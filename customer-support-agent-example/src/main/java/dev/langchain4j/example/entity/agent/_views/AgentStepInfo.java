package dev.langchain4j.example.entity.agent._views;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentStepInfo {
    private int stepNumber;
    private int maxSteps;

    public boolean isLastStep() {
        return stepNumber >= maxSteps - 1;
    }
}
