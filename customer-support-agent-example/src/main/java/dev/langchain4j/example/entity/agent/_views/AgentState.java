package dev.langchain4j.example.entity.agent._views;

import dev.langchain4j.example.entity.agent.message_manager._views.MessageManagerState;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class AgentState {
    private String agentId = UUID.randomUUID().toString();
    private int nSteps = 1;
    private int consecutiveFailures = 0;
    @Nullable
    private List<ActionResult> lastResult;
    private AgentHistoryList history;
    @Nullable private String lastPlan;
    private boolean paused;
    private boolean stopped;
    private MessageManagerState messageManagerState;
}
