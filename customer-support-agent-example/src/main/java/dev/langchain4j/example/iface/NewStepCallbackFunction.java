package dev.langchain4j.example.iface;

import dev.langchain4j.example.entity.agent._views.AgentOutput;
import dev.langchain4j.example.entity.browser._views.BrowserState;

@FunctionalInterface
public interface NewStepCallbackFunction {
    void apply(BrowserState browserState, AgentOutput agentOutput, Integer i);
}
