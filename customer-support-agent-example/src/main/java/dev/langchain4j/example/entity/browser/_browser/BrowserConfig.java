package dev.langchain4j.example.entity.browser._browser;

import dev.langchain4j.example.entity.browser._context.BrowserContextConfig;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class BrowserConfig {
    private String wssUrl;
    private String cdpUrl;
    @Builder.Default
    private String browserClass = "chromium";
    private String browserBinaryPath;
    @Builder.Default
    private List<String> extraBrowserArgs = new ArrayList<>();
    @Builder.Default
    private boolean headless = false;
    @Builder.Default
    private boolean disableSecurity = false;
    @Builder.Default
    private boolean deterministicRendering = false;
    @Builder.Default
    private boolean keepAlive = false;
    private ProxySettings proxy;
    @Builder.Default
    private BrowserContextConfig newContextConfig = BrowserContextConfig.builder().build();
}
