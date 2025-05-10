package dev.langchain4j.example.entity.agent.message_manager._service;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.UnsupportedFeatureException;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.internal.Exceptions;
import dev.langchain4j.internal.RetryUtils;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElementHelper;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.InternalOpenAiHelper;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiChatResponseMetadata;
import dev.langchain4j.model.openai.internal.OpenAiClient;
import dev.langchain4j.model.openai.internal.chat.*;
import dev.langchain4j.model.openai.internal.chat.Content;
import dev.langchain4j.model.openai.internal.chat.ToolMessage;
import dev.langchain4j.model.openai.spi.OpenAiChatModelBuilderFactory;
import dev.langchain4j.spi.ServiceHelper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OpenAiChatModelWithToolMessage implements ChatLanguageModel {
    private final OpenAiClient client;
    private final Integer maxRetries;
    private final OpenAiChatRequestParameters defaultRequestParameters;
    private final String responseFormat;
    private final Set<Capability> supportedCapabilities;
    private final Boolean strictJsonSchema;
    private final Boolean strictTools;
    private final List<ChatModelListener> listeners;

    public OpenAiChatModelWithToolMessage(OpenAiChatModelWithToolMessageBuilder builder) {
        if ("demo".equals(builder.apiKey) && !"http://langchain4j.dev/demo/openai/v1".equals(builder.baseUrl)) {
            throw new RuntimeException("If you wish to continue using the 'demo' key, please specify the base URL explicitly:\nOpenAiChatModel.builder().baseUrl(\"http://langchain4j.dev/demo/openai/v1\").apiKey(\"demo\").build();\n");
        } else {
            this.client = OpenAiClient.builder().httpClientBuilder(builder.httpClientBuilder).baseUrl((String)Utils.getOrDefault(builder.baseUrl, "https://api.openai.com/v1")).apiKey(builder.apiKey).organizationId(builder.organizationId).projectId(builder.projectId).connectTimeout((Duration)Utils.getOrDefault(builder.timeout, Duration.ofSeconds(15L))).readTimeout((Duration)Utils.getOrDefault(builder.timeout, Duration.ofSeconds(60L))).logRequests((Boolean)Utils.getOrDefault(builder.logRequests, false)).logResponses((Boolean)Utils.getOrDefault(builder.logResponses, false)).userAgent("langchain4j-openai").customHeaders(builder.customHeaders).build();
            this.maxRetries = (Integer)Utils.getOrDefault(builder.maxRetries, 3);
            ChatRequestParameters commonParameters;
            if (builder.defaultRequestParameters != null) {
                commonParameters = builder.defaultRequestParameters;
            } else {
                commonParameters = DefaultChatRequestParameters.builder().build();
            }

            ChatRequestParameters var5 = builder.defaultRequestParameters;
            OpenAiChatRequestParameters openAiParameters;
            if (var5 instanceof OpenAiChatRequestParameters) {
                OpenAiChatRequestParameters openAiChatRequestParameters = (OpenAiChatRequestParameters)var5;
                openAiParameters = openAiChatRequestParameters;
            } else {
                openAiParameters = OpenAiChatRequestParameters.builder().build();
            }

            this.defaultRequestParameters = ((OpenAiChatRequestParameters.Builder)((OpenAiChatRequestParameters.Builder)((OpenAiChatRequestParameters.Builder)((OpenAiChatRequestParameters.Builder)((OpenAiChatRequestParameters.Builder)((OpenAiChatRequestParameters.Builder)((OpenAiChatRequestParameters.Builder)((OpenAiChatRequestParameters.Builder)((OpenAiChatRequestParameters.Builder)((OpenAiChatRequestParameters.Builder)OpenAiChatRequestParameters.builder().modelName((String)Utils.getOrDefault(builder.modelName, commonParameters.modelName()))).temperature((Double)Utils.getOrDefault(builder.temperature, commonParameters.temperature()))).topP((Double)Utils.getOrDefault(builder.topP, commonParameters.topP()))).frequencyPenalty((Double)Utils.getOrDefault(builder.frequencyPenalty, commonParameters.frequencyPenalty()))).presencePenalty((Double)Utils.getOrDefault(builder.presencePenalty, commonParameters.presencePenalty()))).maxOutputTokens((Integer)Utils.getOrDefault(builder.maxTokens, commonParameters.maxOutputTokens()))).stopSequences((List)Utils.getOrDefault(builder.stop, () -> Utils.copyIfNotNull(commonParameters.stopSequences())))).toolSpecifications(Utils.copyIfNotNull(commonParameters.toolSpecifications()))).toolChoice(commonParameters.toolChoice())).responseFormat((ResponseFormat)Utils.getOrDefault(fromOpenAiResponseFormat(builder.responseFormat), commonParameters.responseFormat()))).maxCompletionTokens((Integer)Utils.getOrDefault(builder.maxCompletionTokens, openAiParameters.maxCompletionTokens())).logitBias((Map)Utils.getOrDefault(builder.logitBias, () -> Utils.copyIfNotNull(openAiParameters.logitBias()))).parallelToolCalls((Boolean)Utils.getOrDefault(builder.parallelToolCalls, openAiParameters.parallelToolCalls())).seed((Integer)Utils.getOrDefault(builder.seed, openAiParameters.seed())).user((String)Utils.getOrDefault(builder.user, openAiParameters.user())).store((Boolean)Utils.getOrDefault(builder.store, openAiParameters.store())).metadata((Map)Utils.getOrDefault(builder.metadata, () -> Utils.copyIfNotNull(openAiParameters.metadata()))).serviceTier((String)Utils.getOrDefault(builder.serviceTier, openAiParameters.serviceTier())).reasoningEffort(openAiParameters.reasoningEffort()).build();
            this.responseFormat = builder.responseFormat;
            this.supportedCapabilities = new HashSet((Collection)Utils.getOrDefault(builder.supportedCapabilities, Collections.emptySet()));
            this.strictJsonSchema = (Boolean)Utils.getOrDefault(builder.strictJsonSchema, false);
            this.strictTools = (Boolean)Utils.getOrDefault(builder.strictTools, false);
            this.listeners = (List<ChatModelListener>)(builder.listeners == null ? new ArrayList<ChatModelListener>() : new ArrayList(builder.listeners));
        }
    }

    static dev.langchain4j.model.chat.request.ResponseFormat fromOpenAiResponseFormat(String responseFormat) {
        return "json_object".equals(responseFormat) ? dev.langchain4j.model.chat.request.ResponseFormat.JSON : null;
    }

    public OpenAiChatRequestParameters defaultRequestParameters() {
        return this.defaultRequestParameters;
    }

    public Set<Capability> supportedCapabilities() {
        Set<Capability> capabilities = new HashSet(this.supportedCapabilities);
        if ("json_schema".equals(this.responseFormat)) {
            capabilities.add(Capability.RESPONSE_FORMAT_JSON_SCHEMA);
        }

        return capabilities;
    }

    static void validate(ChatRequestParameters parameters) {
        if (parameters.topK() != null) {
            throw new UnsupportedFeatureException("'topK' parameter is not supported by OpenAI");
        }
    }

    static ChatCompletionRequest.Builder toOpenAiChatRequest(ChatRequest chatRequest, OpenAiChatRequestParameters parameters, Boolean strictTools, Boolean strictJsonSchema) {
        return ChatCompletionRequest.builder().messages(toOpenAiMessages(chatRequest.messages())).model(parameters.modelName()).temperature(parameters.temperature()).topP(parameters.topP()).frequencyPenalty(parameters.frequencyPenalty()).presencePenalty(parameters.presencePenalty()).maxTokens(parameters.maxOutputTokens()).stop(parameters.stopSequences()).tools(InternalOpenAiHelper.toTools(parameters.toolSpecifications(), strictTools)).toolChoice(InternalOpenAiHelper.toOpenAiToolChoice(parameters.toolChoice())).responseFormat(toOpenAiResponseFormat(parameters.responseFormat(), strictJsonSchema)).maxCompletionTokens(parameters.maxCompletionTokens()).logitBias(parameters.logitBias()).parallelToolCalls(parameters.parallelToolCalls()).seed(parameters.seed()).user(parameters.user()).store(parameters.store()).metadata(parameters.metadata()).serviceTier(parameters.serviceTier()).reasoningEffort(parameters.reasoningEffort());
    }

    public static List<Message> toOpenAiMessages(List<ChatMessage> messages) {
        return (List)messages.stream().map(OpenAiChatModelWithToolMessage::toOpenAiMessage).collect(Collectors.toList());
    }

    static dev.langchain4j.model.openai.internal.chat.ResponseFormat toOpenAiResponseFormat(dev.langchain4j.model.chat.request.ResponseFormat responseFormat, Boolean strict) {
        if (responseFormat != null && responseFormat.type() != ResponseFormatType.TEXT) {
            JsonSchema jsonSchema = responseFormat.jsonSchema();
            if (jsonSchema == null) {
                return dev.langchain4j.model.openai.internal.chat.ResponseFormat.builder().type(dev.langchain4j.model.openai.internal.chat.ResponseFormatType.JSON_OBJECT).build();
            } else if (!(jsonSchema.rootElement() instanceof JsonObjectSchema)) {
                throw new IllegalArgumentException("For OpenAI, the root element of the JSON Schema must be a JsonObjectSchema, but it was: " + String.valueOf(jsonSchema.rootElement().getClass()));
            } else {
                dev.langchain4j.model.openai.internal.chat.JsonSchema openAiJsonSchema = dev.langchain4j.model.openai.internal.chat.JsonSchema.builder().name(jsonSchema.name()).strict(strict).schema(JsonSchemaElementHelper.toMap(jsonSchema.rootElement(), strict)).build();
                return dev.langchain4j.model.openai.internal.chat.ResponseFormat.builder().type(dev.langchain4j.model.openai.internal.chat.ResponseFormatType.JSON_SCHEMA).jsonSchema(openAiJsonSchema).build();
            }
        } else {
            return null;
        }
    }

    public static Message toOpenAiMessage(ChatMessage message) {
        if (message instanceof SystemMessage) {
            return dev.langchain4j.model.openai.internal.chat.SystemMessage.from(((SystemMessage)message).text());
        } else if (message instanceof dev.langchain4j.example.entity.agent.message_manager._service.ToolMessage) {
            //TODO
            return null;
        } else if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage)message;
            return userMessage.hasSingleText() ? dev.langchain4j.model.openai.internal.chat.UserMessage.builder().content(userMessage.singleText()).name(userMessage.name()).build() : dev.langchain4j.model.openai.internal.chat.UserMessage.builder().content((List)userMessage.contents().stream().map(OpenAiChatModelWithToolMessage::toOpenAiContent).collect(Collectors.toList())).name(userMessage.name()).build();
        } else if (message instanceof AiMessage) {
            AiMessage aiMessage = (AiMessage)message;
            if (!aiMessage.hasToolExecutionRequests()) {
                return AssistantMessage.from(aiMessage.text());
            } else {
                ToolExecutionRequest toolExecutionRequest = (ToolExecutionRequest)aiMessage.toolExecutionRequests().get(0);
                if (toolExecutionRequest.id() == null) {
                    FunctionCall functionCall = FunctionCall.builder().name(toolExecutionRequest.name()).arguments(toolExecutionRequest.arguments()).build();
                    return AssistantMessage.builder().functionCall(functionCall).build();
                } else {
                    List<ToolCall> toolCalls = (List)aiMessage.toolExecutionRequests().stream().map((it) -> ToolCall.builder().id(it.id()).type(ToolType.FUNCTION).function(FunctionCall.builder().name(it.name()).arguments(it.arguments()).build()).build()).collect(Collectors.toList());
                    return AssistantMessage.builder().content(aiMessage.text()).toolCalls(toolCalls).build();
                }
            }
        } else if (message instanceof ToolExecutionResultMessage) {
            ToolExecutionResultMessage toolExecutionResultMessage = (ToolExecutionResultMessage)message;
            return (Message)(toolExecutionResultMessage.id() == null ? FunctionMessage.from(toolExecutionResultMessage.toolName(), toolExecutionResultMessage.text()) : ToolMessage.from(toolExecutionResultMessage.id(), toolExecutionResultMessage.text()));
        } else {
            throw Exceptions.illegalArgument("Unknown message type: " + String.valueOf(message.type()), new Object[0]);
        }
    }

    private static Content toOpenAiContent(dev.langchain4j.data.message.Content content) {
        if (content instanceof TextContent) {
            return toOpenAiContent((TextContent)content);
        } else if (content instanceof ImageContent) {
            return toOpenAiContent((ImageContent)content);
        } else if (content instanceof AudioContent) {
            AudioContent audioContent = (AudioContent)content;
            return toOpenAiContent(audioContent);
        } else {
            throw Exceptions.illegalArgument("Unknown content type: " + String.valueOf(content), new Object[0]);
        }
    }

    public ChatResponse doChat(ChatRequest chatRequest) {
        OpenAiChatRequestParameters parameters = (OpenAiChatRequestParameters)chatRequest.parameters();
        validate(parameters);
        ChatCompletionRequest openAiRequest = toOpenAiChatRequest(chatRequest, parameters, this.strictTools, this.strictJsonSchema).build();
        ChatCompletionResponse openAiResponse = (ChatCompletionResponse)RetryUtils.withRetryMappingExceptions(() -> (ChatCompletionResponse)this.client.chatCompletion(openAiRequest).execute(), this.maxRetries);
        OpenAiChatResponseMetadata responseMetadata = ((OpenAiChatResponseMetadata.Builder)((OpenAiChatResponseMetadata.Builder)((OpenAiChatResponseMetadata.Builder)((OpenAiChatResponseMetadata.Builder)OpenAiChatResponseMetadata.builder().id(openAiResponse.id())).modelName(openAiResponse.model())).tokenUsage(InternalOpenAiHelper.tokenUsageFrom(openAiResponse.usage()))).finishReason(InternalOpenAiHelper.finishReasonFrom(((ChatCompletionChoice)openAiResponse.choices().get(0)).finishReason()))).created(openAiResponse.created()).serviceTier(openAiResponse.serviceTier()).systemFingerprint(openAiResponse.systemFingerprint()).build();
        return ChatResponse.builder().aiMessage(InternalOpenAiHelper.aiMessageFrom(openAiResponse)).metadata(responseMetadata).build();
    }

    public List<ChatModelListener> listeners() {
        return this.listeners;
    }

    public ModelProvider provider() {
        return ModelProvider.OPEN_AI;
    }

    public static OpenAiChatModelWithToolMessageBuilder builder() {
        Iterator var0 = ServiceHelper.loadFactories(OpenAiChatModelWithToolMessageBuilderFactory.class).iterator();
        if (var0.hasNext()) {
            OpenAiChatModelWithToolMessageBuilderFactory factory = (OpenAiChatModelWithToolMessageBuilderFactory)var0.next();
            return (OpenAiChatModelWithToolMessageBuilder)factory.get();
        } else {
            return new OpenAiChatModelWithToolMessageBuilder();
        }
    }

    public static class OpenAiChatModelWithToolMessageBuilder {
        private HttpClientBuilder httpClientBuilder;
        private String baseUrl;
        private String apiKey;
        private String organizationId;
        private String projectId;
        private ChatRequestParameters defaultRequestParameters;
        private String modelName;
        private Double temperature;
        private Double topP;
        private List<String> stop;
        private Integer maxTokens;
        private Integer maxCompletionTokens;
        private Double presencePenalty;
        private Double frequencyPenalty;
        private Map<String, Integer> logitBias;
        private Set<Capability> supportedCapabilities;
        private String responseFormat;
        private Boolean strictJsonSchema;
        private Integer seed;
        private String user;
        private Boolean strictTools;
        private Boolean parallelToolCalls;
        private Boolean store;
        private Map<String, String> metadata;
        private String serviceTier;
        private Duration timeout;
        private Integer maxRetries;
        private Boolean logRequests;
        private Boolean logResponses;
        private Map<String, String> customHeaders;
        private List<ChatModelListener> listeners;

        public OpenAiChatModelWithToolMessageBuilder httpClientBuilder(HttpClientBuilder httpClientBuilder) {
            this.httpClientBuilder = httpClientBuilder;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder defaultRequestParameters(ChatRequestParameters parameters) {
            this.defaultRequestParameters = parameters;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder modelName(OpenAiChatModelName modelName) {
            this.modelName = modelName.toString();
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder organizationId(String organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder stop(List<String> stop) {
            this.stop = stop;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder maxCompletionTokens(Integer maxCompletionTokens) {
            this.maxCompletionTokens = maxCompletionTokens;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder logitBias(Map<String, Integer> logitBias) {
            this.logitBias = logitBias;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder responseFormat(String responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder supportedCapabilities(Set<Capability> supportedCapabilities) {
            this.supportedCapabilities = supportedCapabilities;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder supportedCapabilities(Capability... supportedCapabilities) {
            return this.supportedCapabilities(new HashSet(Arrays.asList(supportedCapabilities)));
        }

        public OpenAiChatModelWithToolMessageBuilder strictJsonSchema(Boolean strictJsonSchema) {
            this.strictJsonSchema = strictJsonSchema;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder seed(Integer seed) {
            this.seed = seed;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder user(String user) {
            this.user = user;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder strictTools(Boolean strictTools) {
            this.strictTools = strictTools;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder parallelToolCalls(Boolean parallelToolCalls) {
            this.parallelToolCalls = parallelToolCalls;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder store(Boolean store) {
            this.store = store;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder serviceTier(String serviceTier) {
            this.serviceTier = serviceTier;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder logRequests(Boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder logResponses(Boolean logResponses) {
            this.logResponses = logResponses;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder customHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        public OpenAiChatModelWithToolMessageBuilder listeners(List<ChatModelListener> listeners) {
            this.listeners = listeners;
            return this;
        }

        public OpenAiChatModelWithToolMessage build() {
            return new OpenAiChatModelWithToolMessage(this);
        }
    }
}

