package dev.langchain4j.example.entity.agent.message_manager._service;

import dev.langchain4j.model.openai.internal.OpenAiClient;
import java.util.function.Supplier;

public interface OpenAiQwenClientBuilderFactory extends Supplier<OpenAiQwenClient.Builder> {
}
