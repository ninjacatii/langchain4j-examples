package dev.langchain4j.example.entity.dom._views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class DOMTextNode extends DOMBaseNode {
    private String text;
    private final String type = "TEXT_NODE";

    public boolean hasParentWithHighlightIndex() {
        DOMElementNode current = parent;
        while (current != null) {
            if (current.getHighlightIndex() != null) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    public boolean isParentInViewport() {
        return parent != null && parent.isInViewport();
    }

    public boolean isParentTopElement() {
        return parent != null && parent.isTopElement();
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("text", text);
        json.put("type", type);
        return json;
    }
}
