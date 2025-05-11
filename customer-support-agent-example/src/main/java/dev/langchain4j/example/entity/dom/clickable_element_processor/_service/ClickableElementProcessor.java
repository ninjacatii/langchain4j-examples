package dev.langchain4j.example.entity.dom.clickable_element_processor._service;

import dev.langchain4j.example.entity.dom._views.DOMBaseNode;
import dev.langchain4j.example.entity.dom._views.DOMElementNode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ClickableElementProcessor {
    public static Set<String> getClickableElementsHashes(DOMElementNode domElement) {
        List<DOMElementNode> clickableElements = getClickableElements(domElement);
        Set<String> hashes = new HashSet<>();
        for (DOMElementNode element : clickableElements) {
            hashes.add(hashDomElement(element));
        }
        return hashes;
    }

    public static List<DOMElementNode> getClickableElements(DOMElementNode domElement) {
        List<DOMElementNode> clickableElements = new ArrayList<>();
        for (DOMBaseNode child : domElement.getChildren()) {
            if (child instanceof DOMElementNode) {
                DOMElementNode child1 = (DOMElementNode)child;
                if (child1.getHighlightIndex() != null) {
                    clickableElements.add(child1);
                }
                clickableElements.addAll(getClickableElements(child1));
            }
        }
        return clickableElements;
    }

    public static String hashDomElement(DOMElementNode domElement) {
        List<String> parentBranchPath = getParentBranchPath(domElement);
        String branchPathHash = parentBranchPathHash(parentBranchPath);
        String attributesHash = attributesHash(domElement.getAttributes());
        String xpathHash = xpathHash(domElement.getXpath());

        return hashString(branchPathHash + "-" + attributesHash + "-" + xpathHash);
    }

    private static List<String> getParentBranchPath(DOMElementNode domElement) {
        List<DOMElementNode> parents = new ArrayList<>();
        DOMElementNode currentElement = domElement;
        while (currentElement.getParent() != null) {
            parents.add(currentElement);
            currentElement = currentElement.getParent();
        }

        List<String> tagNames = new ArrayList<>();
        for (DOMElementNode parent : parents) {
            tagNames.add(parent.getTagName());
        }
        return tagNames;
    }

    private static String parentBranchPathHash(List<String> parentBranchPath) {
        String parentBranchPathString = String.join("/", parentBranchPath);
        return hashString(parentBranchPathString);
    }

    private static String attributesHash(Map<String, String> attributes) {
        StringBuilder attributesString = new StringBuilder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            attributesString.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return hashString(attributesString.toString());
    }

    private static String xpathHash(String xpath) {
        return hashString(xpath);
    }

    private static String hashString(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(string.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
