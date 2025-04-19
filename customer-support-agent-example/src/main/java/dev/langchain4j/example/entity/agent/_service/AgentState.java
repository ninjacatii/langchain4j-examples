package dev.langchain4j.example.entity.agent._service;

import dev.langchain4j.example.entity.agent._views.ActionResult;
import dev.langchain4j.example.entity.agent._views.AgentHistory;
import dev.langchain4j.example.entity.agent.message_manager._views.MessageManagerState;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class AgentState {
    private MessageManagerState messageManagerState = new MessageManagerState();
    private String agentId = UUID.randomUUID().toString();
    private int nSteps = 0;
    private int consecutiveFailures = 0;
    private boolean paused = false;
    private boolean stopped = false;
    private List<ActionResult> lastResult;
    private List<AgentHistory> history = new ArrayList<>();
}
