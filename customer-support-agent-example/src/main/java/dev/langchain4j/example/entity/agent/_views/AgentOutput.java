package dev.langchain4j.example.entity.agent._views;

import dev.langchain4j.example.entity.controller.registry._views.ActionModel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AgentOutput {
    private AgentBrain currentState;
    private List<ActionModel> action;

}
