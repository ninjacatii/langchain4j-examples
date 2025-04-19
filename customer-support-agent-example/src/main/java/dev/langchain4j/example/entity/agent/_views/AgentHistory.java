package dev.langchain4j.example.entity.agent._views;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import dev.langchain4j.example.entity.controller.registry._views.ActionModel;

@Data
public class AgentHistory {
    @Nullable
    private AgentOutput modelOutput;
    private List<ActionResult> result;
    private BrowserStateHistory state;
    @Nullable private StepMetadata metadata;

    public Map<String, Object> modelDump() {
        Map<String, Object> dump = new HashMap<>();

        if (modelOutput != null) {
            List<Map<String, Object>> actionDump = modelOutput.getAction().stream()
                    .map(ActionModel::modelDump)
                    .collect(Collectors.toList());

            dump.put("modelOutput", Map.of(
                    "currentState", modelOutput.getCurrentState().modelDump(),
                    "action", actionDump
            ));
        }

        dump.put("result", result.stream()
                .map(ActionResult::modelDump)
                .collect(Collectors.toList()));

        dump.put("state", state.toDict());
        dump.put("metadata", metadata != null ? metadata.modelDump() : null);

        return dump;
    }

    public static List<DOMHistoryElement> getInteractedElement(
            AgentOutput modelOutput,
            SelectorMap selectorMap
    ) {
        // Implementation omitted for brevity
        return new ArrayList<>();
    }
}
