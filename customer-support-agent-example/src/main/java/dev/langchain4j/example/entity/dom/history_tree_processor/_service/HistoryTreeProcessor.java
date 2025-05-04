package dev.langchain4j.example.entity.dom.history_tree_processor._service;

import dev.langchain4j.example.entity.dom._views.DOMBaseNode;
import dev.langchain4j.example.entity.dom._views.DOMElementNode;
import dev.langchain4j.example.entity.dom.history_tree_processor._view.DOMHistoryElement;
import dev.langchain4j.example.entity.dom.history_tree_processor._view.HashedDomElement;
import dev.langchain4j.example.entity.browser._context.BrowserContext;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HistoryTreeProcessor {
    private static final String HASH_ALGORITHM = "SHA-256";

    public static DOMHistoryElement convertDomElementToHistoryElement(DOMElementNode domElement) {
        List<String> parentBranchPath = getParentBranchPath(domElement);
        String cssSelector = BrowserContext.enhancedCssSelectorForElement(domElement, true);

        return new DOMHistoryElement(
                domElement.getTagName(),
                domElement.getXpath(),
                domElement.getHighlightIndex(),
                parentBranchPath,
                domElement.getAttributes(),
                domElement.isShadowRoot(),
                cssSelector,
                domElement.getPageCoordinates(),
                domElement.getViewportCoordinates(),
                domElement.getViewportInfo()
        );
    }

    public static DOMElementNode findHistoryElementInTree(DOMHistoryElement domHistoryElement, DOMElementNode tree) {
        HashedDomElement hashedDomHistoryElement = hashDomHistoryElement(domHistoryElement);
        return processNode(tree, hashedDomHistoryElement);
    }

    private static DOMElementNode processNode(DOMElementNode node, HashedDomElement targetHash) {
        if (node.getHighlightIndex() != null) {
            HashedDomElement hashedNode = hashDomElement(node);
            if (hashedNode.equals(targetHash)) {
                return node;
            }
        }

        for (DOMBaseNode child : node.getChildren()) {
            if (child instanceof DOMElementNode) {
                DOMElementNode result = processNode((DOMElementNode)child, targetHash);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static boolean compareHistoryElementAndDomElement(DOMHistoryElement domHistoryElement, DOMElementNode domElement) {
        HashedDomElement hashedDomHistoryElement = hashDomHistoryElement(domHistoryElement);
        HashedDomElement hashedDomElement = hashDomElement(domElement);
        return hashedDomHistoryElement.equals(hashedDomElement);
    }

    public static HashedDomElement hashDomHistoryElement(DOMHistoryElement domHistoryElement) {
        String branchPathHash = parentBranchPathHash(domHistoryElement.getEntireParentBranchPath());
        String attributesHash = attributesHash(domHistoryElement.getAttributes());
        String xpathHash = xpathHash(domHistoryElement.getXpath());
        return new HashedDomElement(branchPathHash, attributesHash, xpathHash);
    }

    public static HashedDomElement hashDomElement(DOMElementNode domElement) {
        List<String> parentBranchPath = getParentBranchPath(domElement);
        String branchPathHash = parentBranchPathHash(parentBranchPath);
        String attributesHash = attributesHash(domElement.getAttributes());
        String xpathHash = xpathHash(domElement.getXpath());
        return new HashedDomElement(branchPathHash, attributesHash, xpathHash);
    }

    public static List<String> getParentBranchPath(DOMElementNode domElement) {
        List<DOMElementNode> parents = new ArrayList<>();
        DOMElementNode currentElement = domElement;
        while (currentElement.getParent() != null) {
            parents.add(currentElement);
            currentElement = currentElement.getParent();
        }

        Collections.reverse(parents);
        return parents.stream()
                .map(DOMElementNode::getTagName)
                .collect(Collectors.toList());
    }

    private static String parentBranchPathHash(List<String> parentBranchPath) {
        String parentBranchPathString = String.join("/", parentBranchPath);
        return sha256Hash(parentBranchPathString);
    }

    private static String attributesHash(Map<String, String> attributes) {
        StringBuilder attributesString = new StringBuilder();
        attributes.forEach((key, value) -> attributesString.append(key).append("=").append(value));
        return sha256Hash(attributesString.toString());
    }

    private static String xpathHash(String xpath) {
        return sha256Hash(xpath);
    }

    private static String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(input.getBytes());
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute hash", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
