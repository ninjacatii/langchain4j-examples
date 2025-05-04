package dev.langchain4j.example.entity.browser._context;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import dev.langchain4j.example.entity.ParseResult;
import dev.langchain4j.example.entity.browser._browser.Browser;
import dev.langchain4j.example.entity.browser._views.BrowserError;
import dev.langchain4j.example.entity.browser._views.BrowserState;
import dev.langchain4j.example.entity.browser._views.TabInfo;
import dev.langchain4j.example.entity.browser._views.URLNotAllowedError;
import dev.langchain4j.example.entity.dom._service.DomService;
import dev.langchain4j.example.entity.dom._views.DOMBaseNode;
import dev.langchain4j.example.entity.dom._views.DOMElementNode;
import dev.langchain4j.example.entity.dom._views.DOMState;
import dev.langchain4j.example.util.dom.history_tree_processor.Utils;
import dev.langchain4j.example.entity.dom.clickable_element_processor._service.ClickableElementProcessor;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.util.Tuple;

@Slf4j
public class BrowserContext implements AutoCloseable {
    private final String contextId = UUID.randomUUID().toString();
    private final Browser browser;
    private final BrowserContextConfig config;
    private final BrowserContextState state;
    private BrowserSession session;
    private Page activeTab;
    private Consumer<Page> pageEventHandler;
    private BrowserState currentState;

    public BrowserContext(Browser browser, BrowserContextConfig config, BrowserContextState state) {
        this.browser = browser;
        this.config = config != null ? config : new BrowserContextConfig();
        this.state = state != null ? state : new BrowserContextState();
    }

    private BrowserSession initializeSession() {
        com.microsoft.playwright.Browser playwrightBrowser = this.browser.getPlaywrightBrowser();
        com.microsoft.playwright.BrowserContext context = this.createContext(playwrightBrowser);
        this.pageEventHandler = null;
        List<Page> pages = context.pages();
        this.session = new BrowserSession(context, null);

        Page activePage = null;
        if (!StrUtil.isBlank(this.browser.getConfig().getCdpUrl())) {
            if (!StrUtil.isBlank(this.state.getTargetId())) {
                List<Hashtable<String, String>> targets = this.getCdpTargets();
                for (Hashtable<String, String> target : targets) {
                    if (!StrUtil.isBlank(this.state.getTargetId()) && this.state.getTargetId().equals(target.get("targetId"))) {
                        for (Page page : pages) {
                            if (!StrUtil.isBlank(page.url()) && page.url().equals(target.get("url"))) {
                                activePage = page;
                                break;
                            }
                        }
                    }

                }
            }
        }
        if (activePage == null) {
            if (!CollUtil.isEmpty(pages) &&
                    !StrUtil.isBlank(pages.get(0).url()) &&
                    !pages.get(0).url().startsWith("hrome://") &&
                    !pages.get(0).url().startsWith("chrome-extension://")) {
                activePage = pages.get(0);
                log.debug("🔍  Using existing page: {}", activePage.url());
            } else {
                activePage = context.newPage();
                activePage.navigate("about:blank");
                log.debug("🆕  Created new page: {}", activePage.url());
            }

            if (!StrUtil.isBlank(this.browser.getConfig().getCdpUrl())) {
                List<Hashtable<String, String>> targets = this.getCdpTargets();
                for (Hashtable<String, String> target : targets) {
                    if (activePage.url().equals(target.get("url"))) {
                        this.state.setTargetId(target.get("targetId"));
                        break;
                    }
                }
            }
        }

        log.debug("🫨  Bringing tab to front: {}", activePage);
        activePage.bringToFront();
        activePage.waitForLoadState(LoadState.LOAD);

        this.activeTab = activePage;
        return this.session;
    }

    private void addNewPageListener(com.microsoft.playwright.BrowserContext context) {
        Consumer<Page> onPage = (page) -> {
            if (!StrUtil.isBlank(this.browser.getConfig().getCdpUrl())) {
                page.reload();
            }
            page.waitForLoadState();
            log.debug("\uD83D\uDCD1  New page opened: " + page.url());

            if (!page.url().startsWith("chrome-extension://") && !page.url().startsWith("chrome://")) {
                this.activeTab = page;
            }

            if (this.session != null) {
                this.state.setTargetId(null);
            }
        };
        this.pageEventHandler = onPage;
        context.onPage(onPage);
    }

    public BrowserSession getSession() {
        if (this.session == null) {
            return this.initializeSession();
        }
        return this.session;
    }

    public Page getCurrentPage() {
        BrowserSession session = this.getSession();
        return this.getCurrentPage(session);
    }

