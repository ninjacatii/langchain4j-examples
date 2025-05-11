package dev.langchain4j.example.entity.browser._browser;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import dev.langchain4j.example.entity.browser._context.BrowserContext;
import dev.langchain4j.example.entity.browser._context.BrowserContextConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
public class Browser implements AutoCloseable {
    private final BrowserConfig config;
    private Playwright playwright;
    private com.microsoft.playwright.Browser playwrightBrowser;
    private Process chromeProcess;

    public Browser(BrowserConfig config) {
        this.config = config != null ? config : BrowserConfig.builder().build();
    }

    public BrowserContext newContext(BrowserContextConfig config) {
        BrowserContextConfig mergedConfig = mergeConfigs(config);
        return new BrowserContext(this, mergedConfig, null);
    }

    private BrowserContextConfig mergeConfigs(BrowserContextConfig config) {
        // Merge browser and context configs
        return config != null ? config : BrowserContextConfig.builder().build();
    }

    public com.microsoft.playwright.Browser getPlaywrightBrowser() {
        if (playwrightBrowser != null) {
            return playwrightBrowser;
        }
        return initBrowser();
    }

    private com.microsoft.playwright.Browser initBrowser() {
        try {
            playwright = Playwright.create();

            if (config.getCdpUrl() != null) {
                playwrightBrowser = setupRemoteCdpBrowser();
            } else if (config.getWssUrl() != null) {
                playwrightBrowser = setupRemoteWssBrowser();
            } else if (config.getBrowserBinaryPath() != null) {
                playwrightBrowser = setupUserProvidedBrowser();
            } else {
                playwrightBrowser = setupBuiltinBrowser();
            }

            return playwrightBrowser;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize browser", e);
        }
    }

    private com.microsoft.playwright.Browser setupRemoteCdpBrowser() {
        if (config.getBrowserClass().equals("firefox")) {
            throw new IllegalArgumentException("Firefox no longer supports CDP");
        }
        return playwright.chromium().connectOverCDP(config.getCdpUrl());
    }

    private com.microsoft.playwright.Browser setupRemoteWssBrowser() {
        return getBrowserClass().connect(config.getWssUrl());
    }

    private com.microsoft.playwright.Browser setupUserProvidedBrowser() {
        // Implementation for user-provided browser binary
        // Would include process management similar to Python version
        return null;
    }

    private com.microsoft.playwright.Browser setupBuiltinBrowser() {
        BrowserType browserType = getBrowserClass();
        //IN_DOCKER以后要写在配置文件中，现在暂时先全设为false
        List<String> args = getBrowserArgs(config.isHeadless(), false, config.isDisableSecurity());

        return browserType.launch(new BrowserType.LaunchOptions()
                .setHeadless(config.isHeadless())
                .setArgs(args)
                //.setProxy(null)
                .setHandleSIGTERM(false)
                .setHandleSIGINT(false));
    }

    private BrowserType getBrowserClass() {
        switch (config.getBrowserClass()) {
            case "chromium": return playwright.chromium();
            case "firefox": return playwright.firefox();
            case "webkit": return playwright.webkit();
            default: throw new IllegalArgumentException("Unsupported browser class");
        }
    }

    private List<String> getBrowserArgs(boolean headless, boolean docker, boolean disableSecurity) {
        return ChromeConfig.getAllArgs(headless, docker, disableSecurity);
    }

    public void close() {
        if (config.isKeepAlive()) return;

        try {
            if (playwrightBrowser != null) {
                playwrightBrowser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
            if (chromeProcess != null) {
                chromeProcess.destroy();
            }
        } finally {
            playwrightBrowser = null;
            playwright = null;
            chromeProcess = null;
            System.gc();
        }
    }
}
