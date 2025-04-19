package dev.langchain4j.example.entity.dom.history_tree_processor._view;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ViewportInfo {
    private int scrollX;
    private int scrollY;
    private int width;
    private int height;
}
