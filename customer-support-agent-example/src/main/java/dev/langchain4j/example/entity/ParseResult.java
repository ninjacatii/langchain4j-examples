package dev.langchain4j.example.entity;

public record ParseResult(String scheme, String netloc, String path, String params, String query, String fragment) {
}
