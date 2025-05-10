package dev.langchain4j.example.entity.browser._context;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.options.Geolocation;
import com.microsoft.playwright.options.HttpCredentials;
import com.microsoft.playwright.options.RecordVideoSize;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BrowserContextConfig {
    private String cookiesFile;
    @Builder.Default
    private float minimumWaitPageLoadTime = 0.25f;
    @Builder.Default
    private float waitForNetworkIdlePageLoadTime = 0.5f;
    @Builder.Default
    private float maximumWaitPageLoadTime = 5;
    @Builder.Default
    private float waitBetweenActions = 0.5f;
    @Builder.Default
    private boolean disableSecurity = false;
    @Builder.Default
    private RecordVideoSize browserWindowSize = new BrowserContextWindowSize(1280, 1100);
    private boolean noViewport;
    private Path saveRecordingPath;
    private String saveDownloadsPath;
    private Path saveHarPath;
    private String tracePath;
    private String locale;
    @Builder.Default
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36";
    @Builder.Default
    private boolean highlightElements = true;
    @Builder.Default
    private int viewportExpansion = 0;
    private List<String> allowedDomains;
    @Builder.Default
    private boolean includeDynamicAttributes = true;
    private HttpCredentials httpCredentials;
    private boolean isMobile;
    private boolean hasTouch;
    private Geolocation geolocation;
    private List<String> permissions;
    private String timezoneId;
    @Builder.Default
    private boolean keepAlive = false;
}
