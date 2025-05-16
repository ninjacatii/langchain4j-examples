package dev.langchain4j.example.entity.agent._service;

import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Data;

@Data
public class MyChatResponse {
    private ChatResponse chatResponse;
    private Throwable throwable;
}