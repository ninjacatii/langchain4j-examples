package dev.langchain4j.example.entity.browser._context;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BrowserContextConfig {
    private String cookiesFile;
    private double minimumWaitPageLoadTime = 0.25;
    private double waitForNetworkIdlePageLoadTime = 0.5;
    private double maximumWaitPageLoadTime = 5;
    private double waitBetweenActions = 0.5;
    private boolean disableSecurity = false;
    private BrowserContextWindowSize browserWindowSize = new BrowserContextWindowSize(1280, 1100);
    private Boolean noViewport;
    private String saveRecordingPath;
    private String saveDownloadsPath;
    private String saveHarPath;
    private String tracePath;
    private String locale;
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36";
    private boolean highlightElements = true;
    private int viewportExpansion = 0;
    private List<String> allowedDomains;
    private boolean includeDynamicAttributes = true;
    private Map<String, String> httpCredentials;
    private Boolean isMobile;
    private Boolean hasTouch;
    private Map<String, Object> geolocation;
    private List<String> permissions;
    private String timezoneId;
    private boolean keepAlive = false;
}
