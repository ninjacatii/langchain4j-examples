package dev.langchain4j.example.entity.controller.registry._service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.microsoft.playwright.Page;
import dev.langchain4j.example.entity.agent._views.ActionResult;
import dev.langchain4j.example.entity.browser._context.BrowserContext;
import dev.langchain4j.example.entity.controller.registry._views.ActionModel;
import dev.langchain4j.example.entity.controller.registry._views.ActionRegistry;
import dev.langchain4j.example.entity.controller.registry._views.RegisteredAction;
import dev.langchain4j.example.iface.MethodToAction;
import dev.langchain4j.example.util.ActionPageFilters;
import dev.langchain4j.example.util.Actions;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Slf4j
@Data
public class Registry<T> {
    private final ActionRegistry registry = new ActionRegistry();
    private final List<String> excludeActions;

    @SuppressWarnings("unchecked")
    private static <T, R> Function<T, R> methodHandleToFunction(MethodHandle handle) {
        return input -> {
            try {
                return (R) handle.invokeExact(input);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Function<Page, Boolean> getPageFilter(String methodName) {
        try {
            if (StrUtil.isBlank(methodName)) {
                return null;
            } else {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                MethodHandle handle = lookup.findStatic(
                        ActionPageFilters.class, methodName, MethodType.methodType(Boolean.class, Page.class)
                );
                return methodHandleToFunction(handle);
            }
        } catch (Exception e) {
//            log.info("No page filter method:" + methodName);
            return null;
        }
    }

    //给个空的Registry
    public Registry() {
        excludeActions = null;

    }

    //默认Registry
    public Registry(List<String> excludeActions) {
        this.excludeActions = excludeActions != null ? excludeActions : new ArrayList<>();

        Class<?> actionsClass = Actions.class;
        Method[] methods = actionsClass.getMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(MethodToAction.class)) {
                registryAction(method);
            }
        }
    }

    public void registryAction(Method method) {
        MethodToAction methodToAction = method.getAnnotation(MethodToAction.class);
        RegisteredAction action = new RegisteredAction(
                method.getName(),
                methodToAction.description(),
                methodToAction.paraType(),
                methodToAction.paraName(),
                method,
                Arrays.stream(methodToAction.domains()).toList(),
                getPageFilter(method.getName() + "_PageFilter")
        );
        this.registry.getActions().put(method.getName(), action);
    }


    public ActionResult executeAction(String actionName,
                                Map<String, Object> params,
                                BrowserContext browser,
                                ChatLanguageModel pageExtractionLlm,
                                Map<String, String> sensitiveData,
                                List<String> availableFilePaths,
                                T context) {
        if (!registry.getActions().containsKey(actionName)) {
            throw new IllegalArgumentException("Action " + actionName + " not found");
        }

        RegisteredAction action = registry.getActions().get(actionName);
        String[] paraName = action.paraName();
        Object[] validatedParams = validateParams(paraName, params, browser, pageExtractionLlm);
        try {
            return (ActionResult)action.function().invoke(null, validatedParams);
        } catch (Exception e) {
            log.error("Error executing action[" + actionName + "]:" + e.getMessage(), e);
            return ActionResult.builder().success(false).error("Error executing action[" + actionName + "]:" + e.getMessage()).build();
        }
    }

    private Object[] validateParams(String[] paraName, Map<String, Object> params, BrowserContext browser, ChatLanguageModel pageExtractionLlm) {
        var result = new Object[paraName.length + 2];
        for (int i = 0; i < paraName.length; i++) {
            String name = paraName[i];
            if (params.containsKey(name)) {
                Object o = params.get(name);
                result[i] = o;
            } else {
                result[i] = null;
            }
        }
        result[result.length - 2] = browser;
        result[result.length - 1] = pageExtractionLlm;
        return result;
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
            Method method = action.function().getClass().getMethod("execute");
            return method.invoke(action.function(), params, browser,
                    pageExtractionLlm, availableFilePaths, context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke action", e);
        }
    }

    public ActionModel createActionModel(List<String> includeActions, Page page) {
        var actionModel = new ActionModel();
        for (String key: this.registry.getActions().keySet()) {
            if (CollUtil.isEmpty(includeActions) || includeActions.contains(key)) {
                RegisteredAction action = this.registry.getActions().get(key);
                if (page == null || (ActionRegistry.matchPageFilter(action.pageFilter(), page) && ActionRegistry.matchDomains(action.domains(), page.url()))) {
                    var map = new HashMap<String, Object>();
                    for (String para: action.paraName()) {
                        map.put(para, null);
                    }
                    actionModel.put(key, map);
                }
            }
        }
        return actionModel;
    }

    public String getPromptDescription(Page page) {
        return registry.getPromptDescription(page);
    }
}
