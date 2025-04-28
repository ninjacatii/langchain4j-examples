package dev.langchain4j.example.util;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Media;
import dev.langchain4j.example.entity.agent._views.ActionResult;
import dev.langchain4j.example.entity.browser._context.BrowserContext;
import dev.langchain4j.example.entity.browser._context.BrowserSession;
import dev.langchain4j.example.entity.dom._views.DOMElementNode;
import dev.langchain4j.example.iface.MethodToAction;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Actions {
    @MethodToAction(
            description = "Complete task - with return text and if the task is finished (success=True) or not yet  completely finished (success=False), because last step is reached",
            paraType = { String.class, Boolean.class },
            paraName = { "text", "success" }
    )
    public static ActionResult done(String text, Boolean success, BrowserContext browser) {
        return ActionResult.builder().isDone(true).success(success).extractedContent(text).build();
    }

    @MethodToAction(
        description = "Search the query in Google in the current tab, the query should be a search query like humans search in Google, concrete and not vague or super long. More the single most important items. ",
        paraType = { String.class },
        paraName = { "query" }
    )
    public static ActionResult searchGoogle(String query, BrowserContext browser) {
        Page page = browser.getCurrentPage();
        page.navigate("https://cn.bing.com/search?q=" + query + "&ensearch=1'");
        String msg = "🔍  Searched for \"" + query + "\" in Bing";
        log.info(msg);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Navigate to URL in the current tab",
            paraType = { String.class },
            paraName = { "url" }
    )
    public static ActionResult goToUrl(String url, BrowserContext browser) {
        Page page = browser.getCurrentPage();
        page.navigate(url);
        page.waitForLoadState();
        String msg = "🔗  Navigated to " + url;
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Go back",
            paraType = {  },
            paraName = {  }
    )
    public static ActionResult goBack(BrowserContext browser) {
        browser.goBack();
        String msg = "🔙  Navigated back";
        log.info(msg);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Wait for x seconds default 3",
            paraType = { Integer.class },
            paraName = { "seconds" }
    )
    public static ActionResult wait(Integer seconds, BrowserContext browser) {
        String msg = "🕒  Waiting for {seconds} seconds";
        log.info(msg);
        ThreadUtil.sleep(seconds, TimeUnit.SECONDS);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Click element by index",
            paraType = { Integer.class, String.class },
            paraName = { "index", "xpath" }
    )
    public static ActionResult clickElementByIndex(Integer index, String xpath, BrowserContext browser) throws Exception {
        BrowserSession session = browser.getSession();

        if (!browser.getSelectorMap().containsKey(index)) {
            throw new Exception("Element with index " + index + " does not exist - retry or use alternative actions");
        }

        DOMElementNode elementNode = browser.getDomElementByIndex(index);
        int initialPages = session.getContext().pages().size();

        //if element has file uploader then don't click
        if (browser.isFileUploader(elementNode, 3, 0)) {
            String msg = "Index " + index + " - has an element which opens file upload dialog. To upload files please use a specific function to upload files ";
            log.info(msg);
            return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
        }

        String msg = "";

        try {
            String downloadPath = browser.clickElementNode(elementNode);
            if (StrUtil.isNotBlank(downloadPath)) {
                msg = "💾  Downloaded file to " + downloadPath;
            } else {
                msg = "🖱️  Clicked button with index " + index + ": " + elementNode.getAllTextTillNextClickableElement(2);
            }

            log.info(msg);
            log.debug("Element xpath: {}", xpath);
            if (session.getContext().pages().size() > initialPages) {
                String newTabMsg = "New tab opened - switching to it";
                msg += " - " + newTabMsg;
                log.info(newTabMsg);
                browser.switchToTab(-1);
            }
            return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
        } catch (Exception e) {
            log.warn("Element not clickable with index {} - most likely the page changed", index);
            return ActionResult.builder().error(e.getMessage()).build();
        }
    }

    @MethodToAction(
            description = "Input text into a input interactive element",
            paraType = { Integer.class, String.class, String.class },
            paraName = { "index", "text", "xpath" }
    )
    public static ActionResult inputText(Integer index, String text, String xpath, BrowserContext browser) throws Exception {
        if (!browser.getSelectorMap().containsKey(index)) {
            throw new Exception("Element with index " + index + " does not exist - retry or use alternative actions");
        }

        DOMElementNode elementNode = browser.getDomElementByIndex(index);
        browser.inputTextElementNode(elementNode, text);
        String msg = "⌨️  Input {params.text} into index " + index;
        log.info(msg);
        log.debug("Element xpath: {}", xpath);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Save the current page as a PDF file",
            paraType = {  },
            paraName = {  }
    )
    public static ActionResult savePdf(BrowserContext browser) {
        Page page = browser.getCurrentPage();
        // 原始 Python 代码:
        // short_url = re.sub(r'^https?://(?:www\.)?|/$', '', page.url)
        // slug = re.sub(r'[^a-zA-Z0-9]+', '-', short_url).strip('-').lower()

        // Java 版本
        String shortUrl = page.url()
                .replaceAll("^https?://(?:www\\.)?|/$", "");  // 移除协议头和尾部斜杠
        String slug = shortUrl
                .replaceAll("[^a-zA-Z0-9]+", "-")  // 非字母数字替换为连字符
                .replaceAll("^-|-$", "")           // 去除首尾连字符
                .toLowerCase();                     // 转换为小写

        String sanitizedFilename = slug + ".pdf";

        page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));
        page.pdf(new Page.PdfOptions().setPath(Paths.get(sanitizedFilename)).setFormat("A4").setPrintBackground(false));
        String msg = "Saving page with URL " + page.url() + " as PDF to ./" + sanitizedFilename;
        log.info(msg);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }



}
