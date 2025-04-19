package dev.langchain4j.example.entity.controller.registry._views;

import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.microsoft.playwright.Page;

@Data
public class ActionRegistry {
    private Map<String, RegisteredAction> actions = new HashMap<>();

    private boolean matchDomains(List<String> domains, String url) {
        if (domains == null || url == null || url.isEmpty()) {
            return true;
        }

        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return false;

            // Remove port if present
            host = host.split(":")[0];

            for (String pattern : domains) {
                if (matchesGlob(pattern, host)) {
                    return true;
                }
            }
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private boolean matchesGlob(String pattern, String input) {
        // Simple glob matching implementation
        String regex = pattern.replace(".", "\\.")
                .replace("*", ".*");
        return input.matches(regex);
    }

    private boolean matchPageFilter(Predicate<Page> pageFilter, Page page) {
        return pageFilter == null || pageFilter.test(page);
    }

    public String getPromptDescription(Page page) {
        if (page == null) {
            return actions.values().stream()
                    .filter(action -> action.pageFilter() == null && action.domains() == null)
                    .map(RegisteredAction::getPromptDescription)
                    .collect(Collectors.joining("\n"));
        }

        return actions.values().stream()
                .filter(action -> {
                    boolean domainMatch = matchDomains(action.domains(), page.url());
                    boolean pageMatch = matchPageFilter(action.pageFilter(), page);
                    return domainMatch && pageMatch;
                })
                .map(RegisteredAction::getPromptDescription)
                .collect(Collectors.joining("\n"));
    }
}
