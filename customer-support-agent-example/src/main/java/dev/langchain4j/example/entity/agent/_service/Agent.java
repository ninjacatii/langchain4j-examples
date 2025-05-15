package dev.langchain4j.example.entity.agent._service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.microsoft.playwright.Page;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.example.entity.agent._prompts.AgentMessagePrompt;
import dev.langchain4j.example.entity.agent._prompts.PlannerPrompt;
import dev.langchain4j.example.entity.agent._prompts.SystemPrompt;
import dev.langchain4j.example.entity.agent._views.*;
import dev.langchain4j.example.entity.agent.message_manager.Utils;
import dev.langchain4j.example.entity.agent.message_manager._service.MessageManager;
import dev.langchain4j.example.entity.agent.message_manager._service.MessageManagerSettings;
import dev.langchain4j.example.entity.browser._context.BrowserContext;
import dev.langchain4j.example.entity.browser._views.BrowserState;
import dev.langchain4j.example.entity.browser._views.BrowserStateHistory;
import dev.langchain4j.example.entity.controller.registry._service.Controller;
import dev.langchain4j.example.entity.controller.registry._views.ActionModel;
import dev.langchain4j.example.entity.controller.registry._views.RegisteredAction;
import dev.langchain4j.example.entity.dom._views.DOMElementNode;
import dev.langchain4j.example.entity.dom.history_tree_processor._service.HistoryTreeProcessor;
import dev.langchain4j.example.entity.dom.history_tree_processor._view.DOMHistoryElement;
import dev.langchain4j.example.entity.memory._service.Memory;
import dev.langchain4j.example.entity.memory._service.MemorySettings;
import dev.langchain4j.example.exception.LLMException;
import dev.langchain4j.example.iface.NewStepCallbackFunction;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.example.entity.browser._browser.Browser;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.*;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.util.Tuple;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class Agent<T> {
    private String task;
    private ChatLanguageModel llm;
    private Controller<T> controller;
    private Map<String, String> sensitiveData;
    private AgentSettings settings;
    private AgentState state;
    private MessageManager messageManager;
    private Memory memory;
    private Browser browser;
    private BrowserContext browserContext;
    private String modelName;
    private String plannerModelName;
    private String chatModelLibrary;
    private String version;
    private String source;
    private T context;
    private String unfilteredActions;
    private ToolCallingMethod toolCallingMethod;
    private List<ActionModel> initialActions;
    private boolean injectedBrowser;
    private boolean injectedBrowserContext;
    private NewStepCallbackFunction registerNewStepCallback;
    private Consumer<AgentHistoryList> registerDoneCallback;
    private Supplier<Boolean> registerExternalAgentStatusRaiseErrorCallback;
    private ActionModel actionModel;
    private AgentOutput agentOutput;
    private ActionModel doneActionModel;
    private AgentOutput doneActionOutput;
    private Runnable verificationTask;

    public Agent(String task,
                 ChatLanguageModel llm,
                 BrowserContext browserContext) {
        this(task, llm, null, browserContext, new Controller(null), null, null, null, null, null,
                AgentSettings.builder()
                        .useVision(false)
                        .useVisionForPlanner(false)
                        .saveConversationPath(null)
                        .saveConversationPathEncoding("utf-8")
                        .maxFailures(3)
                        .retryDelay(10)
                        .overrideSystemMessage(null)
                        .extendSystemMessage(null)
                        .maxInputTokens(128000)
                        .validateOutput(false)
                        .messageContext(null)
                        .generateGif(false)
                        .availableFilePaths(null)
                        .includeAttributes(Arrays.asList(
                                "title",
                                "type",
                                "name",
                                "role",
                                "aria-label",
                                "placeholder",
                                "value",
                                "alt",
                                "aria-expanded",
                                "data-date-format"
                        ))
                        .maxActionsPerStep(10)
                        .toolCallingMethod("auto")
                        .pageExtractionLlm(null)
                        .plannerLlm(null)
                        .plannerInterval(1)
                        .isPlannerReasoning(false)
                        .build(),
                null,
                null,
                false,
                10,
                null
        );
    }

    public Agent(
            String task,
            ChatLanguageModel llm,
            // Optional parameters
            Browser browser,
            BrowserContext browserContext,
            Controller<T> controller,
            // Initial agent run parameters
            Map<String, String> sensitiveData,
            List<HashMap<String, HashMap<String, Object>>> initialActions,
            // Cloud Callbacks
            NewStepCallbackFunction registerNewStepCallback,
            Consumer<AgentHistoryList> registerDoneCallback,
            Supplier<Boolean> registerExternalAgentStatusRaiseErrorCallback,
            // Agent settings
            AgentSettings agentSettings,
            // Inject state
            AgentState injectedAgentState,
            //
            T context,
            // Memory settings
            boolean enableMemory,
            int memoryInterval,
            Hashtable<Object, Object> memoryConfig) {
        if (agentSettings.getPageExtractionLlm() == null) {
            agentSettings.setPageExtractionLlm(llm);
        }

        this.task = task;
        this.llm = llm;
        this.controller = controller;
        this.sensitiveData = sensitiveData;

        this.settings = agentSettings;

        this.state = injectedAgentState != null ? injectedAgentState : new AgentState();

        this.setupActionModels();
        this.setBrowserUseVersionAndSource();
        this.initialActions = initialActions != null ? this.convertInitialActions(initialActions) : null;

        this.setModelNames();
        this.toolCallingMethod = this.setToolCallingMethod();

        this.unfilteredActions = this.controller.getRegistry().getPromptDescription(null);

        this.settings.setMessageContext(this.setMessageContext());

        this.messageManager = new MessageManager(
                task,
                new SystemPrompt(
                    this.unfilteredActions,
                    this.settings.getMaxActionsPerStep(),
                    this.settings.getOverrideSystemMessage(),
                    this.settings.getExtendSystemMessage()).getSystemMessage(),
                MessageManagerSettings.builder()
                        .maxInputTokens(this.settings.getMaxInputTokens())
                        .includeAttributes(this.settings.getIncludeAttributes())
                        .messageContext(this.settings.getMessageContext())
                        .sensitiveData(sensitiveData)
                        .availableFilePaths(this.settings.getAvailableFilePaths())
                        .build(),
                this.state.getMessageManagerState());

        if (this.settings.isEnableMemory()) {
            MemorySettings memorySettings = new MemorySettings(
                    this.state.getAgentId(),
                    this.settings.getMemoryInterval(),
                    this.settings.getMemoryConfig()
            );

            this.memory = new Memory(this.messageManager, this.llm, memorySettings);
        } else {
            this.memory = null;
        }

        this.injectedBrowser = browser != null;
        this.injectedBrowserContext = browserContext != null;
        this.browser = browser != null ? browser : new Browser(null);
        this.browserContext = browserContext != null ? browserContext :
                new BrowserContext(this.browser, this.browser.getConfig().getNewContextConfig(), null);

        this.registerNewStepCallback = registerNewStepCallback;
        this.registerDoneCallback = registerDoneCallback;
        this.registerExternalAgentStatusRaiseErrorCallback = registerExternalAgentStatusRaiseErrorCallback;

        this.context = context;

        if (StrUtil.isNotBlank(this.settings.getSaveConversationPath())) {
            log.info("Saving conversation to {}", this.settings.getSaveConversationPath());
        }
    }

    private String setMessageContext() {
        if (this.toolCallingMethod == ToolCallingMethod.RAW) {
            if (StrUtil.isNotBlank(this.settings.getMessageContext())) {
                this.settings.setMessageContext(this.settings.getMessageContext() + "\n\nAvailable actions: " + this.unfilteredActions);
            } else {
                this.settings.setMessageContext("Available actions: " + this.unfilteredActions);
            }
        }
        return this.settings.getMessageContext();
    }

    private void setBrowserUseVersionAndSource() {
        //TODO:no use

//        """Get the version and source of the browser-use package (git or pip in a nutshell)"""
//        try:
//			# First check for repository-specific files
//        repo_files = ['.git', 'README.md', 'docs', 'examples']
//        package_root = Path(__file__).parent.parent.parent
//
//			# If all of these files/dirs exist, it's likely from git
//        if all(Path(package_root / file).exists() for file in repo_files):
//        try:
//					import subprocess
//
//                version = subprocess.check_output(['git', 'describe', '--tags']).decode('utf-8').strip()
//        except Exception:
//        version = 'unknown'
//        source = 'git'
//			else:
//				# If no repo files found, try getting version from pip
//				import pkg_resources
//
//                version = pkg_resources.get_distribution('browser-use').version
//        source = 'pip'
//        except Exception:
//        version = 'unknown'
//        source = 'unknown'
//
//        logger.debug(f'Version: {version}, Source: {source}')
//        self.version = version
//        self.source = source
    }

    private void setModelNames() {
        //TODO:no use

//        self.chat_model_library = self.llm.__class__.__name__
//        self.model_name = 'Unknown'
//        if hasattr(self.llm, 'model_name'):
//        model = self.llm.model_name  # type: ignore
//        self.model_name = model if model is not None else 'Unknown'
//        elif hasattr(self.llm, 'model'):
//        model = self.llm.model  # type: ignore
//        self.model_name = model if model is not None else 'Unknown'
//
//        if self.settings.planner_llm:
//        if hasattr(self.settings.planner_llm, 'model_name'):
//        self.planner_model_name = self.settings.planner_llm.model_name  # type: ignore
//        elif hasattr(self.settings.planner_llm, 'model'):
//        self.planner_model_name = self.settings.planner_llm.model  # type: ignore
//			else:
//        self.planner_model_name = 'Unknown'
//		else:
//        self.planner_model_name = None
    }

    private void setupActionModels() {
        //TODO:no use

//        """Setup dynamic action models from controller's registry"""
//		# Initially only include actions with no filters
//        self.ActionModel = self.controller.registry.create_action_model()
//		# Create output model with the dynamic actions
//        self.AgentOutput = AgentOutput.type_with_custom_actions(self.ActionModel)
//
//		# used to force the done action when max_steps is reached
//        self.DoneActionModel = self.controller.registry.create_action_model(include_actions=['done'])
//        self.DoneAgentOutput = AgentOutput.type_with_custom_actions(self.DoneActionModel)
    }

    private ToolCallingMethod setToolCallingMethod() {
        return toolCallingMethod;
    }

    public void addNewTask(String newTask) {
        this.messageManager.addNewTask(newTask);
    }

    private void raiseIfStoppedOrPaused() {
        //TODO:no use

//        """Utility function that raises an InterruptedError if the agent is stopped or paused."""
//
//        if self.register_external_agent_status_raise_error_callback:
//        if await self.register_external_agent_status_raise_error_callback():
//        raise InterruptedError
//
//        if self.state.stopped or self.state.paused:
//			# logger.debug('Agent paused after getting state')
//        raise InterruptedError
    }

    public void step(AgentStepInfo stepInfo) {
        log.info("📍 Step {}", this.state.getNSteps());
        BrowserState state = null;
        AgentOutput modelOutput = null;
        List<ActionResult> result = null;
        DateTime stepStartTime = DateUtil.date();
        int tokens = 0;

        try {
            state = this.browserContext.getState(true);
            Page activePage = this.browserContext.getCurrentPage();

            if (this.settings.isEnableMemory() && this.memory != null && this.state.getNSteps() % this.settings.getMemoryInterval() == 0) {
                this.memory.createProceduralMemory(this.state.getNSteps());
            }

            this.raiseIfStoppedOrPaused();

            //this.updateActionModelsForPage(activePage);

            String pageFilteredActions = this.controller.getRegistry().getPromptDescription(activePage);

            if (StrUtil.isNotBlank(pageFilteredActions)) {
                String pageActionMessage = "For this page, these additional actions are available:\n" + pageFilteredActions;
                this.messageManager.addMessageWithTokens(new UserMessage(pageActionMessage), null, null);
            }

            if (this.toolCallingMethod == ToolCallingMethod.RAW) {
                String allUnfilteredActions = this.controller.getRegistry().getPromptDescription(null);
                String allActions = allUnfilteredActions;
                if (StrUtil.isNotBlank(pageFilteredActions)) {
                    allActions += "\n" + pageFilteredActions;
                }

                String messageContext = this.messageManager.getSettings().getMessageContext();
                String[] contextLines = (StrUtil.isBlank(messageContext) ? "" : messageContext).split("\n");
                var nonActionLines = new ArrayList<String>();
                for (String line: contextLines) {
                    if (!line.startsWith("Available actions:")) {
                        nonActionLines.add(line);
                    }
                }
                String updatedContext =StrUtil.join("\n", nonActionLines);
                if (StrUtil.isNotBlank(updatedContext)) {
                    updatedContext += "\n\nAvailable actions: " + allActions;
                } else {
                    updatedContext = "Available actions: " + allActions;
                }
                this.messageManager.getSettings().setMessageContext(updatedContext);
            }
            this.messageManager.addStateMessage(state, this.state.getLastResult(), stepInfo, this.settings.isUseVision());

            if (this.settings.getPlannerLlm() != null && this.state.getNSteps() % this.settings.getPlannerInterval() == 0) {
                String plan = this.runPlanner();
                this.messageManager.addPlan(plan, -1);
            }

            if (stepInfo != null && stepInfo.isLastStep()) {
                String msg = "Now comes your last step. Use only the \"done\" action now. No other actions - so here your action sequence must have length 1.";
                msg += "\nIf the task is not yet fully finished as requested by the user, set success in \"done\" to false! E.g. if not all steps are fully completed.";
                msg += "\nIf the task is fully finished, set success in \"done\" to true.";
                msg += "\nInclude everything you found out for the ultimate task in the done text.";
                log.info("Last step finishing up");
                this.messageManager.addMessageWithTokens(new UserMessage(msg), null, null);
//                this.AgentOutput = this.DoneAgentOutput;
            }

            List<ChatMessage> inputMessages = this.messageManager.getMessages();
            tokens = this.messageManager.getState().getHistory().getCurrentTokens();

            try {
                modelOutput = this.getNextAction(inputMessages);

                this.raiseIfStoppedOrPaused();

                this.state.setNSteps(this.state.getNSteps() + 1);

                if (this.registerNewStepCallback != null) {
                    this.registerNewStepCallback.apply(state, modelOutput, this.state.getNSteps());
                }
                if (StrUtil.isNotBlank(this.settings.getSaveConversationPath())) {
                    String target = this.settings.getSaveConversationPath() + "_" + this.state.getNSteps() + ".txt";
                    //saveConversation(inputMessages, modelOutput, target, this.settings.getSaveConversationPathEncoding());
                }
                this.messageManager.removeLastStateMessage();

                this.raiseIfStoppedOrPaused();

                this.messageManager.addModelOutput(modelOutput);
            } catch (Exception e) {
                this.messageManager.removeLastStateMessage();
                throw e;
            }

            result = this.multiAct(modelOutput.getAction(), true);

            this.state.setLastResult(result);

            if (!result.isEmpty() && result.get(result.size() - 1).isDone()) {
                log.info("📄 Result: " + result.get(result.size() - 1).getExtractedContent());
            }

            this.state.setConsecutiveFailures(0);
        } catch (Exception e) {
            log.error("step error:{}", e.getMessage(), e);
            result = this.handleStepError(e);
            this.state.setLastResult(result);
        } finally {
            DateTime stepEndTime = DateUtil.date();
            if (result == null) {
                return;
            }
            if (state != null) {
                StepMetadata metadata = new StepMetadata(
                        this.state.getNSteps(),
                        stepStartTime,
                        stepEndTime,
                        tokens
                );
                this.makeHistoryItem(modelOutput, state, result, metadata);
            }
        }
    }

    private List<ActionResult> handleStepError(Exception e) {
        String errorMsg = AgentError.formatError(e, false);
        String prefix = "❌ Result failed {self.state.consecutive_failures + 1}/{self.settings.max_failures} times:\n ";
        this.state.setConsecutiveFailures(this.state.getConsecutiveFailures() + 1);

        if (errorMsg.indexOf("Browser closed") >= 0) {
            log.error("❌  Browser is closed or disconnected, unable to proceed");
            return Collections.singletonList(ActionResult.builder().error("Browser closed or disconnected, unable to proceed").includeInMemory(false).build());
        }
        return Collections.singletonList(ActionResult.builder().error(errorMsg).includeInMemory(true).build());
    }

    private void makeHistoryItem(AgentOutput modelOutput, BrowserState state, List<ActionResult> result, StepMetadata metadata) {
        List<DOMHistoryElement> interactedElements = null;
        if (modelOutput != null) {
            interactedElements = AgentHistory.getInteractedElement(modelOutput, state.getSelectorMap());
        } else {
            interactedElements = new ArrayList<>();
        }

        BrowserStateHistory stateHistory = new BrowserStateHistory(
                state.getUrl(),
                state.getTitle(),
                state.getTabs(),
                interactedElements,
                state.getScreenshot()
        );

        AgentHistory historyItem = new AgentHistory(modelOutput, result, stateHistory, metadata);
        this.state.getHistory().getHistory().add(historyItem);
    }

    private static final Pattern THINK_TAGS = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
    private static final Pattern STRAY_CLOSE_TAG = Pattern.compile(".*?</think>", Pattern.DOTALL);
    private String removeThinkTags(String text) {
        // 第一步：移除完整的 <think>...</think> 标签及内容
        text = THINK_TAGS.matcher(text).replaceAll("");
        // 第二步：移除残留的 </think> 及其前面的所有内容
        text = STRAY_CLOSE_TAG.matcher(text).replaceAll("");
        // 去除首尾空白字符
        return text.trim();
    }

    private List<ChatMessage> convertInputMessages(List<ChatMessage> inputMessages) {
        return inputMessages;
    }

    private ResponseFormat getAgentOutputFormat() {
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchema.builder()
                        .name("Response")
                        .rootElement(JsonObjectSchema.builder()
                                .addProperty("currentState", JsonObjectSchema.builder()
                                        .addStringProperty("evaluationPreviousGoal")
                                        .addStringProperty("memory")
                                        .addStringProperty("nextGoal")
                                        .required("evaluationPreviousGoal", "memory", "nextGoal")
                                        .build())
                                .addProperty("action", JsonArraySchema.builder()
                                        .items(JsonObjectSchema.builder().build()).build())
                                .build()).build()
                ).build();
        return responseFormat;
    }

    public static String capitalizeFirstLetter(String str) {
        if (StrUtil.isBlank(str)) {
            return str; // 处理空或null字符串[1,3](@ref)
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    public static String convertToUpperCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input; // 处理空值或空字符串
        }
        Matcher matcher = Pattern.compile("_([a-z])").matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        // 首字母大写处理
        return Character.toUpperCase(sb.charAt(0)) + sb.substring(1);
    }

    private JsonObjectSchema getParaJsonObjectScheme(RegisteredAction action) throws Exception {
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder();
        var required = new ArrayList<String>();
        for (int i = 0; i < action.paraType().length; i++) {
            required.add(action.paraName()[i]);
            builder = switch (action.paraType()[i].getName()) {
                case "java.lang.String" ->
                        builder.addStringProperty(action.paraName()[i], capitalizeFirstLetter(action.paraName()[i]));
                case "java.lang.Integer" ->
                        builder.addIntegerProperty(action.paraName()[i], capitalizeFirstLetter(action.paraName()[i]));
                case "java.lang.Boolean" ->
                        builder.addBooleanProperty(action.paraName()[i], capitalizeFirstLetter(action.paraName()[i]));
                case "java.lang.Float", "java.lang.Double" ->
                        builder.addNumberProperty(action.paraName()[i], capitalizeFirstLetter(action.paraName()[i]));
                default -> throw new Exception("Unknown class name:" + action.paraType()[i].getName());
            };
        }

        JsonObjectSchema result = builder
                .required(required)
                .description(convertToUpperCamelCase(action.name()) + "Action")
                .build();

        return result;
    }

    private JsonObjectSchema getActionJsonObjectSchema() throws Exception {
        Map<String, RegisteredAction> map = this.controller.getRegistry().getRegistry().getActions();
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder();
        for (String key: map.keySet()) {
            RegisteredAction action = map.get(key);
            builder = builder.addProperty(key, JsonAnyOfSchema.builder()
                    .anyOf(getParaJsonObjectScheme(action), new JsonNullSchema())
                    .description(action.description())
                    .build());
        }
        return builder.build();
    }

    private ToolSpecification getToolSpecification() throws Exception {
        ToolSpecification toolSpecification = ToolSpecification.builder()
                .name("AgentOutput")
                .description("AgentOutput model with custom actions")
                .parameters(JsonObjectSchema.builder()
                        .addProperty("current_state", JsonObjectSchema.builder()
                                .description("Current state of the agent")
                                .addStringProperty("evaluation_previous_goal")
                                .addStringProperty("memory")
                                .addStringProperty("next_goal")
                                .required(Arrays.asList("evaluation_previous_goal", "memory", "next_goal"))
                                .build())
                        .addProperty("action", JsonArraySchema.builder()
                                .description("List of actions to execute")
                                .items(getActionJsonObjectSchema())
                                .build())
                        .required(Arrays.asList("current_state", "action"))
                        .build())
                .build();
        return toolSpecification;
    }

    public AgentOutput getNextAction(List<ChatMessage> inputMessages) throws Exception {
        inputMessages = this.convertInputMessages(inputMessages);

        ChatResponse output;
        var response = new HashMap<String, Object>();
        AgentOutput parsed = null;
        if (this.toolCallingMethod == ToolCallingMethod.RAW) {
            log.debug("Using " + this.toolCallingMethod + " for " + this.chatModelLibrary);
            try {
                output = this.llm.chat(inputMessages);
                response.put("raw", output);
                response.put("parsed", null);
            } catch (Exception e) {
                log.error("Failed to invoke model: {}", e.getMessage());
                throw new LLMException(401, "LLM API call failed");
            }
            try {
                JSONObject parsedJson = Utils.extractJsonFromModelOutput(output.aiMessage().text());
                this.agentOutput = JSONUtil.toBean(parsedJson, AgentOutput.class);
                parsed = this.agentOutput;
                response.put("parsed", parsed);
            } catch (Exception e) {
                log.warn("Failed to parse model output: {} {}", output, e.getMessage());
                throw new Exception("Could not parse response.");
            }
        } else if (this.toolCallingMethod == null) {
            try {
                ChatRequest chatRequest = ChatRequest.builder().toolSpecifications(getToolSpecification()).messages(inputMessages).build();
//                ChatRequest chatRequest = ChatRequest.builder().messages(inputMessages).build();
                output = this.llm.chat(chatRequest);
                response.put("raw", output);
                response.put("parsed", null);
                parsed = JSONUtil.toBean(output.aiMessage().toolExecutionRequests().get(0).arguments(), AgentOutput.class);
            } catch (Exception e) {
                log.error("Failed to invoke model: {}", e.getMessage());
                throw new LLMException(401, "LLM API call failed");
            }
        } else {
            log.debug("Using {} for {}", this.toolCallingMethod, this.chatModelLibrary);
            output = this.llm.chat(inputMessages);
            response.put("raw", output);
            response.put("parsed", null);
        }

        if (response.get("parsing_error") != null && response.containsKey("raw")) {
            JSONObject rawMsg = JSONUtil.parseObj(String.valueOf(response.get("raw")));
            if (rawMsg.containsKey("tool_calls")) {
                JSONObject toolCall = rawMsg.getJSONArray("tool_calls").getJSONObject(0);
                String toolCallName = toolCall.getStr("name");
                HashMap<String, Object> toolCallArgs = toolCall.get("args", HashMap.class);

                AgentBrain currentState = new AgentBrain(
                        "Processing tool call",
                        "Executing action",
                        "Using tool call",
                        "Execute " + toolCallName
                );
                List<ActionModel> action = new ArrayList<>();
                ActionModel am = new ActionModel();
                am.put(toolCallName, toolCallArgs);
                action.add(am);

                this.agentOutput = new AgentOutput(currentState, action);
                parsed = this.agentOutput;
            } else {
                parsed = null;
            }
        } else if (parsed == null) {
            parsed = responseToAgentOutput(response);
        }

        if (parsed == null) {
            try {
                JSONObject parsedJson = Utils.extractJsonFromModelOutput(output.aiMessage().text());
                this.agentOutput = JSONUtil.toBean(parsedJson, AgentOutput.class);
                parsed = this.agentOutput;
            } catch (Exception e) {
                log.warn("Failed to parse model output: {} {}", JSONUtil.parseObj(response.get("raw")).get("content"), e.getMessage());
                throw new Exception("Could not parse response.");
            }
        }

        if (parsed.getAction().size() > this.settings.getMaxActionsPerStep()) {
            parsed.setAction(CollUtil.split(parsed.getAction(), this.settings.getMaxActionsPerStep()).get(0));
        }

        if (!(this.state.isPaused() || this.state.isStopped())) {
            logResponse(parsed);
        }
        return parsed;
    }

    private void logResponse(AgentOutput response) {
        String emoji = "";
        if (response.getCurrentState().getEvaluationPreviousGoal().indexOf("Success") >= 0) {
            emoji = "👍";
        } else if (response.getCurrentState().getEvaluationPreviousGoal().indexOf("Failed") >= 0) {
            emoji = "⚠";
        } else {
            emoji = "🤷";
        }
        log.info("{} Eval: {}", emoji, response.getCurrentState().getEvaluationPreviousGoal());
        log.info("🧠 Memory: {}", response.getCurrentState().getMemory());
        log.info("🎯 Next goal: {}", response.getCurrentState().getNextGoal());
        for (int i = 0; i < response.getAction().size(); i++) {
            ActionModel action = response.getAction().get(i);
            log.info("🛠️  Action {}/{}: {}", i + 1, response.getAction().size(), action.modelDump(true).toString());
        }
    }

    private AgentOutput responseToAgentOutput(HashMap<String, Object> response) throws Exception {
        String str = ((ChatResponse)response.get("raw")).aiMessage().text();
        String start = "```json";
        str = str.substring(str.indexOf(start) + start.length());
        String end = "```";
        str = str.substring(0, str.lastIndexOf(end));
        if (JSONUtil.isTypeJSONObject(str)) {
            return JSONUtil.toBean(str, AgentOutput.class);
        } else {
            log.error("responseToAgentOutput error, text:" + ((ChatResponse)response.get("raw")).aiMessage().text());
            throw new Exception("responseToAgentOutput");
        }
    }

    private void logAgentRun() {
        log.info("🚀 Starting task: {}", this.task);
        log.debug("Version: {}, Source: {}", this.version, this.source);
    }

    public Tuple<Boolean, Boolean> takeStep() {
        this.step(null);

        if (this.state.getHistory().isDone()) {
            if (this.settings.isValidateOutput()) {
                if (!this.validateOutput()) {
                    return new Tuple<>(true, false);
                }
            }

            this.logCompletion();
            if (this.registerDoneCallback != null) {
                this.registerDoneCallback.accept(this.state.getHistory());
            }
            return new Tuple<>(true, true);
        }
        return new Tuple<>(false, false);
    }

    public AgentHistoryList run(int maxSteps, AgentHookFunc onStepStart, AgentHookFunc onStepEnd) {
        if (this.verificationTask != null) {
            try {
                this.verificationTask.run();
            } catch (Exception e) {
            }
        }

        try {
            this.logAgentRun();

            if (!CollUtil.isEmpty(this.initialActions)) {
                this.state.setLastResult(this.multiAct(this.initialActions, false));
            }

            for (int step = 1; step <= maxSteps; step++) {
                if (this.state.getConsecutiveFailures() >= this.settings.getMaxFailures()) {
                    log.error("❌ Stopping due to {} consecutive failures", this.settings.getMaxFailures());
                    break;
                }

                if (this.state.isStopped()) {
                    log.info("Agent stopped");
                    break;
                }

                while (this.state.isPaused()) {
                    ThreadUtil.sleep(0.2, TimeUnit.SECONDS);
                    if (this.state.isStopped()) {
                        break;
                    }
                }

                if (onStepStart != null) {
                    onStepStart.accept(this);
                }

                AgentStepInfo stepInfo = new AgentStepInfo(step, maxSteps);
                this.step(stepInfo);

                if (onStepEnd != null) {
                    onStepEnd.accept(this);
                }

                if (this.state.getHistory().isDone()) {
                    if (this.settings.isValidateOutput() && step < maxSteps - 1) {
                        if (!this.validateOutput()) {
                            continue;
                        }
                    }

                    this.logCompletion();
                    break;
                }
            }
            return this.state.getHistory();
        } finally {
            this.close();
            if (this.settings.isGenerateGif()) {
                String outputPath = "agent_history.gif";
                if (StrUtil.isNotBlank(this.settings.getGenerateGifPath())) {
                    outputPath = this.settings.getGenerateGifPath();
                }
                //create_history_gif(task=self.task, history=self.state.history, output_path=output_path)
            }
        }
    }

    public List<ActionResult> multiAct(List<ActionModel> actions, boolean checkForNewElements) {
        var results = new ArrayList<ActionResult>();
        Map<Integer, DOMElementNode> cachedSelectorMap = this.browserContext.getSelectorMap();
        Set<String> cachedPathHashes = cachedSelectorMap.values()
                .stream()
                .map(e -> e.getHash().getBranchPathHash())
                .collect(Collectors.toSet());

        this.browserContext.removeHighlights();

        for (int i = 0; i < actions.size(); i++) {
            ActionModel action = actions.get(i);
            if (action.getIndex() != null && i != 0) {
                BrowserState newState = this.browserContext.getState(false);
                Map<Integer, DOMElementNode> newSelectorMap = newState.getSelectorMap();

                DOMElementNode origTarget = cachedSelectorMap.get(action.getIndex());
                String origTargetHash = origTarget != null ? origTarget.getHash().getBranchPathHash() : null;
                DOMElementNode newTarget = newSelectorMap.get(action.getIndex());
                String newTargetHash = newTarget != null ? newTarget.getHash().getBranchPathHash() : null;
                if (origTargetHash.equals(newTargetHash)) {
                    String msg = "Element index changed after action " + i + " / " + actions.size() + ", because page changed.";
                    log.info(msg);
                    results.add(ActionResult.builder().extractedContent(msg).includeInMemory(true).build());
                    break;
                }

                Set<String> newPathHashes = newSelectorMap.values().stream().map(e -> e.getHash().getBranchPathHash()).collect(Collectors.toSet());
                if (checkForNewElements && cachedPathHashes.containsAll(newPathHashes)) {
                    String msg = "Something new appeared after action " + i + " / " + actions.size();
                    log.info(msg);
                    results.add(ActionResult.builder().extractedContent(msg).includeInMemory(true).build());
                    break;
                }
            }
            this.raiseIfStoppedOrPaused();

            ActionResult result = this.controller.act(
                    action,
                    this.browserContext,
                    this.settings.getPageExtractionLlm(),
                    this.sensitiveData,
                    this.settings.getAvailableFilePaths(),
                    this.context
            );

            results.add(result);

            log.debug("Executed action " + (i + 1) + " / " + actions.size());
        }
        return results;
    }

    private boolean validateOutput() {
        String systemMsg =
                "You are a validator of an agent who interacts with a browser. " +
                        "Validate if the output of last action is what the user wanted and if the task is completed. " +
                        "If the task is unclear defined, you can let it pass. But if something is missing or the image does not show what was requested dont let it pass. " +
                        "Try to understand the page and help the model with suggestions like scroll, do x, ... to get the solution right. " +
                        "Task to validate: " + this.task + ". Return a JSON object with 2 keys: is_valid and reason. " +
                        "is_valid is a boolean that indicates if the output is correct. " +
                        "reason is a string that explains why it is valid or not." +
                        " example: {\"is_valid\": false, \"reason\": \"The user wanted to search for \\\"cat photos\\\", but the agent searched for \\\"dog photos\\\" instead.\"}";

        if (this.browserContext.getSession() != null) {
            BrowserState state = this.browserContext.getState(false);
            AgentMessagePrompt content = new AgentMessagePrompt(state, this.state.getLastResult(), this.settings.getIncludeAttributes(), null);
            List<ChatMessage> msg = Arrays.asList(new SystemMessage(systemMsg));
            ChatResponse output = this.llm.chat(msg);
            JSONObject response = JSONUtil.parseObj(output.aiMessage().text());
            JSONObject parsed = response.getJSONObject("parsed");
            boolean isValid = parsed.getBool("is_valid");
            if (!isValid) {
                log.info("❌ Validator decision: {}", parsed.getStr("reason"));
                String msg1 = "The output is not yet correct. " + parsed.getStr("reason") + ".";
                this.state.setLastResult(Arrays.asList(ActionResult.builder().extractedContent(msg1).includeInMemory(true).build()));
            } else {
                log.info("✅ Validator decision: {}", parsed.getStr("reason"));
            }
            return isValid;
        } else {
            return true;
        }
    }

    public void logCompletion() {
        log.info("✅ Task completed");
        if (this.state.getHistory().isSuccessful()) {
            log.info("✅ Successfully");
        } else {
            log.info("❌ Unfinished");
        }

        int totalTokens = this.state.getHistory().totalInputTokens();
        log.info("📝 Total input tokens used (approximate): {}", totalTokens);

        if (this.registerDoneCallback != null) {
            this.registerDoneCallback.accept(this.state.getHistory());
        }
    }

    public List<ActionResult> rerunHistory(AgentHistoryList history, int maxRetries, boolean skipFailure, float delayBetweenActions) {
        if (!CollUtil.isEmpty(this.initialActions)) {
            List<ActionResult> result = this.multiAct(this.initialActions, true);
            this.state.setLastResult(result);
        }

        var results = new ArrayList<ActionResult>();

        for (int i = 0; i < history.getHistory().size(); i++) {
            AgentHistory historyItem = history.getHistory().get(i);

            String goal = historyItem.getModelOutput() != null ? historyItem.getModelOutput().getCurrentState().getNextGoal() : "";
            log.info("Replaying step {}/{}: goal: {}", i + 1, history.getHistory().size(), goal);

            if (historyItem.getModelOutput() == null
                    || CollUtil.isEmpty(historyItem.getModelOutput().getAction())) {
                log.warn("Step {}: No action to replay, skipping", i + 1);
                results.add(ActionResult.builder().error("No action to replay").build());
                continue;
            }

            int retryCount = 0;
            while (retryCount < maxRetries) {
                try {
                    List<ActionResult> result = this.executeHistoryStep(historyItem, delayBetweenActions);
                    results.addAll(result);
                    break;
                } catch (Exception e) {
                    retryCount += 1;
                    if (retryCount == maxRetries) {
                        String errorMsg = "Step " + (i + 1) + " failed after " + maxRetries + " attempts: " + e.getMessage();
                        log.error(errorMsg);
                        if (!skipFailure) {
                            results.add(ActionResult.builder().error(errorMsg).build());
                            throw new RuntimeException(errorMsg);
                        }
                    } else {
                        log.warn("Step {} failed (attempt {}/{}), retrying...", i + 1, retryCount, maxRetries);
                        ThreadUtil.sleep(delayBetweenActions, TimeUnit.SECONDS);
                    }
                }
            }
        }
        return results;
    }

    private List<ActionResult> executeHistoryStep(AgentHistory historyItem, float delay) throws Exception {
        BrowserState state = this.browserContext.getState(false);
        if (state == null || historyItem.getModelOutput() == null) {
            throw new Exception("Invalid state or model output");
        }
        List<ActionModel> updatedActions = new ArrayList<>();
        for (int i = 0; i < historyItem.getModelOutput().getAction().size(); i++) {
            ActionModel action = historyItem.getModelOutput().getAction().get(i);
            ActionModel updatedAction = this.updateActionIndices(historyItem.getState().getInteractedElement().get(i), action, state);
            updatedActions.add(updatedAction);

            if (updatedAction == null) {
                throw new Exception("Could not find matching element " + i + " in current page");
            }
        }

        List<ActionResult> result = this.multiAct(updatedActions, true);

        ThreadUtil.sleep(delay, TimeUnit.SECONDS);
        return result;
    }

    private ActionModel updateActionIndices(DOMHistoryElement historyElement, ActionModel action, BrowserState currentState) {
        if (historyElement ==null || currentState.getElementTree() == null) {
            return action;
        }

        DOMElementNode currentElement = HistoryTreeProcessor.findHistoryElementInTree(historyElement, currentState.getElementTree());

        if (currentElement == null || currentElement.getHighlightIndex() == null) {
            return null;
        }

        int oldIndex = action.getIndex();
        if (oldIndex != currentElement.getHighlightIndex()) {
            action.setIndex(currentElement.getHighlightIndex());
            log.info("Element moved in DOM, updated index from {} to {}", oldIndex, currentElement.getHighlightIndex());
        }

        return action;
    }

    public void pause() {
        state.setPaused(true);
    }

    public void resume() {
        state.setPaused(false);
    }

    public void stop() {
        state.setStopped(true);
    }

    private List<ActionModel> convertInitialActions(List<HashMap<String, HashMap<String, Object>>> actions) {
        List<ActionModel> convertedActions = new ArrayList<>();
        for (HashMap<String, HashMap<String, Object>> actionDict : actions) {
            for (String actionName: actionDict.keySet()) {
                this.actionModel = (ActionModel)actionDict;
                convertedActions.add(this.actionModel);
            }
        }
        return convertedActions;
    }

    private String runPlanner() {
        if (this.settings.getPlannerLlm() == null) {
            return null;
        }

        Page page = this.browserContext.getCurrentPage();

        String standardActions = this.controller.getRegistry().getPromptDescription(null);
        String pageActions = this.controller.getRegistry().getPromptDescription(page);

        String allActions = standardActions;
        if (StrUtil.isNotBlank(pageActions)) {
            allActions += "\n" + pageActions;
        }

        List<ChatMessage> plannerMessages = new ArrayList<>();
        plannerMessages.add(
                new PlannerPrompt(allActions, 10, null, null).getSystemMessage(this.settings.isPlannerReasoning())
        );
        plannerMessages.addAll(
                this.messageManager.getMessages().subList(1, this.messageManager.getMessages().size())
        );

        if (!this.settings.isUseVisionForPlanner() && this.settings.isUseVision()) {
            UserMessage lastStateMessage = (UserMessage) plannerMessages.get(plannerMessages.size() - 1);
            String newMsg = "";
            if (lastStateMessage.contents() != null) {
                for (Content msg : lastStateMessage.contents()) {
                    if (msg.type() == ContentType.TEXT) {
                        newMsg += ((TextContent)context).text();
                    } else if (msg.type() == ContentType.IMAGE) {
                        continue;
                    }
                }
            } else {
                //这儿暂时只取最后一条
                newMsg = ((TextContent)lastStateMessage.contents().get(lastStateMessage.contents().size() - 1)).text();
            }

            plannerMessages.remove(plannerMessages.size() - 1);
            plannerMessages.add(new UserMessage(newMsg));
        }
        plannerMessages = convertInputMessages(plannerMessages);

        ChatResponse output;
        try {
            output = this.settings.getPlannerLlm().chat(plannerMessages);
        } catch (Exception e) {
            log.error("Failed to invoke planner: {}", e.getMessage());
            throw new LLMException(401, "LLM API call failed");
        }
        JSONObject response = JSONUtil.parseObj(output.aiMessage().text());
        String plan = response.getStr("content");
        return plan;
    }

    public MessageManager messageManager() {
        return this.messageManager;
    }

    public void close() {
        if (browserContext != null) {
            browserContext.close();
        }
        if (browser != null) {
            browser.close();
        }
    }
}
