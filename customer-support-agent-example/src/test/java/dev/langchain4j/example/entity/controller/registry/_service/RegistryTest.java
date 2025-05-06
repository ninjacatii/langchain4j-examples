package dev.langchain4j.example.entity.controller.registry._service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.example.entity.agent._views.*;
import dev.langchain4j.example.entity.browser._context.BrowserContext;
import dev.langchain4j.example.entity.browser._views.BrowserStateHistory;
import dev.langchain4j.example.entity.browser._views.TabInfo;
import dev.langchain4j.example.entity.controller.registry._views.ActionModel;
import dev.langchain4j.example.entity.dom.history_tree_processor._view.DOMHistoryElement;
import dev.langchain4j.example.iface.MethodToAction;
import dev.langchain4j.example.util.Actions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class RegistryTest {
    @MethodToAction(
            description = "Complete task - with return text and if the task is finished (success=True) or not yet  completely finished (success=False), because last step is reached",
            paraType = { String.class, Boolean.class },
            paraName = { "text", "success" }
    )
    public static ActionResult done(String text, Boolean success, BrowserContext browser) {
        return ActionResult.builder().isDone(true).success(success).extractedContent(text).build();
    }

    @MethodToAction(
            description = "Wait for x seconds default 3",
            paraType = { Integer.class },
            paraName = { "seconds" }
    )
    public static ActionResult wait(Integer seconds, BrowserContext browser) {
        String msg = "🕒  Waiting for {seconds} seconds";
        log.info(msg);
        ThreadUtil.sleep(seconds, TimeUnit.SECONDS);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Wait for x seconds default 3",
            paraType = { Integer.class },
            paraName = { "seconds" }
    )
    public static ActionResult wait1(Integer seconds, BrowserContext browser) {
        String msg = "🕒  Waiting for {seconds} seconds";
        log.info(msg);
        ThreadUtil.sleep(seconds, TimeUnit.SECONDS);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    private ActionModel actionRegistry() throws Exception {
        var registry = new Registry<>();

        Class<?> registryTestClass = RegistryTest.class;
        Method doneMethod = registryTestClass.getMethod("done", String.class, Boolean.class, BrowserContext.class);
        Method waitMethod = registryTestClass.getMethod("wait", Integer.class, BrowserContext.class);
        Method wait1Method = registryTestClass.getMethod("wait1", Integer.class, BrowserContext.class);

        registry.registryAction(doneMethod);
        registry.registryAction(waitMethod);
        registry.registryAction(wait1Method);

        return registry.createActionModel(null, null);
    }

    private AgentHistoryList getSampleHistory(ActionModel actionModel) throws Exception {
        ActionModel waitAction = actionModel.getAction("wait", JSONUtil.createObj().set("seconds", 1));
        ActionModel wait1Action = actionModel.getAction("wait1", JSONUtil.createObj().set("seconds", 3));
        ActionModel doneAction = actionModel.getAction("done", JSONUtil.createObj().set("text", "Task completed"));

        List<AgentHistory> histories = new ArrayList<>();
        histories.add(new AgentHistory(
                new AgentOutput(
                    AgentBrain.builder().evaluationPreviousGoal("None").memory("Started task").nextGoal("Click button").build(),
                        Collections.singletonList(waitAction)),
                Collections.singletonList(ActionResult.builder().isDone(false).build()),
                new BrowserStateHistory(
                        "https://example.com",
                        "Page 1",
                        List.of(new TabInfo(1, "https://example.com", "Page 1")),
                        List.of(DOMHistoryElement.builder().xpath("//button[1]").build()),
                        "screenshot1.png"
                ), null));
        histories.add(new AgentHistory(
                new AgentOutput(
                        AgentBrain.builder().evaluationPreviousGoal("Clicked button").memory("Button clicked").nextGoal("Extract content").build(),
                        Collections.singletonList(wait1Action)),
                Collections.singletonList(ActionResult.builder().isDone(false).extractedContent("Extracted text").error("Failed to extract completely").build()),
                new BrowserStateHistory(
                        "https://example.com/page2",
                        "Page 2",
                        List.of(new TabInfo(2, "https://example.com/page2", "Page 2")),
                        List.of(DOMHistoryElement.builder().xpath("//div[2]").build()),
                        "screenshot2.png"
                ), null));
        histories.add(new AgentHistory(
                new AgentOutput(
                        AgentBrain.builder().evaluationPreviousGoal("Extracted content").memory("Content extracted").nextGoal("Finish task").build(),
                        Collections.singletonList(doneAction)),
                Collections.singletonList(ActionResult.builder().isDone(true).extractedContent("Task completed").error(null).build()),
                new BrowserStateHistory(
                        "https://example.com/page2",
                        "Page 2",
                        List.of(new TabInfo(2, "https://example.com/page2", "Page 2")),
                        List.of(DOMHistoryElement.builder().xpath("//div[2]").build()),
                        "screenshot3.png"
                ), null));
        return new AgentHistoryList(histories);
    }

    @Test
    public void testLastModelOutput() throws Exception {
        AgentHistoryList sampleHistory = getSampleHistory(actionRegistry());
        JSONObject lastOutput = sampleHistory.lastAction();
        log.info(JSONUtil.toJsonStr(lastOutput));
    }

    @Test
    public void testGetErrors() throws Exception {
        AgentHistoryList sampleHistory = getSampleHistory(actionRegistry());
        List<String> errors = sampleHistory.errors();
        log.info(JSONUtil.toJsonStr(errors));
    }

    @Test
    public void testFinalResult() throws Exception {
        AgentHistoryList sampleHistory = getSampleHistory(actionRegistry());
        log.info(sampleHistory.finalResult());
    }

}