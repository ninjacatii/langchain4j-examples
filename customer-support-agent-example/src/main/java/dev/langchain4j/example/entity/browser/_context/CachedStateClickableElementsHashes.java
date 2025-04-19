package dev.langchain4j.example.entity.browser._context;

import lombok.Data;

import java.util.Set;

@Data
public class CachedStateClickableElementsHashes {
    private String url;
    private Set<String> hashes;
}
