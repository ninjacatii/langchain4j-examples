package dev.langchain4j.example.entity.browser._views;

import dev.langchain4j.example.entity.dom._views.DOMElementNode;
import dev.langchain4j.example.entity.dom._views.DOMState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class BrowserState extends DOMState {
    private String url;
    private String title;
    private List<TabInfo> tabs;
    private String screenshot; // Nullable
    private Integer pixelsAbove = 0;
    private Integer pixelsBelow = 0;
    private List<String> browserErrors = new ArrayList<>();

    public BrowserState(
            DOMElementNode elementTree,
            Map<Integer, DOMElementNode> selectorMap,
            String url,
            String title,
            List<TabInfo> tabs,
            String screenshot,
            Integer pixelsAbove,
            Integer pixelsBelow) {
        this.elementTree = elementTree;
        this.selectorMap = selectorMap;
        this.url = url;
        this.title = title;
        this.tabs = tabs;
        this.screenshot = screenshot;
        this.pixelsAbove = pixelsAbove;
        this.pixelsBelow = pixelsBelow;
    }
}
