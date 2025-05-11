package dev.langchain4j.example.entity.controller.registry._service;


import dev.langchain4j.example.entity.agent._views.ActionResult;
import dev.langchain4j.example.entity.controller.registry._views.ActionModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.example.entity.browser._context.BrowserContext;
import lombok.Data;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Data
public class Controller<T> {
    private Registry<T> registry;

    public Controller(List<String> excludeActions) {
        this.registry = new Registry<>(excludeActions);
    }

    public ActionResult act(
            ActionModel action,
            BrowserContext browserContext,
            ChatLanguageModel pageExtractionLlm,
            Map<String, String> sensitiveData,
            List<String> availableFilePaths,
            T context) {
        for (String actionName : action.keySet()) {
            HashMap<String, Object> params = action.get(actionName);
            ActionResult result = this.registry.executeAction(
                    actionName,
                    params,
                    browserContext,
                    pageExtractionLlm,
                    sensitiveData,
                    availableFilePaths,
                    context
            );
            if (result == null) {
                return new ActionResult(false, false, null, null, false);
            } else {
                return result;
            }
        }
        return new ActionResult(false, false, null, null, false);
    }
}
