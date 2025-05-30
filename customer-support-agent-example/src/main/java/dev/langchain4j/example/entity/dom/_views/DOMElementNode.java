package dev.langchain4j.example.entity.dom._views;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.example.entity.dom.history_tree_processor._service.HistoryTreeProcessor;
import dev.langchain4j.example.entity.dom.history_tree_processor._view.CoordinateSet;
import dev.langchain4j.example.entity.dom.history_tree_processor._view.HashedDomElement;
import dev.langchain4j.example.entity.dom.history_tree_processor._view.ViewportInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class DOMElementNode extends DOMBaseNode {
    private String tagName;
    private String xpath;
    private Map<String, String> attributes;
    private List<DOMBaseNode> children;
    private boolean isInteractive = false;
    private boolean isTopElement = false;
    private boolean isInViewport = false;
    private boolean shadowRoot = false;
    private Integer highlightIndex; // Nullable
    private CoordinateSet viewportCoordinates; // Nullable
    private CoordinateSet pageCoordinates; // Nullable
    private ViewportInfo viewportInfo; // Nullable
    private boolean isNew; // Nullable

    public DOMElementNode(
            String tagName,
            String xpath,
            Map<String, String> attributes,
            List<DOMBaseNode> children,
            boolean isVisible,
            boolean isInteractive,
            boolean isTopElement,
            boolean isInViewport,
            Integer highlightIndex,
            boolean shadowRoot,
            DOMElementNode parent,
            ViewportInfo viewportInfo
    ) {
        this.tagName = tagName;
        this.xpath = xpath;
        this.attributes = attributes;
        this.children = children;
        this.isVisible = isVisible;
        this.isInteractive = isInteractive;
        this.isTopElement = isTopElement;
        this.isInViewport = isInViewport;
        this.highlightIndex = highlightIndex;
        this.shadowRoot = shadowRoot;
        this.parent = parent;
        this.viewportInfo = viewportInfo;
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("tag_name", tagName);
        json.put("xpath", xpath);
        json.put("attributes", attributes);
        json.put("is_visible", isVisible);
        json.put("is_interactive", isInteractive);
        json.put("is_top_element", isTopElement);
        json.put("is_in_viewport", isInViewport);
        json.put("shadow_root", shadowRoot);
        json.put("highlight_index", highlightIndex);
        json.put("viewport_coordinates", viewportCoordinates);
        json.put("page_coordinates", pageCoordinates);

        List<Map<String, Object>> childrenJson = new ArrayList<>();
        for (DOMBaseNode child : children) {
            childrenJson.add(child.toJson());
        }
        json.put("children", childrenJson);

        return json;
    }

    public HashedDomElement getHash() {
        return HistoryTreeProcessor.hashDomElement(this);
    }

    public String getAllTextTillNextClickableElement(int maxDepth) {
        StringBuilder textBuilder = new StringBuilder();
        collectText(this, 0, maxDepth, textBuilder);
        return textBuilder.toString().trim();
    }

    private void collectText(DOMBaseNode node, int currentDepth, int maxDepth, StringBuilder builder) {
        if (maxDepth != -1 && currentDepth > maxDepth) {
            return;
        }

        if (node instanceof DOMElementNode) {
            DOMElementNode elementNode = (DOMElementNode) node;
            if (elementNode != this && elementNode.getHighlightIndex() != null) {
                return;
            }
        }

        if (node instanceof DOMTextNode) {
            builder.append(((DOMTextNode) node).getText()).append("\n");
        } else if (node instanceof DOMElementNode) {
            for (DOMBaseNode child : ((DOMElementNode) node).getChildren()) {
                collectText(child, currentDepth + 1, maxDepth, builder);
            }
        }
    }

    public String clickableElementsToString(List<String> includeAttributes) {
        var formattedText = new ArrayList<String>();
        processNode(this, 0, includeAttributes, formattedText);
        return StrUtil.join("\n", formattedText);
    }

    private void processNode(DOMBaseNode node, int depth, List<String> includeAttributes, List<String> formattedText) {
        int nextDepth = depth;
        String depthStr = "\t".repeat(depth);

        if (node instanceof DOMElementNode) {
            DOMElementNode elementNode = (DOMElementNode) node;
            if (elementNode.getHighlightIndex() != null) {
                nextDepth += 1;

                String text = elementNode.getAllTextTillNextClickableElement(-1);
                String attributesStr = buildAttributesString(elementNode, includeAttributes, text);

                String highlightIndicator = (elementNode.isNew ? "*[" : "[") + elementNode.getHighlightIndex() + (elementNode.isNew ? "]*" : "]");
                String line = depthStr + highlightIndicator + "<" + elementNode.getTagName();
                if (StrUtil.isNotBlank(attributesStr)) {
                    line += " " + attributesStr;
                }

                if (StrUtil.isNotBlank(text)) {
                    if (StrUtil.isBlank(attributesStr)) {
                        line += " ";
                    }
                    line += ">" + text;
                } else if (StrUtil.isBlank(attributesStr)) {
                    line += " ";
                }
                line += " />";
                formattedText.add(line);
            }

            for (DOMBaseNode child : elementNode.getChildren()) {
                processNode(child, nextDepth, includeAttributes, formattedText);
            }
        } else if (node instanceof DOMTextNode) {
            DOMTextNode textNode = (DOMTextNode) node;
            if (!textNode.hasParentWithHighlightIndex()
                    && textNode.getParent() != null
                    && textNode.getParent().isVisible()
                    && textNode.getParent().isTopElement()) {
                formattedText.add(depthStr + textNode.getText());
            }
        }
    }

    private String buildAttributesString(DOMElementNode node, List<String> includeAttributes, String text) {
        if (includeAttributes == null || includeAttributes.isEmpty()) {
            return "";
        }

        Map<String, String> filteredAttrs = new HashMap<>();
        for (String attr : includeAttributes) {
            if (node.getAttributes().containsKey(attr)) {
                String value = node.getAttributes().get(attr);

                if ("role".equals(attr) && node.getTagName().equals(value)) {
                    continue;
                }
                if ("aria-label".equals(attr) && StrUtil.isNotBlank(value) && value.trim().equals(text.trim())) {
                    continue;
                }
                if ("placeholder".equals(attr) && StrUtil.isNotBlank(value) && value.trim().equals(text.trim())) {
                    continue;
                }
                filteredAttrs.put(attr, value);
            }
        }

        if (filteredAttrs.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : filteredAttrs.entrySet()) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(entry.getKey()).append("='").append(entry.getValue()).append("'");
        }
        return builder.toString();
    }

    public DOMElementNode getFileUploadElement(boolean checkSiblings) {
        if ("input".equals(tagName) && "file".equals(attributes.get("type"))) {
            return this;
        }

        for (DOMBaseNode child : children) {
            if (child instanceof DOMElementNode) {
                DOMElementNode result = ((DOMElementNode) child).getFileUploadElement(false);
                if (result != null) {
                    return result;
                }
            }
        }

        if (checkSiblings && parent != null) {
            for (DOMBaseNode sibling : parent.getChildren()) {
                if (sibling != this && sibling instanceof DOMElementNode) {
                    DOMElementNode result = ((DOMElementNode) sibling).getFileUploadElement(false);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }
}
