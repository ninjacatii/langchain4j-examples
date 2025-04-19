package dev.langchain4j.example.entity.agent._views;

import lombok.Data;

@Data
public class StepMetadata {
    private double stepStartTime;
    private double stepEndTime;
    private int inputTokens;
    private int stepNumber;

    public double getDurationSeconds() {
        return stepEndTime - stepStartTime;
    }
}
