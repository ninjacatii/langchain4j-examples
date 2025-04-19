package dev.langchain4j.example.entity.dom._views;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DOMState {
    protected DOMElementNode elementTree;
    protected Map<Integer, DOMElementNode> selectorMap;

    public DOMState() {

    }
}
