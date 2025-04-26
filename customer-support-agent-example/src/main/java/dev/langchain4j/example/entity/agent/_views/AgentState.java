package dev.langchain4j.example.entity.agent._views;

import dev.langchain4j.example.entity.agent.message_manager._views.MessageManagerState;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class AgentState {
    private String agentId;
    private int nSteps;
    private int consecutiveFailures;
    @Nullable
    private List<ActionResult> lastResult;
    private AgentHistoryList history;
    @Nullable private String lastPlan;
    private boolean paused;
    private boolean stopped;
    private MessageManagerState messageManagerState;

    public AgentState() {
        this.agentId = UUID.randomUUID().toString();
        this.nSteps = 1;
        this.consecutiveFailures = 0;
        this.history = new AgentHistoryList(new ArrayList<>());
        this.messageManagerState = new MessageManagerState();
    }
}
