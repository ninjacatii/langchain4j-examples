package dev.langchain4j.example.tests;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.microsoft.playwright.Page;
import dev.langchain4j.example.entity.browser._browser.Browser;
import dev.langchain4j.example.entity.browser._browser.BrowserConfig;
import dev.langchain4j.example.entity.browser._browser.ProxySettings;
import dev.langchain4j.example.entity.browser._context.BrowserContext;
import dev.langchain4j.example.entity.browser._context.BrowserContextConfig;
import dev.langchain4j.example.entity.browser._context.BrowserContextWindowSize;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class TestBrowserConfigModels {

    @Test
    void testProxySettingsModel() {
        ProxySettings proxySettings = ProxySettings.builder()
                .server("http://example.proxy:8080")
                .bypass("localhost")
                .username("testuser")
                .password("testpass")
                .build();

        assertEquals("http://example.proxy:8080", proxySettings.getServer());
        assertEquals("localhost", proxySettings.getBypass());


        JSONObject proxyMap = proxySettings.modelDump();
        assertTrue(proxyMap.containsKey("server"));
        assertEquals("http://example.proxy:8080", proxyMap.get("server"));
        assertEquals("localhost", proxyMap.get("bypass"));
        assertEquals("testuser", proxyMap.get("username"));
        assertEquals("testpass", proxyMap.get("password"));
    }

    @Test
    void testWindowSizeModel() {
        BrowserContextWindowSize windowSize = new BrowserContextWindowSize(1280, 1100);

        assertEquals(1280, windowSize.width);
        assertEquals(1100, windowSize.height);

        JSONObject sizeMap = windowSize.modelDump();
        assertEquals(1280, sizeMap.get("width"));
        assertEquals(1100, sizeMap.get("height"));
    }

    @Test
    @Disabled("Skip browser test in CI")
    void testWindowSizeWithRealBrowser() {
        BrowserContextWindowSize windowSize = new BrowserContextWindowSize(1024, 768);

        BrowserConfig browserConfig = BrowserConfig.builder().headless(true).build();

        BrowserContextConfig contextConfig = BrowserContextConfig.builder()
                .browserWindowSize(windowSize)
                .maximumWaitPageLoadTime(2.0f)
                .minimumWaitPageLoadTime(0.2f)
                .noViewport(true)
                .build();

        try (Browser browser = new Browser(browserConfig)) {
            com.microsoft.playwright.Browser playwrightBrowser = browser.getPlaywrightBrowser();
            assertNotNull(playwrightBrowser);

            BrowserContext browserContext = new BrowserContext(browser, contextConfig, null);
            browserContext.initializeSession();
            Page page = browserContext.getCurrentPage();
            assertNotNull(page);

            Object videoSize = page.evaluate("""
                    () => {
                        // This returns information about the context recording settings
                        // which should match our configured video size (browser_window_size)
                        try {
                            const settings = window.getPlaywrightContextSettings ?\s
                                window.getPlaywrightContextSettings() : null;
                            if (settings && settings.recordVideo) {
                                return settings.recordVideo.size;
                            }
                        } catch (e) {}
                    
                        // Fallback to window dimensions
                        return {
                            width: window.innerWidth,
                            height: window.innerHeight
                        };
                    }
                    """);

            Object viewportSize = page.evaluate("""
                () => {
                    return {
                        width: window.innerWidth,
                        height: window.innerHeight
                    }
                }
                """);

            log.info("Window size config: {}", JSONUtil.toJsonStr(windowSize));
            log.info("Browser viewport size: {}", JSONUtil.toJsonStr(viewportSize));
        }
    }

    @Test
    void testProxyWithRealBrowser() {
        ProxySettings proxySettings = ProxySettings.builder()
                .server("http://non.existent.proxy:9999")
                .bypass("localhost")
                .username("testuser")
                .password("testpass")
                .build();

        BrowserConfig browserConfig = BrowserConfig.builder()
                .headless(true)
                .proxy(proxySettings)
                .build();

        try (Browser browser = new Browser(browserConfig)) {
            // Just verify browser can initialize with proxy settings
            assertNotNull(browser);
        } catch (Exception e) {
            assertFalse(e.getMessage().contains("Proxy configuration error"),
                    "Should not throw proxy configuration error");
        }
    }
}
