package dev.langchain4j.example.entity.dom.history_tree_processor._view;

import lombok.Data;

@Data
public class HashedDomElement {
    private String branchPathHash;
    private String attributesHash;
    private String xpathHash;
}
