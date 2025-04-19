package dev.langchain4j.example.entity.browser._views;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TabInfo {
    private int pageId;
    private String url;
    private String title;
    private Integer parentPageId; // Nullable

    public TabInfo(int pageId, String url, String title) {
        this(pageId, url, title, null);
    }

    public TabInfo(int pageId, String url, String title, Integer parentPageId) {
        this.pageId = pageId;
        this.url = url;
        this.title = title;
        this.parentPageId = parentPageId;
    }
}
