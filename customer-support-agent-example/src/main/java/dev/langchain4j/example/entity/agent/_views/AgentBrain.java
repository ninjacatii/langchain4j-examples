package dev.langchain4j.example.entity.agent._views;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentBrain {
    private String pageSummary;
    private String evaluationPreviousGoal;
    private String memory;
    private String nextGoal;
}
