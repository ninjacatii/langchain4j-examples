package dev.langchain4j.example.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Media;
import dev.langchain4j.example.entity.agent._views.ActionResult;
import dev.langchain4j.example.entity.browser._context.BrowserContext;
import dev.langchain4j.example.entity.browser._context.BrowserSession;
import dev.langchain4j.example.entity.dom._views.DOMElementNode;
import dev.langchain4j.example.iface.MethodToAction;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.github.furstenheim.CopyDown;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Actions {
    @MethodToAction(
            description = "Complete task - with return text and if the task is finished (success=True) or not yet  completely finished (success=False), because last step is reached",
            paraType = { String.class, Boolean.class },
            paraName = { "text", "success" }
    )
    public static ActionResult done(String text, Boolean success, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        return ActionResult.builder().isDone(true).success(success).extractedContent(text).build();
    }

    @MethodToAction(
        description = "Search the query in Google in the current tab, the query should be a search query like humans search in Google, concrete and not vague or super long. More the single most important items. ",
        paraType = { String.class },
        paraName = { "query" }
    )
    public static ActionResult search_google(String query, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
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
    public static ActionResult go_to_url(String url, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
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
    public static ActionResult go_back(BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
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
    public static ActionResult wait(Integer seconds, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        String msg = "🕒  Waiting for {seconds} seconds";
        log.info(msg);
        ThreadUtil.sleep(seconds, TimeUnit.SECONDS);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Click element by index",
            paraType = { Integer.class },
            paraName = { "index" }
    )
    public static ActionResult click_element(Integer index, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) throws Exception {
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
            paraType = { Integer.class, String.class },
            paraName = { "index", "text" }
    )
    public static ActionResult input_text(Integer index, String text, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) throws Exception {
        if (!browser.getSelectorMap().containsKey(index)) {
            throw new Exception("Element with index " + index + " does not exist - retry or use alternative actions");
        }

        DOMElementNode elementNode = browser.getDomElementByIndex(index);
        browser.inputTextElementNode(elementNode, text);
        String msg = "⌨️  Input {params.text} into index " + index;
        log.info(msg);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Save the current page as a PDF file",
            paraType = {  },
            paraName = {  }
    )
    public static ActionResult save_pdf(BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
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

    @MethodToAction(
            description = "Switch tab",
            paraType = { Integer.class },
            paraName = { "pageId" }
    )
    public static ActionResult switch_tab(Integer pageId, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        browser.switchToTab(pageId);

        Page page = browser.getCurrentPage();
        page.waitForLoadState();
        String msg = "🔄  Switched to tab " + pageId;
        log.info(msg);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Open url in new tab",
            paraType = { String.class },
            paraName = { "url" }
    )
    public static ActionResult open_tab(String url, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        browser.createNewTab(url);
        String msg = "🔗  Opened new tab with " + url;
        log.info(msg);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Close an existing tab",
            paraType = { Integer.class },
            paraName = { "pageId" }
    )
    public static ActionResult close_tab(Integer pageId, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        browser.switchToTab(pageId);
        Page page = browser.getCurrentPage();
        String url = page.url();
        page.close();
        String msg = "❌  Closed tab #" + pageId + " with url " + url;
        log.info(msg);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Extract page content to retrieve specific information from the page, e.g. all company names, a specific description, all information about, links with companies in structured format or simply links",
            paraType = { String.class, Boolean.class },
            paraName = { "goal", "shouldStripLinkUrls" }
    )
    public static ActionResult extract_content(String goal, Boolean shouldStripLinkUrls, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        Page page = browser.getCurrentPage();

        var converter = new CopyDown();
        String content = converter.convert(page.content());

        for (Frame iframe: page.frames()) {
            if (!page.url().equals(iframe.url()) && !iframe.url().startsWith("data:")) {
                content += "\n\nIFRAME " + iframe.url() + ":\n";
                content += converter.convert(iframe.content());
            }
        }

        String prompt = "Your task is to extract the content of the page. You will be given a page and a goal and you should extract all relevant information around this goal from the page. If the goal is vague, summarize the page. Respond in json format. Extraction goal: " + goal + ", Page: " + content;
        try {
            String output = pageExtractionLlm.chat(prompt);
            String msg = "📄  Extracted from page\n: " + output + "\n";
            log.info(msg);
            return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
        } catch (Exception e) {
            log.error("Error extracting content: " + e.getMessage(), e);
            String msg = "📄  Extracted from page\n: " + content + "\n";
            log.info(msg);
            return ActionResult.builder().extractedContent(msg).build();
        }
    }

    @MethodToAction(
            description = "Scroll down the page by pixel amount - if no amount is specified, scroll down one page",
            paraType = { Integer.class },
            paraName = { "amount" }
    )
    public static ActionResult scroll_down(Integer amount, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        Page page = browser.getCurrentPage();
        if (amount != null) {
            page.evaluate("window.scrollBy(0, " + amount + ");");
        } else {
            page.evaluate("window.scrollBy(0, window.innerHeight);");
        }

        String amountStr = amount != null ? amount + " pixels" : "one page";
        String msg = "🔍  Scrolled down the page by " + amountStr;
        log.info(msg);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Scroll up the page by pixel amount - if no amount is specified, scroll up one page",
            paraType = { Integer.class },
            paraName = { "amount" }
    )
    public static ActionResult scroll_up(Integer amount, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        Page page = browser.getCurrentPage();
        if (amount != null) {
            page.evaluate("window.scrollBy(0, " + -amount + ");");
        } else {
            page.evaluate("window.scrollBy(0, -window.innerHeight);");
        }

        String amountStr = amount != null ? amount + " pixels" : "one page";
        String msg = "🔍  Scrolled up the page by " + amountStr;
        log.info(msg);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "Send strings of special keys like Escape,Backspace, Insert, PageDown, Delete, Enter, Shortcuts such as `Control+o`, `Control+Shift+T` are supported as well. This gets used in keyboard.press. ",
            paraType = { String.class },
            paraName = { "keys" }
    )
    public static ActionResult send_keys(String keys, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        Page page = browser.getCurrentPage();

        try {
            page.keyboard().press(keys);
        } catch (Exception e) {
            if (e.getMessage().indexOf("Unknown key") >= 0) {
                for (String key: keys.split(",")) {
                    try {
                        page.keyboard().press(key.trim());
                    } catch (Exception ex) {
                        log.error("Error sending key " + key.trim() + ": " + e.getMessage(), e);
                        throw ex;
                    }
                }
            } else {
                throw e;
            }
        }
        String msg = "⌨️  Sent keys: " + keys;
        log.info(msg);
        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
    }

    @MethodToAction(
            description = "If you dont find something which you want to interact with, scroll to it",
            paraType = { String.class },
            paraName = { "text" }
    )
    public static ActionResult scroll_to_text(String text, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        Page page = browser.getCurrentPage();
        try {
            List<Locator> locators = Arrays.asList(
                    page.getByText(text),
                    page.locator("text=" + text),
                    page.locator("//*[contains(text(), '" + text + "')]")
            );

            for (Locator locator: locators) {
                try {
                    if (locator.count() > 0 && locator.first().isVisible()) {
                        locator.first().scrollIntoViewIfNeeded();
                        ThreadUtil.sleep(0.5, TimeUnit.SECONDS);
                        String msg = "🔍  Scrolled to text: " + text;
                        log.info(msg);
                        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
                    }
                } catch (Exception e) {
                    log.error("Locator attempt failed: " + e.getMessage(), e);
                    continue;
                }
            }

            String msg = "Text '" + text + "' not found or not visible on page";
            log.info(msg);
            return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
        } catch (Exception e) {
            String msg = "Failed to scroll to text '" + text + "': " + e.getMessage();
            log.error(msg, e);
            return ActionResult.builder().error(msg).includeInMemory(true).build();
        }
    }

    @MethodToAction(
            description = "Get all options from a native dropdown",
            paraType = { Integer.class },
            paraName = { "index" }
    )
    public static ActionResult get_dropdown_options(Integer index, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        Page page = browser.getCurrentPage();
        Map<Integer, DOMElementNode> selectorMap = browser.getSelectorMap();
        DOMElementNode domElement = selectorMap.get(index);

        try {
            var allOptions = new ArrayList<String>();
            int frameIndex = 0;

            for (Frame frame: page.frames()) {
                try {
                    Map options = (Map)frame.evaluate("""
							(xpath) => {
								const select = document.evaluate(xpath, document, null,
									XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
								if (!select) return null;

								return {
									options: Array.from(select.options).map(opt => ({
										text: opt.text, //do not trim, because we are doing exact match in select_dropdown_option
										value: opt.value,
										index: opt.index
									})),
									id: select.id,
									name: select.name
								};
							}
                            """, domElement.getXpath());
                    if (options != null) {
                        log.debug("Found dropdown in frame {}", frameIndex);
                        log.debug("Dropdown ID: {}, Name: {}", options.get("index"), options.get("name"));

                        var formattedOptions = new ArrayList<String>();
                        for (Object o: (List)(options.get("options"))) {
                            Map opt = (Map)o;
                            String encodedText = JSONUtil.toJsonStr(opt.get("text"));
                            formattedOptions.add(opt.get("index") + ": text=" + encodedText);
                        }
                        allOptions.addAll(formattedOptions);
                    }
                } catch (Exception e) {
                    log.debug("Frame {} evaluation failed: {}", frameIndex, e.getMessage(), e);
                }
                frameIndex += 1;
            }

            if (!CollUtil.isEmpty(allOptions)) {
                String msg = StrUtil.join("\n", allOptions);
                msg += "\nUse the exact text string in select_dropdown_option";
                log.info(msg);
                return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
            } else {
                String msg = "No options found in any frame for dropdown";
                log.info(msg);
                return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
            }
        } catch (Exception e) {
            log.error("Failed to get dropdown options: " + e.getMessage(), e);
            String msg = "Error getting options: " + e.getMessage();
            log.info(msg);
            return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
        }
    }

    @MethodToAction(
            description = "Select dropdown option for interactive element index by the text of the option you want to select",
            paraType = { Integer.class, String.class },
            paraName = { "index", "text" }
    )
    public static ActionResult select_dropdown_option(Integer index, String text, BrowserContext browser, OpenAiStreamingChatModel pageExtractionLlm) {
        Page page = browser.getCurrentPage();
        Map<Integer, DOMElementNode> selectorMap = browser.getSelectorMap();
        DOMElementNode domElement = selectorMap.get(index);

        if (!"select".equals(domElement.getTagName())) {
            log.error("Element is not a select! Tag: {}, Attributes: {}", domElement.getTagName(), domElement.getAttributes());
            String msg = "Cannot select option: Element with index " + index + " is a " + domElement.getTagName() + ", not a select";
            return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
        }

        log.debug("Attempting to select '{}' using xpath: {}", text, domElement.getXpath());
        log.debug("Element attributes: {}", domElement.getAttributes());
        log.debug("Element tag: {}", domElement.getTagName());

        try {
            int frameIndex = 0;
            for (Frame frame: page.frames()) {
                try {
                    log.debug("Trying frame {} URL: {}", frameIndex, frame.url());

                    String findDropdownJs = """
							(xpath) => {
								try {
									const select = document.evaluate(xpath, document, null,
										XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
									if (!select) return null;
									if (select.tagName.toLowerCase() !== 'select') {
										return {
											error: `Found element but it's a ${select.tagName}, not a SELECT`,
											found: false
										};
									}
									return {
										id: select.id,
										name: select.name,
										found: true,
										tagName: select.tagName,
										optionCount: select.options.length,
										currentValue: select.value,
										availableOptions: Array.from(select.options).map(o => o.text.trim())
									};
								} catch (e) {
									return {error: e.toString(), found: false};
								}
							}
                            """;

                    Map dropdownInfo = (Map)frame.evaluate(findDropdownJs, domElement.getXpath());

                    if (dropdownInfo != null) {
                        if (!dropdownInfo.containsKey("found")) {
                            log.error("Frame {} error: {}", frameIndex, dropdownInfo.get("error"));
                            continue;
                        }

                        log.debug("Found dropdown in frame {}: {}", frameIndex, dropdownInfo);

                        List<String> selectedOptionValues = frame.locator("//" + domElement.getXpath()).nth(0).selectOption(text);

                        String msg = "selected option " + text + " with value " + JSONUtil.toJsonStr(selectedOptionValues);
                        log.info(msg + " in frame {}", frameIndex);

                        return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
                    }
                } catch (Exception e) {
                    log.error("Frame {} attempt failed: {}", frameIndex, e.getMessage(), e);
                    log.error("Frame type: {}", frame.getClass().getName());
                    log.error("Frame URL: {}", frame.url());
                }

                frameIndex += 1;
            }
            String msg = "Could not select option '" + text + "' in any frame";
            log.error(msg);
            return ActionResult.builder().extractedContent(msg).includeInMemory(true).build();
        } catch (Exception e) {
            String msg = "Selection failed: " + e.getMessage();
            log.error(msg);
            return ActionResult.builder().error(msg).includeInMemory(true).build();
        }
    }






}
