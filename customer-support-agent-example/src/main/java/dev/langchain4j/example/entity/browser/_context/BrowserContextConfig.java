package dev.langchain4j.example.entity.browser._context;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.options.Geolocation;
import com.microsoft.playwright.options.HttpCredentials;
import com.microsoft.playwright.options.RecordVideoSize;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Data
public class BrowserContextConfig {
    private String cookiesFile;
    private float minimumWaitPageLoadTime = 0.25f;
    private float waitForNetworkIdlePageLoadTime = 0.5f;
    private float maximumWaitPageLoadTime = 5;
    private float waitBetweenActions = 0.5f;
    private boolean disableSecurity = false;
    private RecordVideoSize browserWindowSize = new RecordVideoSize(1280, 1100);
    private Boolean noViewport;
    private Path saveRecordingPath;
    private String saveDownloadsPath;
    private Path saveHarPath;
    private String tracePath;
    private String locale;
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36";
    private boolean highlightElements = true;
    private int viewportExpansion = 0;
    private List<String> allowedDomains;
    private boolean includeDynamicAttributes = true;
    private HttpCredentials httpCredentials;
    private Boolean isMobile;
    private Boolean hasTouch;
    private Geolocation geolocation;
    private List<String> permissions;
    private String timezoneId;
    private boolean keepAlive = false;
}
