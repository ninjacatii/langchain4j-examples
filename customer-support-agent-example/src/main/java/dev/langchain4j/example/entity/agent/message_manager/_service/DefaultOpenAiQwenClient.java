package dev.langchain4j.example.entity.agent.message_manager._service;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpClientBuilderLoader;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.log.LoggingHttpClient;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.openai.internal.*;
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
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DefaultOpenAiQwenClient extends OpenAiQwenClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final Map<String, String> defaultHeaders;

    public DefaultOpenAiQwenClient(DefaultOpenAiQwenClient.Builder builder) {
        HttpClientBuilder httpClientBuilder = (HttpClientBuilder)Utils.getOrDefault(builder.httpClientBuilder, HttpClientBuilderLoader::loadHttpClientBuilder);
        HttpClient httpClient = httpClientBuilder.connectTimeout((Duration)Utils.getOrDefault((Duration)Utils.getOrDefault(builder.connectTimeout, httpClientBuilder.connectTimeout()), Duration.ofSeconds(15L))).readTimeout((Duration)Utils.getOrDefault((Duration)Utils.getOrDefault(builder.readTimeout, httpClientBuilder.readTimeout()), Duration.ofSeconds(60L))).build();
        if (!builder.logRequests && !builder.logResponses) {
            this.httpClient = httpClient;
        } else {
            this.httpClient = new LoggingHttpClient(httpClient, builder.logRequests, builder.logResponses);
        }

        this.baseUrl = ValidationUtils.ensureNotBlank(builder.baseUrl, "baseUrl");
        Map<String, String> defaultHeaders = new HashMap();
        if (builder.apiKey != null) {
            defaultHeaders.put("Authorization", "Bearer " + builder.apiKey);
        }

        if (builder.organizationId != null) {
            defaultHeaders.put("OpenAI-Organization", builder.organizationId);
        }

        if (builder.projectId != null) {
            defaultHeaders.put("OpenAI-Project", builder.projectId);
        }

        if (builder.userAgent != null) {
            defaultHeaders.put("User-Agent", builder.userAgent);
        }

        if (builder.customHeaders != null) {
            defaultHeaders.putAll(builder.customHeaders);
        }

        this.defaultHeaders = defaultHeaders;
    }

    public static DefaultOpenAiQwenClient.Builder builder() {
        return new DefaultOpenAiQwenClient.Builder();
    }

    public SyncOrAsyncOrStreaming<CompletionResponse> completion(CompletionRequestQwen request) {
        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).url(this.baseUrl, "completions").addHeader("Content-Type", new String[]{"application/json"}).addHeaders(this.defaultHeaders).body(Json.toJson(CompletionRequestQwen.builder().from(request).stream(false).enableThinking(false).build())).build();
        HttpRequest streamingHttpRequest = HttpRequest.builder().method(HttpMethod.POST).url(this.baseUrl, "completions").addHeader("Content-Type", new String[]{"application/json"}).addHeaders(this.defaultHeaders).body(Json.toJson(CompletionRequestQwen.builder().from(request).stream(true).enableThinking(false).build())).build();
        return new RequestExecutor(this.httpClient, httpRequest, streamingHttpRequest, CompletionResponse.class);
    }

    public SyncOrAsyncOrStreaming<ChatCompletionResponse> chatCompletion(ChatCompletionRequestQwen request) {
        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).url(this.baseUrl, "chat/completions").addHeader("Content-Type", new String[]{"application/json"}).addHeaders(this.defaultHeaders).body(Json.toJson(ChatCompletionRequestQwen.builder().from(request).stream(false).enableThinking(false).build())).build();
        HttpRequest streamingHttpRequest = HttpRequest.builder().method(HttpMethod.POST).url(this.baseUrl, "chat/completions").addHeader("Content-Type", new String[]{"application/json"}).addHeaders(this.defaultHeaders).body(Json.toJson(ChatCompletionRequestQwen.builder().from(request).stream(true).enableThinking(false).build())).build();
        return new RequestExecutor(this.httpClient, httpRequest, streamingHttpRequest, ChatCompletionResponse.class);
    }

    public SyncOrAsync<EmbeddingResponse> embedding(EmbeddingRequest request) {
        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).url(this.baseUrl, "embeddings").addHeader("Content-Type", new String[]{"application/json"}).addHeaders(this.defaultHeaders).body(Json.toJson(request)).build();
        return new RequestExecutor(this.httpClient, httpRequest, EmbeddingResponse.class);
    }

    public SyncOrAsync<ModerationResponse> moderation(ModerationRequest request) {
        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).url(this.baseUrl, "moderations").addHeader("Content-Type", new String[]{"application/json"}).addHeaders(this.defaultHeaders).body(Json.toJson(request)).build();
        return new RequestExecutor(this.httpClient, httpRequest, ModerationResponse.class);
    }

    public SyncOrAsync<GenerateImagesResponse> imagesGeneration(GenerateImagesRequest request) {
        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).url(this.baseUrl, "images/generations").addHeader("Content-Type", new String[]{"application/json"}).addHeaders(this.defaultHeaders).body(Json.toJson(request)).build();
        return new RequestExecutor(this.httpClient, httpRequest, GenerateImagesResponse.class);
    }

    public static class Builder extends OpenAiQwenClient.Builder<DefaultOpenAiQwenClient, DefaultOpenAiQwenClient.Builder> {
        public DefaultOpenAiQwenClient build() {
            return new DefaultOpenAiQwenClient(this);
        }
    }
}
