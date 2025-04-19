package dev.langchain4j.example.entity.dom._views;

import lombok.Data;

import java.util.Map;

@Data
public class DOMState {
    private DOMElementNode elementTree;
    private Map<Integer, DOMElementNode> selectorMap;
}
