package dev.langchain4j.example.entity.agent.message_manager._service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.example.entity.agent._prompts.AgentMessagePrompt;
import dev.langchain4j.example.entity.agent._views.ActionResult;
import dev.langchain4j.example.entity.agent._views.AgentOutput;
import dev.langchain4j.example.entity.agent._views.AgentStepInfo;
import dev.langchain4j.example.entity.agent.message_manager._views.ManagedMessage;
import dev.langchain4j.example.entity.agent.message_manager._views.MessageManagerState;
import dev.langchain4j.example.entity.agent.message_manager._views.MessageMetadata;
import dev.langchain4j.example.entity.browser._views.BrowserState;


import java.util.ArrayList;
import java.util.List;

public class MessageManager {
    private String task;
    private final MessageManagerSettings settings;
    private final MessageManagerState state;
    private final SystemMessage systemPrompt;

    public MessageManager(String task, SystemMessage systemMessage,
                          MessageManagerSettings settings,
                          MessageManagerState state) {
        this.task = task;
        this.settings = settings != null ? settings : new MessageManagerSettings();
        this.state = state != null ? state : new MessageManagerState();
        this.systemPrompt = systemMessage;

        if (this.state.getHistory().getMessages().isEmpty()) {
            initMessages();
        }
    }

    private void initMessages() {
        addMessageWithTokens(systemPrompt, null,"init");

        if (settings.getMessageContext() != null) {
            UserMessage contextMessage = new UserMessage(
                    "Context for the task" + settings.getMessageContext());
            addMessageWithTokens(contextMessage, null,"init");
        }

        UserMessage taskMessage = new UserMessage(
                "Your ultimate task is: \"\"\"" + task + "\"\"\". If you achieved your ultimate task, " +
                        "stop everything and use the done action in the next step to complete the task. " +
                        "If not, continue as usual.");
        addMessageWithTokens(taskMessage, null,"init");

        if (settings.getSensitiveData() != null) {
            String info = "Here are placeholders for sensitive data: " +
                    String.join(", ", settings.getSensitiveData().keySet());
            info += "To use them, write <secret>the placeholder name</secret>";
            UserMessage infoMessage = new UserMessage(info);
            addMessageWithTokens(infoMessage, null,"init");
        }

        UserMessage placeholderMessage = new UserMessage("Example output:");
        addMessageWithTokens(placeholderMessage, null,"init");

        // Example tool call would be implemented similarly
        // ...

        if (settings.getAvailableFilePaths() != null) {
            UserMessage filepathsMsg = new UserMessage(
                    "Here are file paths you can use: " + String.join(", ", settings.getAvailableFilePaths()));
            addMessageWithTokens(filepathsMsg, null,"init");
        }
    }

    public void addNewTask(String newTask) {
        String content = "Your new ultimate task is: \"\"\"" + newTask + "\"\"\". " +
                "Take the previous context into account and finish your new ultimate task.";
        UserMessage msg = new UserMessage(content);
        addMessageWithTokens(msg, null,null);
        this.task = newTask;
    }

    @TimedExecution("--add_state_message")
    public void addStateMessage(BrowserState state, List<ActionResult> result,
                                AgentStepInfo stepInfo, boolean useVision) {
        if (result != null) {
            for (ActionResult r : result) {
                if (r.isIncludeInMemory()) {
                    if (r.getExtractedContent() != null) {
                        UserMessage msg = new UserMessage("Action result: " + r.getExtractedContent());
                        addMessageWithTokens(msg,null,null);
                    }
                    if (r.getError() != null) {
                        String error = r.getError();
                        if (error.endsWith("\n")) {
                            error = error.substring(0, error.length() - 1);
                        }
                        String lastLine = error.split("\n")[error.split("\n").length - 1];
                        UserMessage msg = new UserMessage("Action error: " + lastLine);
                        addMessageWithTokens(msg, null, null);
                    }
                    result = null;
                }
            }
        }

        AgentMessagePrompt stateMessage = new AgentMessagePrompt(
                state, result, settings.getIncludeAttributes(), stepInfo);
        UserMessage userMessage = stateMessage.getUserMessage(useVision);
        addMessageWithTokens(userMessage, null, null);
    }

    public void addModelOutput(AgentOutput modelOutput) {
        // Tool calls implementation would go here
        // ...
        addMessageWithTokens(new AiMessage("", toolCalls));
        addToolMessage("");
    }

    public void addPlan(String plan, Integer position) {
        if (plan != null) {
            addMessageWithTokens(new AiMessage(plan), position, null);
        }
    }

    @TimedExecution("--get_messages")
    public List<ChatMessage> getMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        for (ManagedMessage m : state.getHistory().getMessages()) {
            messages.add(m.getMessage());
        }
        return messages;
    }

    private void addMessageWithTokens(ChatMessage message, Integer position, String messageType) {
        if (settings.getSensitiveData() != null) {
            message = filterSensitiveData(message);
        }
        int tokenCount = countTokens(message);
        MessageMetadata metadata = new MessageMetadata(tokenCount, messageType);
        state.getHistory().addMessage(message, metadata, position);
    }

    @TimedExecution("--filter_sensitive_data")
    private ChatMessage filterSensitiveData(ChatMessage message) {
        // Implementation of sensitive data filtering
        return message;
    }

    private int countTokens(ChatMessage message) {
        // Implementation of token counting
        return 0;
    }

    private int countTextTokens(String text) {
        return text.length() / settings.getEstimatedCharactersPerToken();
    }

    public void cutMessages() {
        // Implementation of message cutting
    }

    public void addToolMessage(String content, String messageType) {
        // Implementation of tool message addition
    }
}
