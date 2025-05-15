package dev.langchain4j.example.entity.agent.message_manager._service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import dev.langchain4j.model.openai.internal.chat.*;
import dev.langchain4j.model.openai.internal.shared.StreamOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(
        builder = ChatCompletionRequestQwen.Builder.class
)
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class ChatCompletionRequestQwen {
    @JsonProperty
    private final String model;
    @JsonProperty
    private final List<Message> messages;
    @JsonProperty
    private final Double temperature;
    @JsonProperty
    private final Double topP;
    @JsonProperty
    private final Integer n;
    @JsonProperty
    private final Boolean stream;
    @JsonProperty
    private final Boolean enableThinking;
    @JsonProperty
    private final StreamOptions streamOptions;
    @JsonProperty
    private final List<String> stop;
    @JsonProperty
    private final Integer maxTokens;
    @JsonProperty
    private final Integer maxCompletionTokens;
    @JsonProperty
    private final Double presencePenalty;
    @JsonProperty
    private final Double frequencyPenalty;
    @JsonProperty
    private final Map<String, Integer> logitBias;
    @JsonProperty
    private final String user;
    @JsonProperty
    private final ResponseFormat responseFormat;
    @JsonProperty
    private final Integer seed;
    @JsonProperty
    private final List<Tool> tools;
    @JsonProperty
    private final Object toolChoice;
    @JsonProperty
    private final Boolean parallelToolCalls;
    @JsonProperty
    private final Boolean store;
    @JsonProperty
    private final Map<String, String> metadata;
    @JsonProperty
    private final String reasoningEffort;
    @JsonProperty
    private final String serviceTier;
    /** @deprecated */
    @JsonProperty
    @Deprecated
    private final List<Function> functions;
    /** @deprecated */
    @JsonProperty
    @Deprecated
    private final FunctionCall functionCall;

    public ChatCompletionRequestQwen(ChatCompletionRequestQwen.Builder builder) {
        this.model = builder.model;
        this.messages = builder.messages;
        this.temperature = builder.temperature;
        this.topP = builder.topP;
        this.n = builder.n;
        this.stream = builder.stream;
        this.enableThinking = builder.enableThinking;
        this.streamOptions = builder.streamOptions;
        this.stop = builder.stop;
        this.maxTokens = builder.maxTokens;
        this.maxCompletionTokens = builder.maxCompletionTokens;
        this.presencePenalty = builder.presencePenalty;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.logitBias = builder.logitBias;
        this.user = builder.user;
        this.responseFormat = builder.responseFormat;
        this.seed = builder.seed;
        this.tools = builder.tools;
        this.toolChoice = builder.toolChoice;
        this.parallelToolCalls = builder.parallelToolCalls;
        this.store = builder.store;
        this.metadata = builder.metadata;
        this.reasoningEffort = builder.reasoningEffort;
        this.serviceTier = builder.serviceTier;
        this.functions = builder.functions;
        this.functionCall = builder.functionCall;
    }

    public String model() {
        return this.model;
    }

    public List<Message> messages() {
        return this.messages;
    }

    public Double temperature() {
        return this.temperature;
    }

    public Double topP() {
        return this.topP;
    }

    public Integer n() {
        return this.n;
    }

    public Boolean stream() {
        return this.stream;
    }

    public Boolean enableThinking() {
        return this.enableThinking;
    }

    public StreamOptions streamOptions() {
        return this.streamOptions;
    }

    public List<String> stop() {
        return this.stop;
    }

    public Integer maxTokens() {
        return this.maxTokens;
    }

    public Integer maxCompletionTokens() {
        return this.maxCompletionTokens;
    }

    public Double presencePenalty() {
        return this.presencePenalty;
    }

    public Double frequencyPenalty() {
        return this.frequencyPenalty;
    }

    public Map<String, Integer> logitBias() {
        return this.logitBias;
    }

    public String user() {
        return this.user;
    }

    public ResponseFormat responseFormat() {
        return this.responseFormat;
    }

    public Integer seed() {
        return this.seed;
    }

    public List<Tool> tools() {
        return this.tools;
    }

    public Object toolChoice() {
        return this.toolChoice;
    }

    public Boolean parallelToolCalls() {
        return this.parallelToolCalls;
    }

    public Boolean store() {
        return this.store;
    }

    public Map<String, String> metadata() {
        return this.metadata;
    }

    public String reasoningEffort() {
        return this.reasoningEffort;
    }

    public String serviceTier() {
        return this.serviceTier;
    }

    /** @deprecated */
    @Deprecated
    public List<Function> functions() {
        return this.functions;
    }

    /** @deprecated */
    @Deprecated
    public FunctionCall functionCall() {
        return this.functionCall;
    }

    public boolean equals(Object another) {
        if (this == another) {
            return true;
        } else {
            return another instanceof ChatCompletionRequestQwen && this.equalTo((ChatCompletionRequestQwen)another);
        }
    }

    private boolean equalTo(ChatCompletionRequestQwen another) {
        return Objects.equals(this.model, another.model) && Objects.equals(this.messages, another.messages) && Objects.equals(this.temperature, another.temperature) && Objects.equals(this.topP, another.topP) && Objects.equals(this.n, another.n) && Objects.equals(this.stream, another.stream) && Objects.equals(this.enableThinking, another.enableThinking) && Objects.equals(this.streamOptions, another.streamOptions) && Objects.equals(this.stop, another.stop) && Objects.equals(this.maxTokens, another.maxTokens) && Objects.equals(this.maxCompletionTokens, another.maxCompletionTokens) && Objects.equals(this.presencePenalty, another.presencePenalty) && Objects.equals(this.frequencyPenalty, another.frequencyPenalty) && Objects.equals(this.logitBias, another.logitBias) && Objects.equals(this.user, another.user) && Objects.equals(this.responseFormat, another.responseFormat) && Objects.equals(this.seed, another.seed) && Objects.equals(this.tools, another.tools) && Objects.equals(this.toolChoice, another.toolChoice) && Objects.equals(this.parallelToolCalls, another.parallelToolCalls) && Objects.equals(this.store, another.store) && Objects.equals(this.metadata, another.metadata) && Objects.equals(this.reasoningEffort, another.reasoningEffort) && Objects.equals(this.serviceTier, another.serviceTier) && Objects.equals(this.functions, another.functions) && Objects.equals(this.functionCall, another.functionCall);
    }

    public int hashCode() {
        int h = 5381;
        h += (h << 5) + Objects.hashCode(this.model);
        h += (h << 5) + Objects.hashCode(this.messages);
        h += (h << 5) + Objects.hashCode(this.temperature);
        h += (h << 5) + Objects.hashCode(this.topP);
        h += (h << 5) + Objects.hashCode(this.n);
        h += (h << 5) + Objects.hashCode(this.stream);
        h += (h << 5) + Objects.hashCode(this.enableThinking);
        h += (h << 5) + Objects.hashCode(this.streamOptions);
        h += (h << 5) + Objects.hashCode(this.stop);
        h += (h << 5) + Objects.hashCode(this.maxTokens);
        h += (h << 5) + Objects.hashCode(this.maxCompletionTokens);
        h += (h << 5) + Objects.hashCode(this.presencePenalty);
        h += (h << 5) + Objects.hashCode(this.frequencyPenalty);
        h += (h << 5) + Objects.hashCode(this.logitBias);
        h += (h << 5) + Objects.hashCode(this.user);
        h += (h << 5) + Objects.hashCode(this.responseFormat);
        h += (h << 5) + Objects.hashCode(this.seed);
        h += (h << 5) + Objects.hashCode(this.tools);
        h += (h << 5) + Objects.hashCode(this.toolChoice);
        h += (h << 5) + Objects.hashCode(this.parallelToolCalls);
        h += (h << 5) + Objects.hashCode(this.store);
        h += (h << 5) + Objects.hashCode(this.metadata);
        h += (h << 5) + Objects.hashCode(this.reasoningEffort);
        h += (h << 5) + Objects.hashCode(this.serviceTier);
        h += (h << 5) + Objects.hashCode(this.functions);
        h += (h << 5) + Objects.hashCode(this.functionCall);
        return h;
    }

    public String toString() {
        String var10000 = this.model;
        return "ChatCompletionRequestQwen{model=" + var10000 + ", messages=" + String.valueOf(this.messages) + ", temperature=" + this.temperature + ", topP=" + this.topP + ", n=" + this.n + ", stream=" + this.stream + ", enableThinking=" + this.enableThinking + ", streamOptions=" + String.valueOf(this.streamOptions) + ", stop=" + String.valueOf(this.stop) + ", maxTokens=" + this.maxTokens + ", maxCompletionTokens=" + this.maxCompletionTokens + ", presencePenalty=" + this.presencePenalty + ", frequencyPenalty=" + this.frequencyPenalty + ", logitBias=" + String.valueOf(this.logitBias) + ", user=" + this.user + ", responseFormat=" + String.valueOf(this.responseFormat) + ", seed=" + this.seed + ", tools=" + String.valueOf(this.tools) + ", toolChoice=" + String.valueOf(this.toolChoice) + ", parallelToolCalls=" + this.parallelToolCalls + ", store=" + this.store + ", metadata=" + String.valueOf(this.metadata) + ", reasoningEffort=" + this.reasoningEffort + ", serviceTier=" + this.serviceTier + ", functions=" + String.valueOf(this.functions) + ", functionCall=" + String.valueOf(this.functionCall) + "}";
    }

    public static ChatCompletionRequestQwen.Builder builder() {
        return new ChatCompletionRequestQwen.Builder();
    }

    @JsonPOJOBuilder(
            withPrefix = ""
    )
    @JsonIgnoreProperties(
            ignoreUnknown = true
    )
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static final class Builder {
        private String model;
        private List<Message> messages;
        private Double temperature;
        private Double topP;
        private Integer n;
        private Boolean stream;
        private Boolean enableThinking;
        private StreamOptions streamOptions;
        private List<String> stop;
        private Integer maxTokens;
        private Integer maxCompletionTokens;
        private Double presencePenalty;
        private Double frequencyPenalty;
        private Map<String, Integer> logitBias;
        private String user;
        private ResponseFormat responseFormat;
        private Integer seed;
        private List<Tool> tools;
        private Object toolChoice;
        private Boolean parallelToolCalls;
        private Boolean store;
        private Map<String, String> metadata;
        private String reasoningEffort;
        private String serviceTier;
        /** @deprecated */
        @Deprecated
        private List<Function> functions;
        /** @deprecated */
        @Deprecated
        private FunctionCall functionCall;

        public ChatCompletionRequestQwen.Builder from(ChatCompletionRequestQwen instance) {
            this.model(instance.model);
            this.messages(instance.messages);
            this.temperature(instance.temperature);
            this.topP(instance.topP);
            this.n(instance.n);
            this.stream(instance.stream);
            this.enableThinking(instance.enableThinking);
            this.streamOptions(instance.streamOptions);
            this.stop(instance.stop);
            this.maxTokens(instance.maxTokens);
            this.maxCompletionTokens(instance.maxCompletionTokens);
            this.presencePenalty(instance.presencePenalty);
            this.frequencyPenalty(instance.frequencyPenalty);
            this.logitBias(instance.logitBias);
            this.user(instance.user);
            this.responseFormat(instance.responseFormat);
            this.seed(instance.seed);
            this.tools(instance.tools);
            this.toolChoice(instance.toolChoice);
            this.parallelToolCalls(instance.parallelToolCalls);
            this.store(instance.store);
            this.metadata(instance.metadata);
            this.reasoningEffort(instance.reasoningEffort);
            this.serviceTier(instance.serviceTier);
            this.functions(instance.functions);
            this.functionCall(instance.functionCall);
            return this;
        }

        public ChatCompletionRequestQwen.Builder model(String model) {
            this.model = model;
            return this;
        }

        @JsonSetter
        public ChatCompletionRequestQwen.Builder messages(List<Message> messages) {
            if (messages != null) {
                this.messages = Collections.unmodifiableList(messages);
            }

            return this;
        }

        public ChatCompletionRequestQwen.Builder messages(Message... messages) {
            return this.messages(Arrays.asList(messages));
        }

        public ChatCompletionRequestQwen.Builder addSystemMessage(String systemMessage) {
            if (this.messages == null) {
                this.messages = new ArrayList();
            }

            this.messages.add(SystemMessage.from(systemMessage));
            return this;
        }

        public ChatCompletionRequestQwen.Builder addUserMessage(String userMessage) {
            if (this.messages == null) {
                this.messages = new ArrayList();
            }

            this.messages.add(UserMessage.from(userMessage));
            return this;
        }

        public ChatCompletionRequestQwen.Builder addAssistantMessage(String assistantMessage) {
            if (this.messages == null) {
                this.messages = new ArrayList();
            }

            this.messages.add(AssistantMessage.from(assistantMessage));
            return this;
        }

        public ChatCompletionRequestQwen.Builder addToolMessage(String toolCallId, String content) {
            if (this.messages == null) {
                this.messages = new ArrayList();
            }

            this.messages.add(ToolMessage.from(toolCallId, content));
            return this;
        }

        public ChatCompletionRequestQwen.Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public ChatCompletionRequestQwen.Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public ChatCompletionRequestQwen.Builder n(Integer n) {
            this.n = n;
            return this;
        }

        public ChatCompletionRequestQwen.Builder stream(Boolean stream) {
            this.stream = stream;
            return this;
        }

        public ChatCompletionRequestQwen.Builder enableThinking(Boolean enableThinking) {
            this.enableThinking = enableThinking;
            return this;
        }

        public ChatCompletionRequestQwen.Builder streamOptions(StreamOptions streamOptions) {
            this.streamOptions = streamOptions;
            return this;
        }

        @JsonSetter
        public ChatCompletionRequestQwen.Builder stop(List<String> stop) {
            if (stop != null) {
                this.stop = Collections.unmodifiableList(stop);
            }

            return this;
        }

        public ChatCompletionRequestQwen.Builder stop(String... stop) {
            return this.stop(Arrays.asList(stop));
        }

        public ChatCompletionRequestQwen.Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public ChatCompletionRequestQwen.Builder maxCompletionTokens(Integer maxCompletionTokens) {
            this.maxCompletionTokens = maxCompletionTokens;
            return this;
        }

        public ChatCompletionRequestQwen.Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public ChatCompletionRequestQwen.Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public ChatCompletionRequestQwen.Builder logitBias(Map<String, Integer> logitBias) {
            if (logitBias != null) {
                this.logitBias = Collections.unmodifiableMap(logitBias);
            }

            return this;
        }

        public ChatCompletionRequestQwen.Builder user(String user) {
            this.user = user;
            return this;
        }

        public ChatCompletionRequestQwen.Builder responseFormat(ResponseFormatType responseFormatType) {
            if (responseFormatType != null) {
                this.responseFormat = ResponseFormat.builder().type(responseFormatType).build();
            }

            return this;
        }

        @JsonSetter
        public ChatCompletionRequestQwen.Builder responseFormat(ResponseFormat responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }

        public ChatCompletionRequestQwen.Builder seed(Integer seed) {
            this.seed = seed;
            return this;
        }

        @JsonSetter
        public ChatCompletionRequestQwen.Builder tools(List<Tool> tools) {
            if (tools != null) {
                this.tools = Collections.unmodifiableList(tools);
            }

            return this;
        }

        public ChatCompletionRequestQwen.Builder tools(Tool... tools) {
            return this.tools(Arrays.asList(tools));
        }

        public ChatCompletionRequestQwen.Builder toolChoice(ToolChoiceMode toolChoiceMode) {
            this.toolChoice = toolChoiceMode;
            return this;
        }

        public ChatCompletionRequestQwen.Builder toolChoice(String functionName) {
            return this.toolChoice((Object)ToolChoice.from(functionName));
        }

        public ChatCompletionRequestQwen.Builder toolChoice(Object toolChoice) {
            this.toolChoice = toolChoice;
            return this;
        }

        public ChatCompletionRequestQwen.Builder parallelToolCalls(Boolean parallelToolCalls) {
            this.parallelToolCalls = parallelToolCalls;
            return this;
        }

        public ChatCompletionRequestQwen.Builder store(Boolean store) {
            this.store = store;
            return this;
        }

        public ChatCompletionRequestQwen.Builder metadata(Map<String, String> metadata) {
            if (metadata != null) {
                this.metadata = Collections.unmodifiableMap(metadata);
            }

            return this;
        }

        public ChatCompletionRequestQwen.Builder reasoningEffort(String reasoningEffort) {
            this.reasoningEffort = reasoningEffort;
            return this;
        }

        public ChatCompletionRequestQwen.Builder serviceTier(String serviceTier) {
            this.serviceTier = serviceTier;
            return this;
        }

        /** @deprecated */
        @Deprecated
        public ChatCompletionRequestQwen.Builder functions(Function... functions) {
            return this.functions(Arrays.asList(functions));
        }

        /** @deprecated */
        @JsonSetter
        @Deprecated
        public ChatCompletionRequestQwen.Builder functions(List<Function> functions) {
            if (functions != null) {
                this.functions = Collections.unmodifiableList(functions);
            }

            return this;
        }

        /** @deprecated */
        @Deprecated
        public ChatCompletionRequestQwen.Builder functionCall(String functionName) {
            if (functionName != null) {
                this.functionCall = FunctionCall.builder().name(functionName).build();
            }

            return this;
        }

        /** @deprecated */
        @Deprecated
        public ChatCompletionRequestQwen.Builder functionCall(FunctionCall functionCall) {
            this.functionCall = functionCall;
            return this;
        }

        public ChatCompletionRequestQwen build() {
            return new ChatCompletionRequestQwen(this);
        }
    }
}

