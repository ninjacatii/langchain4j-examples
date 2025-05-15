package dev.langchain4j.example.entity.agent.message_manager._service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import dev.langchain4j.model.openai.internal.shared.StreamOptions;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(
        builder = CompletionRequestQwen.Builder.class
)
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class CompletionRequestQwen {
    @JsonProperty
    private final String model;
    @JsonProperty
    private final String prompt;
    @JsonProperty
    private final String suffix;
    @JsonProperty
    private final Integer maxTokens;
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
    private final Integer logprobs;
    @JsonProperty
    private final Boolean echo;
    @JsonProperty
    private final List<String> stop;
    @JsonProperty
    private final Double presencePenalty;
    @JsonProperty
    private final Double frequencyPenalty;
    @JsonProperty
    private final Integer bestOf;
    @JsonProperty
    private final Map<String, Integer> logitBias;
    @JsonProperty
    private final String user;

    public CompletionRequestQwen(Builder builder) {
        this.model = builder.model;
        this.prompt = builder.prompt;
        this.suffix = builder.suffix;
        this.maxTokens = builder.maxTokens;
        this.temperature = builder.temperature;
        this.topP = builder.topP;
        this.n = builder.n;
        this.stream = builder.stream;
        this.enableThinking = builder.enableThinking;
        this.streamOptions = builder.streamOptions;
        this.logprobs = builder.logprobs;
        this.echo = builder.echo;
        this.stop = builder.stop;
        this.presencePenalty = builder.presencePenalty;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.bestOf = builder.bestOf;
        this.logitBias = builder.logitBias;
        this.user = builder.user;
    }

    public String model() {
        return this.model;
    }

    public String prompt() {
        return this.prompt;
    }

    public String suffix() {
        return this.suffix;
    }

    public Integer maxTokens() {
        return this.maxTokens;
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

    public Boolean enableThinking() { return this.enableThinking; }

    public StreamOptions streamOptions() {
        return this.streamOptions;
    }

    public Integer logprobs() {
        return this.logprobs;
    }

    public Boolean echo() {
        return this.echo;
    }

    public List<String> stop() {
        return this.stop;
    }

    public Double presencePenalty() {
        return this.presencePenalty;
    }

    public Double frequencyPenalty() {
        return this.frequencyPenalty;
    }

    public Integer bestOf() {
        return this.bestOf;
    }

    public Map<String, Integer> logitBias() {
        return this.logitBias;
    }

    public String user() {
        return this.user;
    }

    public boolean equals(Object another) {
        if (this == another) {
            return true;
        } else {
            return another instanceof CompletionRequestQwen && this.equalTo((CompletionRequestQwen)another);
        }
    }

    private boolean equalTo(CompletionRequestQwen another) {
        return Objects.equals(this.model, another.model) && Objects.equals(this.prompt, another.prompt) && Objects.equals(this.suffix, another.suffix) && Objects.equals(this.maxTokens, another.maxTokens) && Objects.equals(this.temperature, another.temperature) && Objects.equals(this.topP, another.topP) && Objects.equals(this.n, another.n) && Objects.equals(this.stream, another.stream) && Objects.equals(this.enableThinking, another.enableThinking) && Objects.equals(this.streamOptions, another.streamOptions) && Objects.equals(this.logprobs, another.logprobs) && Objects.equals(this.echo, another.echo) && Objects.equals(this.stop, another.stop) && Objects.equals(this.presencePenalty, another.presencePenalty) && Objects.equals(this.frequencyPenalty, another.frequencyPenalty) && Objects.equals(this.bestOf, another.bestOf) && Objects.equals(this.logitBias, another.logitBias) && Objects.equals(this.user, another.user);
    }

    public int hashCode() {
        int h = 5381;
        h += (h << 5) + Objects.hashCode(this.model);
        h += (h << 5) + Objects.hashCode(this.prompt);
        h += (h << 5) + Objects.hashCode(this.suffix);
        h += (h << 5) + Objects.hashCode(this.maxTokens);
        h += (h << 5) + Objects.hashCode(this.temperature);
        h += (h << 5) + Objects.hashCode(this.topP);
        h += (h << 5) + Objects.hashCode(this.n);
        h += (h << 5) + Objects.hashCode(this.stream);
        h += (h << 5) + Objects.hashCode(this.enableThinking);
        h += (h << 5) + Objects.hashCode(this.streamOptions);
        h += (h << 5) + Objects.hashCode(this.logprobs);
        h += (h << 5) + Objects.hashCode(this.echo);
        h += (h << 5) + Objects.hashCode(this.stop);
        h += (h << 5) + Objects.hashCode(this.presencePenalty);
        h += (h << 5) + Objects.hashCode(this.frequencyPenalty);
        h += (h << 5) + Objects.hashCode(this.bestOf);
        h += (h << 5) + Objects.hashCode(this.logitBias);
        h += (h << 5) + Objects.hashCode(this.user);
        return h;
    }

    public String toString() {
        String var10000 = this.model;
        return "CompletionRequestQwen{model=" + var10000 + ", prompt=" + this.prompt + ", suffix=" + this.suffix + ", maxTokens=" + this.maxTokens + ", temperature=" + this.temperature + ", topP=" + this.topP + ", n=" + this.n + ", stream=" + this.stream + ", enableThinking=" + this.enableThinking + ", streamOptions=" + String.valueOf(this.streamOptions) + ", logprobs=" + this.logprobs + ", echo=" + this.echo + ", stop=" + String.valueOf(this.stop) + ", presencePenalty=" + this.presencePenalty + ", frequencyPenalty=" + this.frequencyPenalty + ", bestOf=" + this.bestOf + ", logitBias=" + String.valueOf(this.logitBias) + ", user=" + this.user + "}";
    }

    public static Builder builder() {
        return new Builder();
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
        private String prompt;
        private String suffix;
        private Integer maxTokens;
        private Double temperature;
        private Double topP;
        private Integer n;
        private Boolean stream;
        private Boolean enableThinking;
        private StreamOptions streamOptions;
        private Integer logprobs;
        private Boolean echo;
        private List<String> stop;
        private Double presencePenalty;
        private Double frequencyPenalty;
        private Integer bestOf;
        private Map<String, Integer> logitBias;
        private String user;

        public Builder from(CompletionRequestQwen request) {
            this.model(request.model);
            this.prompt(request.prompt);
            this.suffix(request.suffix);
            this.maxTokens(request.maxTokens);
            this.temperature(request.temperature);
            this.topP(request.topP);
            this.n(request.n);
            this.stream(request.stream);
            this.enableThinking(request.enableThinking);
            this.streamOptions(request.streamOptions);
            this.logprobs(request.logprobs);
            this.echo(request.echo);
            this.stop(request.stop);
            this.presencePenalty(request.presencePenalty);
            this.frequencyPenalty(request.frequencyPenalty);
            this.bestOf(request.bestOf);
            this.logitBias(request.logitBias);
            this.user(request.user);
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder n(Integer n) {
            this.n = n;
            return this;
        }

        public Builder stream(Boolean stream) {
            this.stream = stream;
            return this;
        }

        public Builder enableThinking(Boolean enableThinking) {
            this.enableThinking = enableThinking;
            return this;
        }

        public Builder streamOptions(StreamOptions streamOptions) {
            this.streamOptions = streamOptions;
            return this;
        }

        public Builder logprobs(Integer logprobs) {
            this.logprobs = logprobs;
            return this;
        }

        public Builder echo(Boolean echo) {
            this.echo = echo;
            return this;
        }

        public Builder stop(List<String> stop) {
            if (stop != null) {
                this.stop = Collections.unmodifiableList(stop);
            }

            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder bestOf(Integer bestOf) {
            this.bestOf = bestOf;
            return this;
        }

        public Builder logitBias(Map<String, Integer> logitBias) {
            if (logitBias != null) {
                this.logitBias = Collections.unmodifiableMap(logitBias);
            }

            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public CompletionRequestQwen build() {
            return new CompletionRequestQwen(this);
        }
    }
}
