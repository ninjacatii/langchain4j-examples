package dev.langchain4j.example.entity.agent.message_manager._service;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.model.openai.internal.DefaultOpenAiClient;
import dev.langchain4j.model.openai.internal.SyncOrAsync;
import dev.langchain4j.model.openai.internal.SyncOrAsyncOrStreaming;
import dev.langchain4j.model.openai.internal.chat.ChatCompletionRequest;
import dev.langchain4j.model.openai.internal.chat.ChatCompletionResponse;
import dev.langchain4j.model.openai.internal.completion.CompletionRequest;
import dev.langchain4j.model.openai.internal.completion.CompletionResponse;
import dev.langchain4j.model.openai.internal.embedding.EmbeddingRequest;
import dev.langchain4j.model.openai.internal.embedding.EmbeddingResponse;
import dev.langchain4j.model.openai.internal.image.GenerateImagesRequest;
import dev.langchain4j.model.openai.internal.image.GenerateImagesResponse;
import dev.langchain4j.model.openai.internal.moderation.ModerationRequest;
import dev.langchain4j.model.openai.internal.moderation.ModerationResponse;
import dev.langchain4j.model.openai.internal.spi.OpenAiClientBuilderFactory;
import dev.langchain4j.model.openai.internal.spi.ServiceHelper;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

public abstract class OpenAiQwenClient {
    public abstract SyncOrAsyncOrStreaming<CompletionResponse> completion(CompletionRequestQwen var1);

    public abstract SyncOrAsyncOrStreaming<ChatCompletionResponse> chatCompletion(ChatCompletionRequestQwen var1);

    public abstract SyncOrAsync<EmbeddingResponse> embedding(EmbeddingRequest var1);

    public abstract SyncOrAsync<ModerationResponse> moderation(ModerationRequest var1);

    public abstract SyncOrAsync<GenerateImagesResponse> imagesGeneration(GenerateImagesRequest var1);

    public static OpenAiQwenClient.Builder builder() {
        Iterator var0 = ServiceHelper.loadFactories(OpenAiQwenClientBuilderFactory.class).iterator();
        if (var0.hasNext()) {
            OpenAiQwenClientBuilderFactory factory = (OpenAiQwenClientBuilderFactory)var0.next();
            return (OpenAiQwenClient.Builder)factory.get();
        } else {
            return DefaultOpenAiQwenClient.builder();
        }
    }

    public abstract static class Builder<T extends OpenAiQwenClient, B extends OpenAiQwenClient.Builder<T, B>> {
        public HttpClientBuilder httpClientBuilder;
        public String baseUrl;
        public String organizationId;
        public String projectId;
        public String apiKey;
        public Duration connectTimeout;
        public Duration readTimeout;
        public String userAgent;
        public boolean logRequests;
        public boolean logResponses;
        public Map<String, String> customHeaders;

        public abstract T build();

        public B httpClientBuilder(HttpClientBuilder httpClientBuilder) {
            this.httpClientBuilder = httpClientBuilder;
            return (B)this;
        }

        public B baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return (B)this;
        }

        public B organizationId(String organizationId) {
            this.organizationId = organizationId;
            return (B)this;
        }

        public B projectId(String projectId) {
            this.projectId = projectId;
            return (B)this;
        }

        public B apiKey(String apiKey) {
            this.apiKey = apiKey;
            return (B)this;
        }

        public B connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return (B)this;
        }

        public B readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return (B)this;
        }

        public B userAgent(String userAgent) {
            this.userAgent = userAgent;
            return (B)this;
        }

        public B logRequests(Boolean logRequests) {
            if (logRequests == null) {
                logRequests = false;
            }

            this.logRequests = logRequests;
            return (B)this;
        }

        public B logResponses(Boolean logResponses) {
            if (logResponses == null) {
                logResponses = false;
            }

            this.logResponses = logResponses;
            return (B)this;
        }

        public B customHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
            return (B)this;
        }
    }
}

