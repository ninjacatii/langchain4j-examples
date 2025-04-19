package dev.langchain4j.example.entity.dom._views;

import lombok.Data;

import java.util.Map;

@Data
public abstract class DOMBaseNode {
    protected boolean isVisible;
    protected DOMElementNode parent; // Nullable

    public abstract Map<String, Object> toJson();
}