    private com.microsoft.playwright.BrowserContext createContext(com.microsoft.playwright.Browser browser) {
        com.microsoft.playwright.BrowserContext context = null;
        if (!StrUtil.isBlank(this.browser.getConfig().getCdpUrl()) && browser.contexts().size() > 0) {
            context = browser.contexts().get(0);
        } else if (!StrUtil.isBlank(this.browser.getConfig().getBrowserBinaryPath()) && browser.contexts().size() > 0) {
            context = browser.contexts().get(0);
        } else {
            var options = new com.microsoft.playwright.Browser.NewContextOptions();
            options.setUserAgent(this.config.getUserAgent());
            options.setJavaScriptEnabled(true);
            options.setBypassCSP(this.config.isDisableSecurity());
            options.setIgnoreHTTPSErrors(this.config.isDisableSecurity());
            options.setRecordVideoDir(this.config.getSaveRecordingPath());
            options.setRecordVideoSize(ObjectUtil.cloneByStream(this.config.getBrowserWindowSize()));
            options.setRecordHarPath(this.config.getSaveHarPath());
            options.setLocale(this.config.getLocale());
            options.setHttpCredentials(this.config.getHttpCredentials());
            options.setIsMobile(this.config.getIsMobile());
            options.setHasTouch(this.config.getHasTouch());
            options.setGeolocation(this.config.getGeolocation());
            options.setPermissions(this.config.getPermissions());
            options.setTimezoneId(this.config.getTimezoneId());
            context = browser.newContext(options);
        }
        if (!StrUtil.isBlank(this.config.getTracePath())) {
            context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
        }
        if (!StrUtil.isBlank(this.config.getCookiesFile()) && FileUtil.exist(this.config.getCookiesFile())) {
            String f = FileUtil.readString(this.config.getCookiesFile(), StandardCharsets.UTF_8);
            List<Cookie> cookies = JSONUtil.toList(f, Cookie.class);
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cookie = cookies.get(i);
                if (cookie.sameSite != null) {
                    cookie.sameSite = SameSiteAttribute.NONE;
                }
            }
            log.info("\uD83C\uDF6A  Loaded " + cookies.size() + " cookies from " + this.config.getCookiesFile());
            context.addCookies(cookies);
        }

