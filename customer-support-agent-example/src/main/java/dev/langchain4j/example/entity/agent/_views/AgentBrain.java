package dev.langchain4j.example.entity.agent._views;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AgentBrain {
    private String pageSummary;

    private String evaluationPreviousGoal;
    private String memory;
    private String nextGoal;
}
