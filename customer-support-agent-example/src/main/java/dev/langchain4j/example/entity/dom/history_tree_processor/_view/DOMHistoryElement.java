package dev.langchain4j.example.entity.dom.history_tree_processor._view;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Builder
public class DOMHistoryElement {
    private String tagName;
    private String xpath;
    private Integer highlightIndex;
    private List<String> entireParentBranchPath;
    private Map<String, String> attributes;
    private boolean shadowRoot = false;
    private String cssSelector;
    private CoordinateSet pageCoordinates;
    private CoordinateSet viewportCoordinates;
    private ViewportInfo viewportInfo;

    public Map<String, Object> toDict() {
        Map<String, Object> dict = new HashMap<>();
        dict.put("tag_name", tagName);
        dict.put("xpath", xpath);
        dict.put("highlight_index", highlightIndex);
        dict.put("entire_parent_branch_path", entireParentBranchPath);
        dict.put("attributes", attributes);
        dict.put("shadow_root", shadowRoot);
        dict.put("css_selector", cssSelector);

        if (pageCoordinates != null) {
            dict.put("page_coordinates", Map.of(
                    "top_left", Map.of("x", pageCoordinates.getTopLeft().getX(), "y", pageCoordinates.getTopLeft().getY()),
                    "top_right", Map.of("x", pageCoordinates.getTopRight().getX(), "y", pageCoordinates.getTopRight().getY()),
                    "bottom_left", Map.of("x", pageCoordinates.getBottomLeft().getX(), "y", pageCoordinates.getBottomLeft().getY()),
                    "bottom_right", Map.of("x", pageCoordinates.getBottomRight().getX(), "y", pageCoordinates.getBottomRight().getY()),
                    "center", Map.of("x", pageCoordinates.getCenter().getX(), "y", pageCoordinates.getCenter().getY()),
                    "width", pageCoordinates.getWidth(),
                    "height", pageCoordinates.getHeight()
            ));
        } else {
            dict.put("page_coordinates", null);
        }

        if (viewportCoordinates != null) {
            dict.put("viewport_coordinates", Map.of(
                    "top_left", Map.of("x", viewportCoordinates.getTopLeft().getX(), "y", viewportCoordinates.getTopLeft().getY()),
                    "top_right", Map.of("x", viewportCoordinates.getTopRight().getX(), "y", viewportCoordinates.getTopRight().getY()),
                    "bottom_left", Map.of("x", viewportCoordinates.getBottomLeft().getX(), "y", viewportCoordinates.getBottomLeft().getY()),
                    "bottom_right", Map.of("x", viewportCoordinates.getBottomRight().getX(), "y", viewportCoordinates.getBottomRight().getY()),
                    "center", Map.of("x", viewportCoordinates.getCenter().getX(), "y", viewportCoordinates.getCenter().getY()),
                    "width", viewportCoordinates.getWidth(),
                    "height", viewportCoordinates.getHeight()
            ));
        } else {
            dict.put("viewport_coordinates", null);
        }

        if (viewportInfo != null) {
            dict.put("viewport_info", Map.of(
                    "scroll_x", viewportInfo.getScrollX(),
                    "scroll_y", viewportInfo.getScrollY(),
                    "width", viewportInfo.getWidth(),
                    "height", viewportInfo.getHeight()
            ));
        } else {
            dict.put("viewport_info", null);
        }

        return dict;
    }
}
