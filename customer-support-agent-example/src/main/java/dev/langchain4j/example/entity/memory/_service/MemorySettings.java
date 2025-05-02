package dev.langchain4j.example.entity.memory._service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class MemorySettings {
    private String agentId;
    private int interval = 10;
    private Map<String, Object> config;
}