        context.addInitScript(
                "            // Webdriver property\n" +
                "            Object.defineProperty(navigator, 'webdriver', {\n" +
                "                get: () => undefined\n" +
                "            });\n" +
                "\n" +
                "            // Languages\n" +
                "            Object.defineProperty(navigator, 'languages', {\n" +
                "                get: () => ['en-US']\n" +
                "            });\n" +
                "\n" +
                "            // Plugins\n" +
                "            Object.defineProperty(navigator, 'plugins', {\n" +
                "                get: () => [1, 2, 3, 4, 5]\n" +
                "            });\n" +
                "\n" +
                "            // Chrome runtime\n" +
                "            window.chrome = { runtime: {} };\n" +
                "\n" +
                "            // Permissions\n" +
                "            const originalQuery = window.navigator.permissions.query;\n" +
                "            window.navigator.permissions.query = (parameters) => (\n" +
                "                parameters.name === 'notifications' ?\n" +
                "                    Promise.resolve({ state: Notification.permission }) :\n" +
                "                    originalQuery(parameters)\n" +
                "            );\n" +
                "            (function () {\n" +
                "                const originalAttachShadow = Element.prototype.attachShadow;\n" +
                "                Element.prototype.attachShadow = function attachShadow(options) {\n" +
                "                    return originalAttachShadow.call(this, { ...options, mode: \"open\" });\n" +
                "                };\n" +
                "            })();");
        return context;
    }

    private void waitForStableNetwork() {
        Page page = this.getCurrentPage();

        var pendingRequests = new HashSet<Request>();
        LastActivity lastActivity = new LastActivity();
        lastActivity.setTime(DateTime.now());

        var RELEVANT_RESOURCE_TYPES = new HashSet<String>(Arrays.asList(
                "document",
                "stylesheet",
                "image",
                "font",
                "script",
                "iframe"
        ));

        var RELEVANT_CONTENT_TYPES = new HashSet<String>(Arrays.asList(
                "text/html",
                "text/css",
                "application/javascript",
                "image/",
                "font/",
                "application/json"
        ));

        var IGNORED_URL_PATTERNS = new HashSet<String>(Arrays.asList(
                // Analytics and tracking
                "analytics",
                "tracking",
                "telemetry",
                "beacon",
                "metrics",
			    // Ad-related
                "doubleclick",
                "adsystem",
                "adserver",
                "advertising",
			    // Social media widgets
                "facebook.com/plugins",
                "platform.twitter",
                "linkedin.com/embed",
			    // Live chat and support
                "livechat",
                "zendesk",
                "intercom",
                "crisp.chat",
                "hotjar",
			    // Push notifications
                "push-notifications",
                "onesignal",
                "pushwoosh",
			    // Background sync/heartbeat
                "heartbeat",
                "ping",
                "alive",
			    // WebRTC and streaming
                "webrtc",
                "rtmp://",
                "wss://",
			    // Common CDNs for dynamic content
                "cloudfront.net",
                "fastly.net"
        ));
        Consumer<Request> onRequest = (request) -> {
            if (!RELEVANT_RESOURCE_TYPES.contains(request.resourceType())) {
                return;
            }

            if (StrUtil.equalsAnyIgnoreCase(request.resourceType(),
                    "websocket",
                    "media",
                    "eventsource",
                    "manifest",
                    "other")) {
                return;
            }

            String url = request.url().toLowerCase();
            if (StrUtil.containsAnyIgnoreCase(url,
                    // Analytics and tracking
                    "analytics",
                    "tracking",
                    "telemetry",
                    "beacon",
                    "metrics",
                    // Ad-related
                    "doubleclick",
                    "adsystem",
                    "adserver",
                    "advertising",
                    // Social media widgets
                    "facebook.com/plugins",
                    "platform.twitter",
                    "linkedin.com/embed",
                    // Live chat and support
                    "livechat",
                    "zendesk",
                    "intercom",
                    "crisp.chat",
                    "hotjar",
                    // Push notifications
                    "push-notifications",
                    "onesignal",
                    "pushwoosh",
                    // Background sync/heartbeat
                    "heartbeat",
                    "ping",
                    "alive",
                    // WebRTC and streaming
                    "webrtc",
                    "rtmp://",
                    "wss://",
                    // Common CDNs for dynamic content
                    "cloudfront.net",
                    "fastly.net")) {
                return;
            }
            if (StrUtil.startWithAnyIgnoreCase(url, "data:", "blob:")) {
                return;
            }
            Map<String, String> headers = request.headers();
            if ("prefetch".equals(headers.get("purpose")) || StrUtil.equalsAnyIgnoreCase(headers.get("sec-fetch-dest"), "video", "audio")) {
                return;
            }
            pendingRequests.add(request);
            lastActivity.setTime(DateUtil.date());
        };
        Consumer<Response> onResponse = (response) -> {
            Request request = response.request();
            if (!pendingRequests.contains(request)) {
                return;
            }

            String contentType = response.headers().getOrDefault("content-type", "").toLowerCase();
            if (StrUtil.equalsAnyIgnoreCase(contentType,
                    "streaming",
                    "video",
                    "audio",
                    "webm",
                    "mp4",
                    "event-stream",
                    "websocket",
                    "protobuf")) {
                pendingRequests.remove(request);
                return;
            }

            if (!RELEVANT_CONTENT_TYPES.contains(contentType)) {
                pendingRequests.remove(request);
                return;
            }

            // Skip if response is too large (likely not essential for page load)
            String contentLength = response.headers().get("content-type");
            if (contentLength != null && Integer.parseInt(contentLength) > 5 * 1024 * 1024) {
                pendingRequests.remove(request);
                return;
            }

            pendingRequests.remove(request);
            lastActivity.setTime(DateUtil.date());
        };

        page.onRequest(onRequest);
        page.onResponse(onResponse);

        try {
            DateTime startTime = DateUtil.date();
            while (true) {
                ThreadUtil.sleep(0.1, TimeUnit.SECONDS);
                DateTime now = DateUtil.date();
                if (pendingRequests.isEmpty() && DateUtil.between (lastActivity.getTime(), now, DateUnit.SECOND) >= this.config.getWaitForNetworkIdlePageLoadTime()) {
                    break;
                }
                if (DateUtil.between(startTime, now, DateUnit.SECOND) > this.config.getMaximumWaitPageLoadTime()) {
                    log.debug("Network timeout after {}s with {} ", this.config.getMaximumWaitPageLoadTime(),pendingRequests.size());
                    break;
                }
            }
        } finally {
            page.onRequest(null);
            page.onResponse(null);
        }
    }

    private void waitForPageAndFramesLoad(Float timeoutOverwrite) {
        DateTime startTime = DateUtil.date();
        try {
            this.waitForStableNetwork();

            Page page = this.getCurrentPage();
            this.checkAndHandleNavigation(page);
        } catch (URLNotAllowedError e) {
            throw e;
        } catch (Exception e) {
            log.warn("⚠️  Page load failed, continuing...");
        }

        float elapsed = DateUtil.between(startTime, DateUtil.date(), DateUnit.SECOND);
        float remaining = 0;
        if (timeoutOverwrite != null) {
            remaining = timeoutOverwrite - elapsed;
        } else {
            remaining = this.config.getMinimumWaitPageLoadTime() - elapsed;
        }
        if (remaining < 0) {
            remaining = 0;
        }
        if (remaining > 0) {
            ThreadUtil.sleep(remaining, TimeUnit.SECONDS);
        }
    }

    private boolean isUrlAllowed(String url) {
        if (this.config.getAllowedDomains() == null) {
            return true;
        }

        try {
            ParseResult parsedUrl = Utils.urlparse(url);
            String domain = parsedUrl.netloc().toLowerCase();

            if ("about:blank".equals(url)) {
                return true;
            }

            if (StrUtil.contains(domain, ":")) {
                domain = domain.split(":")[0];
            }
            for (String allowedDomain : this.config.getAllowedDomains()) {
                if (allowedDomain.toLowerCase().equals(domain) || domain.endsWith("." + allowedDomain.toLowerCase())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("⛔️  Error checking URL allowlist: {}", e.getMessage(), e);
            return false;
        }
    }

    private void checkAndHandleNavigation(Page page) {
        if (!this.isUrlAllowed(page.url())) {
            log.warn("⛔️  Navigation to non-allowed URL detected: {}", page.url());
            try {
                this.goBack();
            } catch (Exception e) {
                log.error("⛔️  Failed to go back after detecting non-allowed URL: {}", e.getMessage(), e);
                throw new URLNotAllowedError("Navigation to non-allowed URL: " + page.url());
            }
        }
    }

    public void navigateTo(String url) {
        if (!this.isUrlAllowed(url)) {
            throw new BrowserError("Navigation to non-allowed URL: " + url);
        }
        Page page = this.getCurrentPage();
        page.navigate(url);
        page.waitForLoadState();
    }

    public void refreshPage() {
        Page page = this.getCurrentPage();
        page.reload();
        page.waitForLoadState();
    }

    public void goBack() {
        Page page = this.getCurrentPage();
        try {
            page.goBack(new Page.GoBackOptions().setTimeout(10).setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        } catch (Exception e) {
            log.debug("⏮️  Error during go_back: {}", e.getMessage());
        }
    }

    public void goForward() {
        Page page = this.getCurrentPage();
        try {
            page.goForward(new Page.GoForwardOptions().setTimeout(10).setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        } catch (Exception e) {
            log.debug("⏭️  Error during go_forward: {}", e.getMessage());
        }
    }

    public void closeCurrentTab() {
        BrowserSession session = this.getSession();
        Page page = this.getCurrentPage(session);
        page.close();
        this.activeTab = null;
        if (!CollUtil.isEmpty(session.getContext().pages())) {
            this.switchToTab(0);
            this.activeTab = session.getContext().pages().get(0);
        }
    }

    public String getPageHtml() {
        Page page = this.getCurrentPage();
        return page.content();
    }

    public Object executeJavascript(String script) {
        Page page = this.getCurrentPage();
        return page.evaluate(script);
    }

    public Object getPageStructure() {
        String debugScript = """
			(() => {
			function getPageStructure(element = document, depth = 0, maxDepth = 10) {
				if (depth >= maxDepth) return '';

				const indent = '  '.repeat(depth);
				let structure = '';

				// Skip certain elements that clutter the output
				const skipTags = new Set(['script', 'style', 'link', 'meta', 'noscript']);

				// Add current element info if it's not the document
				if (element !== document) {
					const tagName = element.tagName.toLowerCase();

					// Skip uninteresting elements
					if (skipTags.has(tagName)) return '';

					const id = element.id ? `#${element.id}` : '';
					const classes = element.className && typeof element.className === 'string' ?
						`.${element.className.split(' ').filter(c => c).join('.')}` : '';

					// Get additional useful attributes
					const attrs = [];
					if (element.getAttribute('role')) attrs.push(`role="${element.getAttribute('role')}"`);
					if (element.getAttribute('aria-label')) attrs.push(`aria-label="${element.getAttribute('aria-label')}"`);
					if (element.getAttribute('type')) attrs.push(`type="${element.getAttribute('type')}"`);
					if (element.getAttribute('name')) attrs.push(`name="${element.getAttribute('name')}"`);
					if (element.getAttribute('src')) {
						const src = element.getAttribute('src');
						attrs.push(`src="${src.substring(0, 50)}${src.length > 50 ? '...' : ''}"`);
					}

					// Add element info
					structure += `${indent}${tagName}${id}${classes}${attrs.length ? ' [' + attrs.join(', ') + ']' : ''}\\n`;

					// Handle iframes specially
					if (tagName === 'iframe') {
						try {
							const iframeDoc = element.contentDocument || element.contentWindow?.document;
							if (iframeDoc) {
								structure += `${indent}  [IFRAME CONTENT]:\\n`;
								structure += getPageStructure(iframeDoc, depth + 2, maxDepth);
							} else {
								structure += `${indent}  [IFRAME: No access - likely cross-origin]\\n`;
							}
						} catch (e) {
							structure += `${indent}  [IFRAME: Access denied - ${e.message}]\\n`;
						}
					}
				}

				// Get all child elements
				const children = element.children || element.childNodes;
				for (const child of children) {
					if (child.nodeType === 1) { // Element nodes only
						structure += getPageStructure(child, depth + 1, maxDepth);
					}
				}

				return structure;
			}

			return getPageStructure();
		})()
		""";
        Page page = this.getCurrentPage();
        return page.evaluate(debugScript);
    }

    public BrowserState getState(boolean cacheClickableElementsHashes) {
        this.waitForPageAndFramesLoad(null);
        BrowserSession session = this.getSession();
        BrowserState updatedState = this.getUpdatedState(-1);

        if (cacheClickableElementsHashes) {
            if (session.getCachedStateClickableElementsHashes() != null &&
                    session.getCachedStateClickableElementsHashes().getUrl().equals(updatedState.getUrl())) {
                List<DOMElementNode> updatedStateClickableElements = ClickableElementProcessor.getClickableElements(updatedState.getElementTree());

                for (DOMElementNode domElementNode : updatedStateClickableElements) {
                    domElementNode.setIsNew(!session.getCachedStateClickableElementsHashes().getHashes().contains(ClickableElementProcessor.hashDomElement(domElementNode)));
                }
            }
            var tmp = new CachedStateClickableElementsHashes();
            tmp.setUrl(updatedState.getUrl());
            tmp.setHashes(ClickableElementProcessor.getClickableElementsHashes(updatedState.getElementTree()));
            session.setCachedStateClickableElementsHashes(tmp);
        }
        session.setCachedState(updatedState);
        if (StrUtil.isNotBlank(this.config.getCookiesFile())) {
            this.saveCookies();
        }
        return session.getCachedState();
    }

    private BrowserState getUpdatedState(int focusElement) {
        BrowserSession session = this.getSession();

        Page page = null;
        try {
            page = this.getCurrentPage();
            page.evaluate("1");
        } catch (Exception e) {
            log.debug("👋  Current page is no longer accessible: {}", e.getMessage());
            List<Page> pages = session.getContext().pages();
            if (!CollUtil.isEmpty(pages)) {
                this.state.setTargetId(null);
                page = this.getCurrentPage(session);
                log.debug("🔄  Switched to page: {}", page.title());
            } else {
                throw new BrowserError("Browser closed: no valid pages available");
            }
        }

        try {
            this.removeHighlights();
            DomService domService = new DomService(page);
            DOMState content = domService.getClickableElements(this.config.isHighlightElements(), this.config.getViewportExpansion(), focusElement);
            List<TabInfo> tabsInfo = this.getTabsInfo();
            String screenshotB64 = this.takeScreenshot(false);
            Tuple<Integer, Integer> tuple = this.getScrollInfo(page);
            int pixelAbove = tuple._1();
            int pixelBelow = tuple._2();

            this.currentState = new BrowserState(
                    content.getElementTree(),
                    content.getSelectorMap(),
                    page.url(),
                    page.title(),
                    tabsInfo,
                    screenshotB64,
                    pixelAbove,
                    pixelBelow
            );

            return this.currentState;
        } catch (Exception e) {
            log.error("❌  Failed to update state: {}", e.getMessage());
            if (this.currentState != null) {
                return this.currentState;
            }
            throw e;
        }
    }

    public String takeScreenshot(boolean fullPage) {
        Page page = this.getCurrentPage();
        page.bringToFront();
        page.waitForLoadState();

        byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(fullPage).setAnimations(ScreenshotAnimations.DISABLED));
        String screenshotB64 = Base64.getEncoder().encodeToString(screenshot);
        return screenshotB64;
    }

    public void removeHighlights() {
        try {
            Page page = this.getCurrentPage();
            page.evaluate("""
                try {
                    // Remove the highlight container and all its contents
                    const container = document.getElementById('playwright-highlight-container');
                    if (container) {
                        container.remove();
                    }

                    // Remove highlight attributes from elements
                    const highlightedElements = document.querySelectorAll('[browser-user-highlight-id^="playwright-highlight-"]');
                    highlightedElements.forEach(el => {
                        el.removeAttribute('browser-user-highlight-id');
                    });
                } catch (e) {
                    console.error('Failed to remove highlights:', e);
                }
                """);
        } catch (Exception e) {
            log.debug("⚠  Failed to remove highlights (this is usually ok): {}", e.getMessage());
        }
    }

    private static String convertSimpleXpathToCssSelector(String xpath) {
        if (StrUtil.isBlank(xpath)) {
            return "";
        }

        xpath = xpath.replaceFirst("^/+", "");
        String[] parts = xpath.split("/");
        var cssParts = new ArrayList<String>();

        for (String part : parts) {
            if (StrUtil.isBlank(part)) {
                continue;
            }

            if (part.contains(":") && !part.contains("[")) {
                String basePart = StrUtil.replace(part, ":", "\\:");
                cssParts.add(basePart);
                continue;
            }

            if (part.contains("[")) {
                String basePart = part.substring(0, part.indexOf("["));
                if (basePart.contains(":")) {
                    basePart = StrUtil.replace(part, ":", "\\:");
                }
                String indexPart = part.substring(part.indexOf("["));

                //将"[0][1][2]"变为['0', '1', '2']
                var indices = new ArrayList<String>();
                for (String s : indexPart.split("]")) {
                    if (!StrUtil.isBlank(s)) {
                        if (s.startsWith("[")) {
                            s = s.substring(1);
                        }
                        indices.add(s);
                    }
                }
                for (String idx : indices) {
                    try {
                        if (StrUtil.isNumeric(idx)) {
                            int index = Convert.toInt(idx) - 1;
                            basePart += ":nth-of-type(" + index + 1 + ")";
                        } else if ("last()".equals(idx)) {
                            basePart += ":last-of-type";
                        } else if (idx.contains("position()")) {
                            if (idx.contains(">1")) {
                                basePart += ":nth-of-type(n+2)";
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
                cssParts.add(basePart);
            } else {
                cssParts.add(part);
            }
        }
        String baseSelector = StrUtil.join(" > ", cssParts);
        return baseSelector;
    }

    public static String enhancedCssSelectorForElement(DOMElementNode element, boolean includeDynamicAttributes) {
        try {
            String cssSelector = convertSimpleXpathToCssSelector(element.getXpath());

            Map<String, String> map = element.getAttributes();
            if (map.containsKey("class") && StrUtil.isNotBlank(map.get("class")) && includeDynamicAttributes) {

                var classes = new ArrayList<String>();
                for (String s : map.get("class").split(" ")) {
                    if (StrUtil.isNotBlank(s)) {
                        classes.add(s.trim());
                    }
                }
                for (String className : classes) {
                    if (className.matches("^[a-zA-Z_][a-zA-Z0-9_-]*$")) {
                        cssSelector += "." + className;
                    } else {
                        continue;
                    }
                }
            }
            var SAFE_ATTRIBUTES = new HashSet<String>(Arrays.asList(
                    // Data attributes (if they're stable in your application)
                    "id",
				    // Standard HTML attributes
                    "name",
                    "type",
                    "placeholder",
				    // Accessibility attributes
                    "aria-label",
                    "aria-labelledby",
                    "aria-describedby",
                    "role",
				    // Common form attributes
                    "for",
                    "autocomplete",
                    "required",
                    "readonly",
				    // Media attributes
                    "alt",
                    "title",
                    "src",
				    // Custom stable attributes (add any application-specific ones)
                    "href",
                    "target"
            ));
            if (includeDynamicAttributes) {
                SAFE_ATTRIBUTES.addAll(Arrays.asList(
                    "data-id",
                    "data-qa",
                    "data-cy",
                    "data-testid"
                ));
            }
            for (String attribute : element.getAttributes().keySet()) {
                String value = element.getAttributes().get(attribute);
                if ("class".equals(attribute)) {
                    continue;
                }
                if (StrUtil.isBlank(attribute)) {
                    continue;
                }
                if (!SAFE_ATTRIBUTES.contains(attribute)) {
                    continue;
                }
                String safeAttribute = StrUtil.replace(attribute, ":", "\\:");

                if (StrUtil.isBlank(value)) {
                    cssSelector += "[" + safeAttribute + "]";
                } else if (StrUtil.containsAnyIgnoreCase(value, "\"", "\\", "'", "<", ">", "`", "\n", "\r", "\t")) {
                    if (value.contains("\n")) {
                        value = value.split("\n")[0];
                    }
                    String collapsedValue = value.replaceAll("\\s+", " ").strip();
                    String safeValue = StrUtil.replace(collapsedValue, "\"", "\\\"");
                    cssSelector += "[" + safeAttribute + "*=\"" + safeValue + "\"]";
                } else {
                    cssSelector += "[" + safeAttribute + "=\"" + value + "\"]";
                }
            }
            return cssSelector;
        } catch (Exception e) {
            String tagName = !StrUtil.isBlank(element.getTagName()) ? element.getTagName() : "*";
            return tagName + "[highlight_index='" + element.getHighlightIndex() + "']";
        }
    }

    public ElementHandle getLocateElement(DOMElementNode element) {
        Page currentPage = this.getCurrentPage();

        var parents = new ArrayList<DOMElementNode>();
        DOMElementNode current = element;
        while (current.getParent() != null) {
            DOMElementNode parent = current.getParent();
            parents.add(parent);
            current = parent;
        }

        CollUtil.reverse(parents);

        var iframes = new ArrayList<DOMElementNode>();
        for (DOMElementNode item : parents) {
            if ("iframe".equals(item.getTagName())) {
                iframes.add(item);
            }
        }

        FrameLocator currentFrame = null;
        for (DOMElementNode parent : iframes) {
            String cssSelector = this.enhancedCssSelectorForElement(parent, this.config.isIncludeDynamicAttributes());
            if (currentFrame == null) {
                currentFrame = currentPage.frameLocator(cssSelector);
            } else {
                currentFrame = currentFrame.frameLocator(cssSelector);
            }
        }

        String cssSelector = this.enhancedCssSelectorForElement(element, this.config.isIncludeDynamicAttributes());

        try {
            if (currentFrame != null) {
                return currentFrame.locator(cssSelector).elementHandle();
            } else {
                ElementHandle elementHandle = currentPage.querySelector(cssSelector);
                if (elementHandle != null) {
                    boolean isHidden = elementHandle.isHidden();
                    if (!isHidden) {
                        elementHandle.scrollIntoViewIfNeeded();
                    }
                    return elementHandle;
                }
                return null;
            }
        } catch (Exception e) {
            log.error("❌  Failed to locate element: {}", e.getMessage());
            return null;
        }
    }

    public ElementHandle getLocateElementByXpath(String xpath) {
        Page currentFrame = this.getCurrentPage();

        try {
            ElementHandle elementHandle = currentFrame.querySelector("xpath=" + xpath);
            if (elementHandle != null) {
                boolean isHidden = elementHandle.isHidden();
                if (!isHidden) {
                    elementHandle.scrollIntoViewIfNeeded();
                }
                return elementHandle;
            }
            return null;
        } catch (Exception e) {
            log.error("❌  Failed to locate element by XPath {}: {}", xpath, e.getMessage());
            return null;
        }
    }

    public ElementHandle getLocateElementByCssSelector(String cssSelector) {
        Page currentFrame = this.getCurrentPage();

        try {
            ElementHandle elementHandle = currentFrame.querySelector(cssSelector);
            if (elementHandle != null) {
                boolean isHidden = elementHandle.isHidden();
                if (!isHidden) {
                    elementHandle.scrollIntoViewIfNeeded();
                }
                return elementHandle;
            }
            return null;
        } catch (Exception e) {
            log.error("❌  Failed to locate element by CSS selector {}: {}", cssSelector, e.getMessage());
            return null;
        }
    }

    public ElementHandle getLocateElementByText(String text, Integer nth, String elementType) {
        Page currentFrame = this.getCurrentPage();
        try {
            String selector = (StrUtil.isBlank(elementType) ? "*" : elementType) + ":text(\"" + text + "\")";
            List<ElementHandle> list = currentFrame.querySelectorAll(selector);
            var elements = new ArrayList<ElementHandle>();
            for (ElementHandle el : list) {
                if (el.isVisible()) {
                    elements.add(el);
                }
            }

            if (CollUtil.isEmpty(elements)) {
                log.error("No visible element with text '{}' found.", text);
                return null;
            }

            ElementHandle elementHandle;
            if (nth != null) {
                if (0 <= nth && nth < elements.size()) {
                    elementHandle = elements.get(nth);
                } else {
                    log.error("Visible element with text '{}' not found at index {}.", text, nth);
                    return null;
                }
            } else {
                elementHandle = elements.get(0);
            }

            boolean isHidden = elementHandle.isHidden();
            if (!isHidden) {
                elementHandle.scrollIntoViewIfNeeded();
            }
            return elementHandle;
        } catch (Exception e) {
            log.error("❌  Failed to locate element by text '{}': {}", text, e.getMessage());
            return null;
        }
    }

    public void inputTextElementNode(DOMElementNode elementNode, String text) {
        try {
            ElementHandle elementHandle = this.getLocateElement(elementNode);

            if (elementHandle == null) {
                throw new BrowserError("Element: " + elementNode.getTagName() + " not found");
            }

            try {
                elementHandle.waitForElementState(ElementState.STABLE, new ElementHandle.WaitForElementStateOptions().setTimeout(1000));
                boolean isHidden = elementHandle.isHidden();
                if (!isHidden) {
                    elementHandle.scrollIntoViewIfNeeded(new ElementHandle.ScrollIntoViewIfNeededOptions().setTimeout(1000));
                }
            } catch (Exception ignored) {
            }

            JSHandle tagHandle = elementHandle.getProperty("tagName");
            String tagName = String.valueOf(tagHandle.jsonValue()).toLowerCase();
            JSHandle isContenteditable = elementHandle.getProperty("isContentEditable");
            JSHandle readonlyHandle = elementHandle.getProperty("readOnly");
            JSHandle disabledHandle = elementHandle.getProperty("disabled");

            boolean readonly = readonlyHandle != null ? Convert.toBool(readonlyHandle.jsonValue()) : false;
            boolean disabled = disabledHandle != null ? Convert.toBool(disabledHandle.jsonValue()) : false;

            if ((isContenteditable.jsonValue() != null || "input".equals(tagName)) && !(readonly || disabled)) {
                elementHandle.evaluate("el => {el.textContent = \"\"; el.value = \"\";}");
                elementHandle.type(text, new ElementHandle.TypeOptions().setDelay(5));
            } else {
                elementHandle.fill(text);
            }
        } catch (Exception e) {
            log.error("❌  Failed to input text into element: {}. Error: {}", elementNode.getTagName(), e.getMessage());
            throw new BrowserError("Failed to input text into index " + elementNode.getHighlightIndex());
        }
    }

    public String clickElementNode(DOMElementNode elementNode) throws Exception {
        Page page = this.getCurrentPage();
        try {
            ElementHandle elementHandle = this.getLocateElement(elementNode);

            if (elementHandle == null) {
                throw new Exception("Element: " + elementNode.getTagName() + " not found");
            }

            BiFunction<Consumer<ElementHandle.ClickOptions>, ElementHandle.ClickOptions, String> performClick = (clickFunc, clickOptions) -> {
                if (StrUtil.isNotBlank(this.config.getSaveDownloadsPath())) {
                    try {
                        Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(5000), () -> {
                            if (clickFunc != null) {
                                clickFunc.accept(clickOptions);
                            } else {
                                page.evaluate("(el) => el.click()", elementHandle);
                            }
                        });
                        String suggestedFilename = download.suggestedFilename();
                        Path downloadPath = Paths.get(this.config.getSaveDownloadsPath(), suggestedFilename);
                        download.saveAs(downloadPath);
                        log.debug("⬇️  Download triggered. Saved file to: {}", downloadPath.toString());
                        return downloadPath.toString();
                    } catch (TimeoutError e) {
                        log.debug("No download triggered within timeout. Checking navigation...");
                        page.waitForLoadState();
                        this.checkAndHandleNavigation(page);
                    }
                } else {
                    if (clickFunc != null) {
                        clickFunc.accept(clickOptions);
                    } else {
                        page.evaluate("(el) => el.click()", elementHandle);
                    }
                    page.waitForLoadState();
                    this.checkAndHandleNavigation(page);
                }
                return null;
            };

            try {
                return performClick.apply(elementHandle::click, new ElementHandle.ClickOptions().setTimeout(1500));
            } catch (URLNotAllowedError e) {
                throw e;
            } catch (Exception e) {
                try {
                    return performClick.apply(null, null);
                } catch (URLNotAllowedError e1) {
                    throw e1;
                } catch (Exception ex) {
                    throw new Exception("Failed to click element: " + e.getMessage());
                }
            }
        } catch (URLNotAllowedError e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Failed to click element: " + elementNode.getTagName() + ". Error: " + e.getMessage());
        }
    }

    public List<TabInfo> getTabsInfo() {
        BrowserSession session = this.getSession();

        var tabsInfo = new ArrayList<TabInfo>();
        for (int i = 0; i < session.getContext().pages().size(); i++) {
            Page page = session.getContext().pages().get(i);
            TabInfo tabInfo = null;
            try {
                tabInfo = new TabInfo(i, page.url(), page.title());
            } catch (TimeoutError e) {
                log.debug("⚠  Failed to get tab info for tab #{}: {} (ignoring)", i, page.url());
                tabInfo = new TabInfo(i, "about:blank", "ignore this tab and do not use it");
            }
            tabsInfo.add(tabInfo);
        }
        return tabsInfo;
    }

    public void switchToTab(int pageId) {
        BrowserSession session = this.getSession();
        List<Page> pages = session.getContext().pages();
        if (pageId == -1) {
            pageId = pages.size() - 1;
        }

        if (pageId >= pages.size()) {
            throw new BrowserError("No tab found with page_id: " + pageId);
        }

        Page page = pages.get(pageId);

        if (StrUtil.isNotBlank(this.browser.getConfig().getCdpUrl())) {
            List<Hashtable<String, String>> targets = this.getCdpTargets();
            for (Hashtable<String, String> target : targets) {
                if (page.url().equals(target.get("url"))) {
                    this.state.setTargetId(target.get("targetId"));
                    break;
                }
            }
        }

        this.activeTab = page;
        page.bringToFront();
        page.waitForLoadState();
    }

    public void createNewTab(String url) {
        if (StrUtil.isNotBlank(url) && !this.isUrlAllowed(url)) {
            throw new BrowserError("Cannot create new tab with non-allowed URL: " + url);
        }

        BrowserSession session = this.getSession();
        Page newPage = session.getContext().newPage();

        this.activeTab = newPage;

        newPage.waitForLoadState();

        if (StrUtil.isNotBlank(url)) {
            newPage.navigate(url);
            this.waitForPageAndFramesLoad(1.0f);
        }

        if (StrUtil.isNotBlank(this.browser.getConfig().getCdpUrl())) {
            List<Hashtable<String, String>> targets = this.getCdpTargets();
            for (Hashtable<String, String> target : targets) {
                if (newPage.url().equals(target.get("url"))) {
                    this.state.setTargetId(target.get("targetId"));
                    break;
                }
            }
        }
    }

    private Page getCurrentPage(BrowserSession session) {
        List<Page> pages = session.getContext().pages();

        if (StrUtil.isNotBlank(this.browser.getConfig().getCdpUrl()) && StrUtil.isNotBlank(this.state.getTargetId())) {
            List<Hashtable<String, String>> targets = this.getCdpTargets();
            for (Hashtable<String, String> target : targets) {
                if (this.state.getTargetId().equals(target.get("targetId"))) {
                    for (Page page : pages) {
                        if (page.url().equals(target.get("url"))) {
                            return page;
                        }
                    }
                }
            }
        }

        if (this.activeTab != null && session.getContext().pages().contains(this.activeTab) && !this.activeTab.isClosed()) {
            return this.activeTab;
        }

        var nonExtensionPages = new ArrayList<Page>();
        for (Page page : pages) {
            if (!page.url().startsWith("chrome-extension://") && !page.url().startsWith("chrome://")) {
                nonExtensionPages.add(page);
            }
        }
        if (!CollUtil.isEmpty(nonExtensionPages)) {
            return nonExtensionPages.get(nonExtensionPages.size() - 1);
        }

        try {
            return session.getContext().newPage();
        } catch (Exception e) {
            log.warn("⚠️  No browser window available, opening a new window");
            this.initializeSession();
            Page page = session.getContext().newPage();
            this.activeTab = page;
            return page;
        }
    }

    public Map<Integer, DOMElementNode> getSelectorMap() {
        BrowserSession session = this.getSession();
        if (session.getCachedState() == null) {
            return new HashMap<>();
        }
        return session.getCachedState().getSelectorMap();
    }

    public ElementHandle getElementByIndex(int index) {
        Map<Integer, DOMElementNode> selectorMap = this.getSelectorMap();
        ElementHandle elementHandle = this.getLocateElement(selectorMap.get(index));
        return elementHandle;
    }

    public DOMElementNode getDomElementByIndex(int index) {
        Map<Integer, DOMElementNode> selectorMap = this.getSelectorMap();
        return selectorMap.get(index);
    }

    public void saveCookies() {
        if (this.session != null && this.session.getContext() != null && StrUtil.isNotBlank(this.config.getCookiesFile())) {
            try {
                List<Cookie> cookies = this.session.getContext().cookies();
                log.debug("🍪  Saving " + cookies.size() + " cookies to " + this.config.getCookiesFile());

                FileUtil.writeString(JSONUtil.toJsonStr(cookies), FileUtil.file(this.config.getCookiesFile()), StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("❌  Failed to save cookies: {}", e.getMessage());
            }
        }
    }

    public boolean isFileUploader(DOMElementNode elementNode, int maxDepth, int currentDepth) {
        if (currentDepth > maxDepth) {
            return false;
        }

        boolean isUploader = false;

        if (elementNode == null) {
            return false;
        }

        if ("input".equals(elementNode.getTagName())) {
            isUploader = "file".equals(elementNode.getAttributes().get("type")) || elementNode.getAttributes().get("accept") != null;
        }

        if (isUploader) {
            return true;
        }

        if (!CollUtil.isEmpty(elementNode.getChildren()) && currentDepth < maxDepth) {
            for (DOMBaseNode child : elementNode.getChildren()) {
                if (child instanceof DOMElementNode) {
                    if (this.isFileUploader((DOMElementNode)child, maxDepth, currentDepth + 1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Tuple<Integer, Integer> getScrollInfo(Page page) {
        int scrollY = Convert.toInt(page.evaluate("window.scrollY"));
        int viewportHeight = Convert.toInt(page.evaluate("window.innerHeight"));
        int totalHeight = Convert.toInt(page.evaluate("document.documentElement.scrollHeight"));
        return new Tuple<>(scrollY, totalHeight - (scrollY + viewportHeight));
    }

    public void resetContext() {
        BrowserSession session = this.getSession();

        List<Page> pages = session.getContext().pages();
        for (Page page : pages) {
            page.close();
        }

        this.activeTab = null;
        session.setCachedState(null);
        this.state.setTargetId(null);
    }

    private String getUniqueFilename(String directory, String filename) {
        String ext = filename.substring(filename.lastIndexOf("."));
        String base = filename.substring(0, filename.lastIndexOf("."));
        int counter = 1;
        String newFilename = filename;
        while (FileUtil.exist(directory + File.separator + newFilename)) {
            newFilename = base + "(" +  counter++ + ")" + ext;
        }
        return newFilename;
    }

    public void waitForElement(String selector, float timeout) {
        Page page = this.getCurrentPage();
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
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

    // Additional browser operations would be implemented here...

    @Override
    public void close() {
        try {
            if (session != null) {
                // Clean up resources
                if (pageEventHandler != null) {
                    session.getContext().offPage(pageEventHandler);
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
    }

    private List<Hashtable<String, String>> getCdpTargets() {
        if (StrUtil.isBlank(this.browser.getConfig().getCdpUrl()) || this.session == null) {
            return new ArrayList<>();
        }
        List<Page> pages = this.session.getContext().pages();
        if (!CollUtil.isEmpty(pages)) {
            return new ArrayList<>();
        }
        try (com.microsoft.playwright.BrowserContext c = pages.get(0).context()) {
            CDPSession cdpSession = c.newCDPSession(pages.get(0));
            JsonObject result = cdpSession.send("Target.getTargets");
            cdpSession.detach();
            List<Hashtable> result1 = JSONUtil.toList(JSONUtil.toJsonStr(result.get("targetInfos")), Hashtable.class);
            return convert(result1, String.class, String.class);
        }
    }

    public static <K extends String, V extends String>
    List<Hashtable<K, V>> convert(Object obj, Class<K> keyType, Class<V> valueType) {

        List<Hashtable<K, V>> result = new ArrayList<>();
        if (obj instanceof List<?>) {
            for (Object item : (List<?>) obj) {
                if (item instanceof Hashtable<?, ?>) {
                    Hashtable<K, V> map = new Hashtable<>();
                    for (Map.Entry<?, ?> entry : ((Hashtable<?, ?>) item).entrySet()) {
                        K key = keyType.cast(entry.getKey());
                        V value = valueType.cast(entry.getValue());
                        map.put(key, value);
                    }
                    result.add(map);
                }
            }
            return result;
        }
        throw new IllegalArgumentException("类型不兼容");
    }
}
