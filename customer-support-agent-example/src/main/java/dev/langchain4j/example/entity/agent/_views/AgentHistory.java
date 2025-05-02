package dev.langchain4j.example.entity.agent._views;

import dev.langchain4j.example.entity.browser._views.BrowserStateHistory;
import dev.langchain4j.example.entity.dom._views.DOMElementNode;
import dev.langchain4j.example.entity.dom.history_tree_processor._service.HistoryTreeProcessor;
import dev.langchain4j.example.entity.dom.history_tree_processor._view.DOMHistoryElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import dev.langchain4j.example.entity.controller.registry._views.ActionModel;

@Data
@AllArgsConstructor
public class AgentHistory {
    @Nullable
    private AgentOutput modelOutput;
    private List<ActionResult> result;
    private BrowserStateHistory state;
    @Nullable private StepMetadata metadata;

    public static List<DOMHistoryElement> getInteractedElement(
            AgentOutput modelOutput,
            Map<Integer, DOMElementNode> selectorMap) {
        var elements = new ArrayList<DOMHistoryElement>();
        for (ActionModel action: modelOutput.getAction()) {
            Integer index = action.getIndex();
            if (index != null && selectorMap.containsKey(index)) {
                DOMElementNode el = selectorMap.get(index);
                elements.add(HistoryTreeProcessor.convertDomElementToHistoryElement(el));
            } else {
                elements.add(null);
            }
        }
        return elements;
    }
}
