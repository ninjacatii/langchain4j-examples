package dev.langchain4j.example.entity.browser._views;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.langchain4j.example.entity.dom.history_tree_processor._view.DOMHistoryElement;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class BrowserStateHistory {
    private String url;
    private String title;
    private List<TabInfo> tabs;
    private List<DOMHistoryElement> interactedElement;
    private String screenshot; // Nullable

    public Map<String, Object> toDict() {
        Map<String, Object> data = new HashMap<>();
        data.put("tabs", tabs.stream()
                .map(tab -> {
                    Map<String, Object> tabMap = new HashMap<>();
                    tabMap.put("pageId", tab.getPageId());
                    tabMap.put("url", tab.getUrl());
                    tabMap.put("title", tab.getTitle());
                    tabMap.put("parentPageId", tab.getParentPageId());
                    return tabMap;
                })
                .collect(Collectors.toList()));

        data.put("screenshot", screenshot);
        data.put("interactedElement", interactedElement.stream()
                .map(el -> el != null ? el.toDict() : null)
                .collect(Collectors.toList()));
        data.put("url", url);
        data.put("title", title);
        return data;
    }
}
