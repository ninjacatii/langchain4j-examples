package dev.langchain4j.example.tests;

import dev.langchain4j.example.entity.agent._service.Agent;
import dev.langchain4j.example.entity.agent._views.AgentHistoryList;
import dev.langchain4j.example.entity.browser._browser.Browser;
import dev.langchain4j.example.entity.browser._browser.BrowserConfig;
import dev.langchain4j.example.entity.browser._context.BrowserContext;
import dev.langchain4j.example.entity.browser._views.BrowserState;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TestAgentActions {
    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;
    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;
    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    private Browser browser;
    private BrowserContext context;
    private OpenAiChatModel llm;

    @BeforeEach
    void setup() {
        BrowserConfig config = BrowserConfig.builder().headless(false).build();
        this.browser = new Browser(config);
        this.context = browser.newContext(null);
        llm = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
    }

    @Test
    @Disabled("Expensive to run")
    void testEcommerceInteraction() {
        Agent agent = new Agent(
//                "请根据链接：https://digvps.com/review，找到链接里的评分等于8的VPS厂商（不要理会不评分或评分未知的厂商），点进这些厂商对应的链接，随机查看每个厂商提供的五款VPS价格和天梯等级，最后将年付价格低于1000块，且天梯等级为E1、E2、E3的VPS列出来，合并做成MD表格。注意：在处理网页信息时，不要原网页内容进行翻译。",
                "Go to taobao.com, search for 'laptop', filter by 4+ stars, and find the price of the first result",
                llm,
                context);

        AgentHistoryList history = agent.run(20, null, null);

        List<String> actionSequence = new ArrayList<>();
        for (Map<String, Object> action : history.modelActions()) {
            String actionName = action.keySet().iterator().next();
            switch (actionName) {
                case "go_to_url":
                case "open_tab":
                    actionSequence.add("navigate");
                    break;
                case "input_text":
                    actionSequence.add("input");
                    String inputText = ((Map<String, String>)action.get("input_text")).get("text").toLowerCase();
                    if (inputText.equals("laptop")) {
                        actionSequence.add("input_exact_correct");
                    } else if (inputText.contains("laptop")) {
                        actionSequence.add("correct_in_input");
                    } else {
                        actionSequence.add("incorrect_input");
                    }
                    break;
                case "click_element":
                    actionSequence.add("click");
                    break;
            }
        }

        assertTrue(actionSequence.contains("navigate"));
        assertTrue(actionSequence.contains("input"));
        assertTrue(actionSequence.contains("click"));
        assertTrue(actionSequence.contains("input_exact_correct") ||
                actionSequence.contains("correct_in_input"));
    }

    @Test
    void testErrorRecovery() {
        Agent agent = new Agent(
                "Navigate to nonexistent-site.com and then recover by going to google.com",
                llm,
                context);

        AgentHistoryList history = agent.run(10, null, null);
        List<String> actionNames = history.actionNames();
        List<LinkedHashMap<String, Object>> actions = history.modelActions();

        assertTrue(actionNames.contains("go_to_url") || actionNames.contains("open_tab"));

        for (Map<String, Object> action : actions) {
            if (action.containsKey("go_to_url")) {
                assertTrue(((Map<String, String>)action.get("go_to_url")).get("url").endsWith("google.com"));
                break;
            }
        }
    }

    @Test
    void testFindContactEmail() {
        Agent agent = new Agent(
                "Go to https://browser-use.com/ and find out the contact email",
                llm,
                context);

        AgentHistoryList history = agent.run(10, null, null);
        List<String> extractedContent = history.extractedContent();
        String email = "info@browser-use.com";

        assertTrue(extractedContent.stream().anyMatch(content -> content.contains(email)),
                "Extracted content does not contain expected email");
    }

    @Test
    void testFindInstallationCommand() {
        Agent agent = new Agent(
                "Find the pip installation command for the browser-use repo",
                llm,
                context);

        AgentHistoryList history = agent.run(10, null, null);
        List<String> extractedContent = history.extractedContent();
        String installCommand = "pip install browser-use";

        assertTrue(extractedContent.stream().anyMatch(content -> content.contains(installCommand)),
                "Extracted content does not contain installation command");
    }

    @Test
    void testCaptchaSolver() {
        List<CaptchaTest> testCases = Arrays.asList(
                new CaptchaTest("Text Captcha", "https://2captcha.com/demo/text", "Captcha is passed successfully!"),
                new CaptchaTest("Basic Captcha", "https://captcha.com/demos/features/captcha-demo.aspx", "Correct!"),
                new CaptchaTest("Rotate Captcha", "https://2captcha.com/demo/rotatecaptcha", "Captcha is passed successfully"),
                new CaptchaTest("MT Captcha", "https://2captcha.com/demo/mtcaptcha", "Verified Successfully")
        );

        for (CaptchaTest testCase : testCases) {
            Agent agent = new Agent(
                    String.format("Go to %s and solve the captcha", testCase.getUrl()),
                    llm,
                    context);

            AgentHistoryList history = agent.run(7, null, null);
            BrowserState state = context.getState(false);
            String allText = state.getElementTree().getAllTextTillNextClickableElement(-1);

            assertTrue(allText.contains(testCase.getSuccessText()),
                    String.format("Failed to solve %s captcha", testCase.getName()));
        }
    }

    static class CaptchaTest {
        private final String name;
        private final String url;
        private final String successText;

        public CaptchaTest(String name, String url, String successText) {
            this.name = name;
            this.url = url;
            this.successText = successText;
        }

        public String getName() { return name; }
        public String getUrl() { return url; }
        public String getSuccessText() { return successText; }
    }
}
