package dev.langchain4j.example.entity.controller.registry._service;

import dev.langchain4j.example.entity.browser._context.BrowserContext;
import dev.langchain4j.example.entity.controller.registry._views.ActionModel;
import dev.langchain4j.example.entity.controller.registry._views.ActionRegistry;
import dev.langchain4j.example.entity.controller.registry._views.RegisteredAction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Data
public class Registry<T> {
    private final ActionRegistry registry = new ActionRegistry();
    private final List<String> excludeActions;

    public Registry(List<String> excludeActions) {
        this.excludeActions = excludeActions != null ? excludeActions : new ArrayList<>();
    }

    public ActionDecorator action(String description, Class<?> paramModel,
                                  List<String> domains, Predicate<Object> pageFilter) {
        return new ActionDecorator(this, description, paramModel, domains, pageFilter);
    }

    public CompletableFuture<Object> executeAction(String actionName, Map<String, Object> params,
                                                   BrowserContext browser, Object pageExtractionLlm,
                                                   Map<String, String> sensitiveData,
                                                   List<String> availableFilePaths, T context) {
        return CompletableFuture.supplyAsync(() -> {
            if (!registry.getActions().containsKey(actionName)) {
                throw new IllegalArgumentException("Action " + actionName + " not found");
            }

            RegisteredAction action = registry.getActions().get(actionName);
            try {
                // Validate parameters
                Object validatedParams = validateParams(action, params);

                // Handle sensitive data
                if (sensitiveData != null) {
                    validatedParams = replaceSensitiveData(validatedParams, sensitiveData);
                }

                // Prepare method invocation
                return invokeAction(action, validatedParams, browser,
                        pageExtractionLlm, availableFilePaths, context);
            } catch (Exception e) {
                throw new RuntimeException("Error executing action " + actionName, e);
            }
        });
    }

    private Object validateParams(RegisteredAction action, Map<String, Object> params) {
        // Parameter validation logic would go here
        return params; // Simplified for example
    }

    private Object replaceSensitiveData(Object params, Map<String, String> sensitiveData) {
        // Sensitive data replacement logic
        Pattern secretPattern = Pattern.compile("<secret>(.*?)</secret>");

        // Implementation would recursively process params and replace secrets
        return params; // Simplified for example
    }

    private Object invokeAction(RegisteredAction action, Object params,
                                BrowserContext browser, Object pageExtractionLlm,
                                List<String> availableFilePaths, T context) {
        // Method invocation logic using reflection
        try {
            Method method = action.getFunction().getClass().getMethod("execute");
            return method.invoke(action.getFunction(), params, browser,
                    pageExtractionLlm, availableFilePaths, context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke action", e);
        }
    }

    public Class<?> createActionModel(List<String> includeActions, Object page) {
        // Dynamic model creation logic
        return ActionModel.class; // Simplified for example
    }

    public String getPromptDescription(Object page) {
        return registry.getPromptDescription(page);
    }

    @Data
    @AllArgsConstructor
    public static class ActionDecorator {
        private Registry<?> registry;
        private String description;
        private Class<?> paramModel;
        private List<String> domains;
        private Predicate<Object> pageFilter;

        public Object decorate(Object function) {
            // Action registration logic
            String actionName = function.getClass().getSimpleName();
            if (registry.getExcludeActions().contains(actionName)) {
                return function;
            }

            RegisteredAction action = new RegisteredAction(
                    actionName, description, function, paramModel, domains, pageFilter);
            registry.getRegistry().getActions().put(actionName, action);
            return function;
        }
    }
}
