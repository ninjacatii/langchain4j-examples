package dev.langchain4j.example.entity.browser._context;

import com.microsoft.playwright.Page;
import dev.langchain4j.example.entity.browser._views.BrowserState;
import lombok.Data;

@Data
public class BrowserSession {
    private Page activeTab;
    private com.microsoft.playwright.BrowserContext context;
    private BrowserState cachedState;
    private CachedStateClickableElementsHashes cachedStateClickableElementsHashes;

    public BrowserSession(com.microsoft.playwright.BrowserContext context, BrowserState cachedState) {
        this.context = context;
        this.cachedState = cachedState;

        // Initialize script injection
        String initScript = """
            (() => {
                if (!window.getEventListeners) {
                    window.getEventListeners = function (node) {
                        return node.__listeners || {};
                    };

                    const originalAddEventListener = Element.prototype.addEventListener;
                    const eventProxy = {
                        addEventListener: function (type, listener, options = {}) {
                            if (!this.__listeners) this.__listeners = {};
                            if (!this.__listeners[type]) this.__listeners[type] = [];
                            
                            this.__listeners[type].push({
                                listener: listener,
                                type: type,
                                ...options
                            });
                            
                            return originalAddEventListener.call(this, type, listener, options);
                        }
                    };
                    Element.prototype.addEventListener = eventProxy.addEventListener;
                }
            })()""";

        context.onPage(page -> page.addInitScript(initScript));
    }
}
