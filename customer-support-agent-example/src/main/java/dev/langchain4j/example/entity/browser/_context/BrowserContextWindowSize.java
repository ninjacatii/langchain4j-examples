package dev.langchain4j.example.entity.browser._context;

import lombok.Data;

@Data
public class BrowserContextWindowSize {
    private int width;
    private int height;

    public BrowserContextWindowSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
