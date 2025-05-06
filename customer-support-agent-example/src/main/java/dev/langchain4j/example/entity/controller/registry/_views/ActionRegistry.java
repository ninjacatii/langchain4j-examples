package dev.langchain4j.example.entity.controller.registry._views;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.example.util.DomainMatcher;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.microsoft.playwright.Page;

@Data
public class ActionRegistry {
    private Map<String, RegisteredAction> actions = new HashMap<>();

    public static boolean matchDomains(List<String> domains, String url) {
        if (domains == null || url == null || url.isEmpty()) {
            return true;
        }

        try {
            URI parsedUrl = new URI(url);
            String domain = parsedUrl.getHost();
            if (domain == null) {
                return false;
            }

            // Remove port if present
            domain = domain.split(":")[0];

            return DomainMatcher.matchesDomain(domain, domains);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static boolean matchPageFilter(Function<Page, Boolean> pageFilter, Page page) {
        if (pageFilter == null) {
            return true;
        }
        return pageFilter.apply(page);
    }

    public String getPromptDescription(Page page) {
        if (page == null) {
            // For system prompt (no page provided), include only actions with no filters
            var list = new ArrayList<String>();
            for (RegisteredAction action : actions.values()) {
                if (action.pageFilter() == null && action.domains() == null) {
                    list.add(action.promptDescription());
                }
            }
            return StrUtil.join("\n", list);
        }

        return actions.values().stream()
                .filter(action -> {
                    if (action.domains() == null && action.pageFilter() == null) {
                        // skip actions with no filters, they are already included in the system prompt
                        return false;
                    }

                    boolean domainMatch = matchDomains(action.domains(), page.url());
                    boolean pageMatch = matchPageFilter(action.pageFilter(), page);
                    return domainMatch && pageMatch;
                })
                .map(RegisteredAction::promptDescription)
                .collect(Collectors.joining("\n"));
    }
}
