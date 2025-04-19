package dev.langchain4j.example.entity.agent.message_manager._views;

import dev.langchain4j.data.message.ChatMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageHistory {
    private List<ManagedMessage> messages = new ArrayList<>();
    private Integer currentTokens = 0;

    public void addMessage(ChatMessage message, MessageMetadata metadata, Integer position) {
        ManagedMessage managedMessage = new ManagedMessage(message, metadata);
        if (position == null) {
            messages.add(managedMessage);
        } else {
            messages.add(position, managedMessage);
        }
        currentTokens += metadata.getTokens();
    }

    public void removeLastStateMessage() {
        if (!messages.isEmpty()) {
            ManagedMessage last = messages.remove(messages.size() - 1);
            currentTokens -= last.getMetadata().getTokens();
        }
    }
}
