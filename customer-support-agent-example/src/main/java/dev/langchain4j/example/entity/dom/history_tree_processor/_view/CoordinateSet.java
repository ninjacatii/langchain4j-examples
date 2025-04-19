package dev.langchain4j.example.entity.dom.history_tree_processor._view;


import lombok.Data;

@Data
public class CoordinateSet {
    private Coordinates topLeft;
    private Coordinates topRight;
    private Coordinates bottomLeft;
    private Coordinates bottomRight;
    private Coordinates center;
    private int width;
    private int height;
}
