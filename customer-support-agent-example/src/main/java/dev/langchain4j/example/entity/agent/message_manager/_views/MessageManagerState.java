package dev.langchain4j.example.entity.agent.message_manager._views;

import lombok.Data;

@Data
public class MessageManagerState {
    private MessageHistory history = new MessageHistory();
    private Integer toolId = 0;

    public void incrementToolId() {
        toolId++;
    }
}
