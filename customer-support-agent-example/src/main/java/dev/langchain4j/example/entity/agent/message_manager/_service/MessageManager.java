package dev.langchain4j.example.entity.agent.message_manager._service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
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
import dev.langchain4j.model.openai.internal.chat.ToolMessage;
import lombok.Data;


import java.util.ArrayList;
import java.util.List;

@Data
public class MessageManager {
    private String task;
    private final MessageManagerSettings settings;
    private final MessageManagerState state;
    private final SystemMessage systemPrompt;

    public MessageManager(String task, SystemMessage systemMessage,
                          MessageManagerSettings settings,
                          MessageManagerState state) {
        this.task = task;
        this.settings = settings != null ? settings : MessageManagerSettings.builder().build();
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

        JSONArray actionSample = JSONUtil.createArray();
        actionSample.add(JSONUtil.createObj().set("click_element", JSONUtil.createObj().set("index", 127)));
        JSONObject args = JSONUtil.createObj().set("current_state", JSONUtil.createObj()
                        .set("evaluation_previous_goal", """
                                Success - I successfully clicked on the 'Apple' link from the Google Search results page,
                                which directed me to the 'Apple' company homepage. This is a good start toward finding
                                the best place to buy a new iPhone as the Apple website often list iPhones for sale.
                                """.trim())
                        .set("memory", """
                                I searched for 'iPhone retailers' on Google. From the Google Search results page,
                                I used the 'click_element' tool to click on a element labelled 'Best Buy' but calling
                                the tool did not direct me to a new page. I then used the 'click_element' tool to click
                                on a element labelled 'Apple' which redirected me to the 'Apple' company homepage.
                                Currently at step 3/15.
                                """.trim())
                        .set("next_goal", """
                                Looking at reported structure of the current page, I can see the item '[127]<h3 iPhone/>'\s
                                in the content. I think this button will lead to more information and potentially prices\s
                                for iPhones. I'll click on the link to 'iPhone' at index [127] using the 'click_element'\s
                                tool and hope to see prices on the next page.
                                """.trim()))
                .set("action", actionSample);

        var toolExecutionRequests = new ArrayList<ToolExecutionRequest>();
        var request = ToolExecutionRequest.builder()
            .id(String.valueOf(this.state.getToolId()))
            .name("AgentOutput")
            .arguments(args.toString()).build();
        toolExecutionRequests.add(request);

        AiMessage exampleToolCall = new AiMessage("", toolExecutionRequests);
        addMessageWithTokens(exampleToolCall, null, "init");
        addToolMessage("Browser started", "init");

        placeholderMessage = new UserMessage("[Your task history memory starts here]");
        addMessageWithTokens(placeholderMessage, null, null);


        if (!CollUtil.isEmpty(settings.getAvailableFilePaths())) {
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

    public void removeLastStateMessage() {
        this.state.getHistory().removeLastStateMessage();
    }

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
        var toolExecutionRequests = new ArrayList<ToolExecutionRequest>();
        var request = ToolExecutionRequest.builder()
                .id(String.valueOf(this.state.getToolId()))
                .name("AgentOutput")
                .arguments(JSONUtil.toJsonStr(modelOutput)).build();
        toolExecutionRequests.add(request);

        AiMessage exampleToolCall = new AiMessage("", toolExecutionRequests);
        addMessageWithTokens(exampleToolCall, null, null);
        addToolMessage("", null);
    }

    public void addPlan(String plan, Integer position) {
        if (plan != null) {
            addMessageWithTokens(new AiMessage(plan), position, null);
        }
    }

    public List<ChatMessage> getMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        for (ManagedMessage m : state.getHistory().getMessages()) {
            messages.add(m.getMessage());
        }
        return messages;
    }

    public void addMessageWithTokens(ChatMessage message, Integer position, String messageType) {
        if (settings.getSensitiveData() != null) {
            message = filterSensitiveData(message);
        }
        int tokenCount = countTokens(message);
        MessageMetadata metadata = new MessageMetadata(tokenCount, messageType);
        state.getHistory().addMessage(message, metadata, position);
    }

    private ChatMessage filterSensitiveData(ChatMessage message) {
        // Implementation of sensitive data filtering
        return message;
    }

    public int countTokens(ChatMessage message) {
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
//        ToolMessage msg = ToolMessage.from(String.valueOf(this.state.getToolId()), content);
//        this.state.setToolId(this.state.getToolId() + 1);
//        this.addMessageWithTokens(msg, null, messageType);
    }
}
