package dev.langchain4j.example.entity.agent.message_manager._views;

import dev.langchain4j.data.message.ChatMessage;
import lombok.Data;

@Data
public class ManagedMessage {
    private ChatMessage message;
    private MessageMetadata metadata;

    public ManagedMessage(ChatMessage message, MessageMetadata metadata) {
        this.message = message;
        this.metadata = metadata;
    }
}
