package dev.langchain4j.example.entity.controller.registry._views;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public record RegisteredAction(
        String name,
        String description,
        Function<Object, Object> function, // Simplified function type
        Class<?> paramModel,
        List<String> domains,
        Predicate<Page> pageFilter
) {
    @JsonIgnore
    public String getPromptDescription() {
        // Simplified prompt generation
        return description + ": \n{" + name + ": " +
                paramModel.getSimpleName() + "}";
    }
}
