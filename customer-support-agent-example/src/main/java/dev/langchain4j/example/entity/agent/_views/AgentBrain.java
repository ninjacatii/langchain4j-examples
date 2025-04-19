package dev.langchain4j.example.entity.agent._views;

import lombok.Data;

@Data
public class AgentBrain {
    private String evaluationPreviousGoal;
    private String memory;
    private String nextGoal;
}
