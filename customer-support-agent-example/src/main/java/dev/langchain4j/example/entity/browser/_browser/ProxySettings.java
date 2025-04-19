package dev.langchain4j.example.entity.browser._browser;

import lombok.Data;

@Data
public class ProxySettings {
    private String server;
    private String bypass;
    private String username;
    private String password;
}
