package dev.langchain4j.example.tests;

import cn.hutool.core.thread.ThreadUtil;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TestAttachChrome {
    @Test
    public void mainTest() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("https://playwright.dev");
            System.out.println(page.title());
        }
    }

    @Test
    public void testFullScreen() {
        log.info("Attempting to connect to Chrome...");
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch()) {
            Page page = browser.newPage();
            page.navigate("https://www.baidu.com/");

            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            log.info("Current page title: {}", page.title());

            try {
                //从body下的第一层child开始找。
                page.waitForSelector("div[id=\"wrapper\"]", new Page.WaitForSelectorOptions().setTimeout(10000));
                log.info("Gmail interface detected");
            } catch (Exception e) {
                log.info("Note: Gmail interface not detected: {}", e.getMessage());
            }

            ThreadUtil.sleep(30, TimeUnit.SECONDS);
        }
    }
}
