package dev.langchain4j.example.tests;

import cn.hutool.core.thread.ThreadUtil;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestFullScreen {
    @Test
    public void testFullScreen() {
        log.info("Attempting to connect to Chrome...");
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType
                     .LaunchOptions()
                     .setHeadless(false)
                     .setArgs(List.of("--start-maximized")))) {
            Page page = browser.newPage();
            page.navigate("https://www.baidu.com/");

            ThreadUtil.sleep(30, TimeUnit.SECONDS);
        }
    }
}
