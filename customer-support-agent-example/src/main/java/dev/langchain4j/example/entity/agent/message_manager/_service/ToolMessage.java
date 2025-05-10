package dev.langchain4j.example.entity.agent.message_manager._service;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.model.openai.internal.chat.Role;

public class ToolMessage implements ChatMessage {
    private final String toolCallId;
    private final String content;

    public ToolMessage(String toolCallId, String content) {
        this.toolCallId = toolCallId;
        this.content = content;
    }

    public String toString() {
        return "ToolMessage { content = \"" + content + "\" tool_call_id = \"" + toolCallId + "\" }";
    }

    @Override
    public ChatMessageType type() {
        return ChatMessageType.AI;
    }
}
