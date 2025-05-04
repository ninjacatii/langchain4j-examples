package dev.langchain4j.example.entity.controller.registry._service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.example.entity.agent._views.*;
import dev.langchain4j.example.entity.browser._views.BrowserStateHistory;
import dev.langchain4j.example.entity.browser._views.TabInfo;
import dev.langchain4j.example.entity.controller.registry._views.ActionModel;
import dev.langchain4j.example.entity.dom.history_tree_processor._view.DOMHistoryElement;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class RegistryTest {
    private AgentHistoryList getSampleHistory() {
        ActionModel clickAction = null;
        ActionModel extractAction = null;
        ActionModel doneAction = null;

        List<AgentHistory> histories = new ArrayList<>();
        histories.add(new AgentHistory(
                new AgentOutput(
                    AgentBrain.builder().evaluationPreviousGoal("None").memory("Started task").nextGoal("Click button").build(),
                        Collections.singletonList(clickAction)),
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
                        Collections.singletonList(extractAction)),
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
    public void testLastModelOutput() {
        AgentHistoryList sampleHistory = getSampleHistory();
        JSONObject lastOutput = sampleHistory.lastAction();
        log.info(JSONUtil.toJsonStr(lastOutput));
    }

}