package dev.langchain4j.example.exception;

import lombok.Getter;

@Getter
public class LLMException extends RuntimeException {
    private final int statusCode;

    public LLMException(int statusCode, String message) {
        super(String.format("Error %d: %s", statusCode, message));
        this.statusCode = statusCode;
    }

    // 支持异常链的构造方法
    public LLMException(int statusCode, String message, Throwable cause) {
        super(String.format("Error %d: %s", statusCode, message), cause);
        this.statusCode = statusCode;
    }
}
