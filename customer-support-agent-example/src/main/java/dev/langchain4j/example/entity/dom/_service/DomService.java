package dev.langchain4j.example.entity.dom._service;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import dev.langchain4j.example.entity.dom._views.DOMBaseNode;
import dev.langchain4j.example.entity.dom._views.DOMElementNode;
import dev.langchain4j.example.entity.dom._views.DOMState;
import dev.langchain4j.example.entity.dom._views.DOMTextNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class DomService {
    private final Page page;
    private final Map<String, Object> xpathCache = new HashMap<>();
    private String jsCode;

    public DomService(Page page) {
        this.page = page;
        try {
            Path jsPath = Paths.get("src/main/resources/browser_use/dom/buildDomTree.js");
            this.jsCode = Files.readString(jsPath);
        } catch (IOException e) {
            log.error("Failed to load buildDomTree.js: " + e.getMessage());
            this.jsCode = "";
        }
    }

    public DOMState getClickableElements(boolean highlightElements, int focusElement, int viewportExpansion) {
        Map.Entry<DOMElementNode, Map<Integer, DOMElementNode>> map = buildDomTree(highlightElements, focusElement, viewportExpansion);
        return new DOMState(map.getKey(), map.getValue());
    }

    public CompletableFuture<List<String>> getCrossOriginIframes() {
        return CompletableFuture.supplyAsync(() -> {
            List<String> iframeUrls = new ArrayList<>();
            try {
                // Get hidden iframe URLs
                List<String> hiddenFrameUrls = (List<String>)page.locator("iframe")
                        .filter(new Locator.FilterOptions().setVisible(false))
                        .evaluateAll("e => e.map(e => e.src)");

                // Check visible cross-origin iframes
                for (com.microsoft.playwright.Frame frame : page.frames()) {
                    String url = frame.url();
                    if (isCrossOrigin(url) && !hiddenFrameUrls.contains(url) && !isAdUrl(url)) {
                        iframeUrls.add(url);
                    }
                }
            } catch (PlaywrightException e) {
                log.error("Error getting cross-origin iframes: " + e.getMessage());
            }
            return iframeUrls;
        });
    }

    private Map.Entry<DOMElementNode, Map<Integer, DOMElementNode>> buildDomTree(
        boolean highlightElements, int focusElement, int viewportExpansion) {

        try {
            if (!"2".equals(page.evaluate("1+1").toString())) {
                throw new RuntimeException("The page cannot evaluate javascript code properly");
            }

            if ("about:blank".equals(page.url())) {
                return Map.entry(
                        new DOMElementNode(
                                "body", "", new HashMap<>(), new ArrayList<>(),
                                false, false, false, false, null, false, null, null
                        ),
                        new HashMap<>()
                );
            }

            Map<String, Object> args = new HashMap<>();
            args.put("doHighlightElements", highlightElements);
            args.put("focusHighlightIndex", focusElement);
            args.put("viewportExpansion", viewportExpansion);
            args.put("debugMode", true);

            Map<String, Object> evalResult = (Map)page.evaluate(jsCode, args);
            return constructDomTree(evalResult);
        } catch (PlaywrightException e) {
            log.error("Error building DOM tree: " + e.getMessage());
            throw new RuntimeException("Failed to build DOM tree", e);
        }
    }

    private Map.Entry<DOMElementNode, Map<Integer, DOMElementNode>> constructDomTree(Map<String, Object> evalResult) {
        Map<String, Map<String, Object>> jsNodeMap = (Map<String, Map<String, Object>>) evalResult.get("map");
        String jsRootId = evalResult.get("rootId").toString();

        Map<Integer, DOMElementNode> selectorMap = new HashMap<>();
        Map<String, DOMBaseNode> nodeMap = new HashMap<>();

        jsNodeMap.forEach((id, nodeData) -> {
            Map.Entry<DOMBaseNode, List<String>> parsedNode = parseNode(nodeData);
            if (parsedNode.getKey() == null) {
                return;
            }

            nodeMap.put(id, parsedNode.getKey());

            if (parsedNode.getKey() instanceof DOMElementNode) {
                DOMElementNode elementNode = (DOMElementNode) parsedNode.getKey();
                if (elementNode.getHighlightIndex() != null) {
                    selectorMap.put(elementNode.getHighlightIndex(), elementNode);
                }

                parsedNode.getValue().forEach(childId -> {
                    DOMBaseNode childNode = nodeMap.get(childId);
                    if (childNode != null) {
                        childNode.setParent(elementNode);
                        elementNode.getChildren().add(childNode);
                    }
                });
            }
        });

        DOMElementNode rootNode = (DOMElementNode) nodeMap.get(jsRootId);
        if (rootNode == null) {
            throw new RuntimeException("Failed to construct DOM tree: root node not found");
        }

        return Map.entry(rootNode, selectorMap);
    }

    private Map.Entry<DOMBaseNode, List<String>> parseNode(Map<String, Object> nodeData) {
        if (nodeData == null || nodeData.isEmpty()) {
            return Map.entry(null, new ArrayList<>());
        }

        // Handle text nodes
        if ("TEXT_NODE".equals(nodeData.get("type"))) {
            DOMTextNode textNode = new DOMTextNode(
                    (String) nodeData.get("text"),
                    (boolean) nodeData.getOrDefault("isVisible", false),
                    null
            );
            return Map.entry(textNode, new ArrayList<>());
        }

        // Handle element nodes
        ViewportInfo viewportInfo = null;
        if (nodeData.containsKey("viewport")) {
            Map<String, Object> viewportData = (Map<String, Object>) nodeData.get("viewport");
            viewportInfo = new ViewportInfo(
                    ((Number) viewportData.get("width")).intValue(),
                    ((Number) viewportData.get("height")).intValue()
            );
        }

        DOMElementNode elementNode = new DOMElementNode(
                (String) nodeData.get("tagName"),
                (String) nodeData.get("xpath"),
                (Map<String, String>) nodeData.getOrDefault("attributes", new HashMap<>()),
                new ArrayList<DOMBaseNode>(),
                (boolean) nodeData.getOrDefault("isVisible", false),
                (boolean) nodeData.getOrDefault("isInteractive", false),
                (boolean) nodeData.getOrDefault("isTopElement", false),
                (boolean) nodeData.getOrDefault("isInViewport", false),
                nodeData.containsKey("highlightIndex") ? ((Number) nodeData.get("highlightIndex")).intValue() : null,
                (boolean) nodeData.getOrDefault("shadowRoot", false),
                null,
                new dev.langchain4j.example.entity.dom.history_tree_processor._view.ViewportInfo(0, 0, viewportInfo.getWidth(), viewportInfo.getHeight())
        );

        List<String> childrenIds = (List<String>) nodeData.getOrDefault("children", new ArrayList<>());
        return Map.entry(elementNode, childrenIds);
    }

    private boolean isCrossOrigin(String url) {
        String currentHost = page.url().split("//")[1].split("/")[0];
        String frameHost = url.split("//")[1].split("/")[0];
        return !frameHost.equals(currentHost);
    }

    private boolean isAdUrl(String url) {
        String host = url.split("//")[1].split("/")[0];
        return host.contains("doubleclick.net") ||
                host.contains("adroll.com") ||
                host.contains("googletagmanager.com");
    }
}
