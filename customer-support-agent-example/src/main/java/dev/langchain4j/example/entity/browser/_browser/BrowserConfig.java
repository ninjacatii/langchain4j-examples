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
    private String browserClass = "chromium";
    private String browserBinaryPath;
    private List<String> extraBrowserArgs = new ArrayList<>();
    private boolean headless = false;
    private boolean disableSecurity = false;
    private boolean deterministicRendering = false;
    private boolean keepAlive = false;
    private ProxySettings proxy;
    private BrowserContextConfig newContextConfig = new BrowserContextConfig();
}
