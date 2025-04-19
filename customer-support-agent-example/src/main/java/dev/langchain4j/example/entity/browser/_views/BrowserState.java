package dev.langchain4j.example.entity.browser._views;

import dev.langchain4j.example.entity.dom._views.DOMState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

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
}
