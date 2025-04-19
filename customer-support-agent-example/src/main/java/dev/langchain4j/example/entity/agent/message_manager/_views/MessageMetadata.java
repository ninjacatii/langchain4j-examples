package dev.langchain4j.example.entity.agent.message_manager._views;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageMetadata {
    private Integer tokens;
    private String messageType;

    public MessageMetadata(Integer tokens, String messageType) {
        this.tokens = tokens;
        this.messageType = messageType;
    }
}
