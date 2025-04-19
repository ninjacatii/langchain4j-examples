package dev.langchain4j.example.entity.agent._views;

import dev.langchain4j.example.entity.controller.registry._views.ActionModel;
import lombok.Data;

import java.util.List;

@Data
public class AgentOutput {
    private AgentBrain currentState;
    private List<ActionModel> action;

    public static Class<?> typeWithCustomActions(Class<? extends ActionModel> customActions) {
        // Java doesn't support dynamic class creation like Python
        // Would need to use reflection or code generation
        throw new UnsupportedOperationException("Dynamic class creation not supported in Java");
    }
}
