package dev.langchain4j.example.entity.browser._context;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.Cookie;
import dev.langchain4j.example.entity.browser._browser.Browser;

public class BrowserContext implements AutoCloseable {
    private final String contextId = UUID.randomUUID().toString();
    private final Browser browser;
    private final BrowserContextConfig config;
    private final BrowserContextState state;
    private BrowserSession session;
    private Page activeTab;
    private Runnable pageEventHandler;

    public BrowserContext(Browser browser, BrowserContextConfig config, BrowserContextState state) {
        this.browser = browser;
        this.config = config != null ? config : new BrowserContextConfig();
        this.state = state != null ? state : new BrowserContextState();
    }

    public CompletableFuture<Void> initializeSession() {
        return CompletableFuture.runAsync(() -> {
            try {
                com.microsoft.playwright.BrowserContext playwrightContext = createContext(browser.getPlaywrightBrowser()).join();
                this.session = new BrowserSession(playwrightContext, null);
                this.activeTab = findOrCreateActiveTab().join();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    private CompletableFuture<com.microsoft.playwright.BrowserContext> createContext(Browser playwrightBrowser) {
        return CompletableFuture.supplyAsync(() -> {
            Browser.NewContextOptions options = new Browser.NewContextOptions()
                    .setUserAgent(config.getUserAgent())
                    .setJavaScriptEnabled(true)
                    .setBypassCSP(config.isDisableSecurity())
                    .setIgnoreHTTPSErrors(config.isDisableSecurity())
                    .setRecordVideoDir(config.getSaveRecordingPath() != null ? Paths.get(config.getSaveRecordingPath()) : null)
                    .setRecordVideoSize(new BrowserContextWindowSize(config.getBrowserWindowSize().getWidth(),
                            config.getBrowserWindowSize().getHeight()))
                    .setRecordHarPath(config.getSaveHarPath() != null ? Paths.get(config.getSaveHarPath()) : null)
                    .setLocale(config.getLocale())
                    .setHttpCredentials(config.getHttpCredentials())
                    .setIsMobile(config.getIsMobile())
                    .setHasTouch(config.getHasTouch())
                    .setGeolocation(config.getGeolocation())
                    .setPermissions(config.getPermissions())
                    .setTimezoneId(config.getTimezoneId());

            com.microsoft.playwright.BrowserContext context = playwrightBrowser.newContext(options);

            // Load cookies if available
            if (config.getCookiesFile() != null && Files.exists(Paths.get(config.getCookiesFile()))) {
                try {
                    String cookiesJson = new String(Files.readAllBytes(Paths.get(config.getCookiesFile())));
                    context.addCookies(Arrays.asList(new Cookie[0])); // TODO: Parse cookies JSON
                } catch (Exception e) {
                    // Log error
                }
            }

            // Add anti-detection scripts
            context.addInitScript("""
                // Webdriver property
                Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
                
                // Languages
                Object.defineProperty(navigator, 'languages', { get: () => ['en-US'] });
                
                // Plugins
                Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });
                
                // Chrome runtime
                window.chrome = { runtime: {} };
                
                // Permissions
                const originalQuery = window.navigator.permissions.query;
                window.navigator.permissions.query = (parameters) => (
                    parameters.name === 'notifications' ?
                        Promise.resolve({ state: Notification.permission }) :
                        originalQuery(parameters)
                );
                
                // Shadow DOM
                (function () {
                    const originalAttachShadow = Element.prototype.attachShadow;
                    Element.prototype.attachShadow = function attachShadow(options) {
                        return originalAttachShadow.call(this, { ...options, mode: "open" });
                    };
                })();
            """);

            return context;
        });
    }

    private CompletableFuture<Page> findOrCreateActiveTab() {
        return CompletableFuture.supplyAsync(() -> {
            List<Page> pages = session.getContext().pages();

            // Try to find existing page
            if (state.getTargetId() != null) {
                // TODO: Implement CDP target lookup
            }

            // Use first available page or create new
            if (!pages.isEmpty()) {
                return pages.get(0);
            } else {
                Page newPage = session.getContext().newPage();
                newPage.navigate("about:blank");
                return newPage;
            }
        });
    }

    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (session != null) {
                    // Clean up resources
                    if (pageEventHandler != null) {
                        session.getContext().removeListener("page", pageEventHandler);
                    }

                    // Stop tracing if enabled
                    if (config.getTracePath() != null) {
                        session.getContext().tracing().stop(new Tracing.StopOptions()
                                .setPath(Paths.get(config.getTracePath(), contextId + ".zip")));
                    }

                    // Close context if not keeping alive
                    if (!config.isKeepAlive()) {
                        session.getContext().close();
                    }
                }
            } finally {
                session = null;
                activeTab = null;
                pageEventHandler = null;
            }
        });
    }

    // Additional browser operations would be implemented here...

    @Override
    public void close() {
        close().join();
    }
}
