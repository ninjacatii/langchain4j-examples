package dev.langchain4j.example.tests;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.example.entity.agent._service.Agent;
import dev.langchain4j.example.entity.browser._browser.Browser;
import dev.langchain4j.example.entity.browser._browser.BrowserConfig;
import dev.langchain4j.example.entity.browser._context.BrowserContext;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestMind2Web {
    private static final int TEST_SUBSET_SIZE = 10;
    private static final int MAX_STEPS = 50;

    private Browser browser;
    private List<Map<String, Object>> testCases;

    @BeforeAll
    static void setupAll() {
        // 初始化全局测试资源
    }

    @BeforeEach
    void setup() throws IOException {
        // 初始化浏览器实例
        BrowserConfig config = BrowserConfig.builder().headless(true).build();
        this.browser = new Browser(config);

        InputStream inputStream = TestMind2Web.class.getClassLoader().getResourceAsStream("processed.json");
        String jsonContent = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent += line + "\n";
            }
        }

        List<Map<String, Object>> allCases = JSONUtil.toBean(
                jsonContent,
                new TypeReference<>() {
                },
                false
        );
        this.testCases = allCases.subList(0, Math.min(TEST_SUBSET_SIZE, allCases.size()));
    }

    @AfterEach
    void tearDown() {
        if (browser != null) {
            browser.close();
        }
    }

    @Test
    void testDatasetIntegrity() {
        List<String> requiredFields = Arrays.asList("website", "confirmed_task", "action_reprs");
        List<String> missingFields = new ArrayList<>();

        for (Map<String, Object> testCase : testCases) {
            for (String field : requiredFields) {
                if (!testCase.containsKey(field)) {
                    missingFields.add("Missing field: " + field);
                }
            }

            // 类型检查
            assertInstanceOf(String.class, testCase.get("confirmed_task"),
                    "'confirmed_task' must be string");

            assertInstanceOf(List.class, testCase.get("action_reprs"),
                    "'action_reprs' must be list");

            assertFalse(((List<?>)testCase.get("action_reprs")).isEmpty(),
                    "Must have at least one action");
        }

        assertTrue(missingFields.isEmpty(),
                String.join("\n", missingFields));
    }

    @Test
    void testRandomSamples() {
        // 随机选择测试用例
        Collections.shuffle(testCases);
        List<Map<String, Object>> samples = testCases.subList(0, 1);

        for (Map<String, Object> sample : samples) {
            String task = String.format("Go to %s.com and %s",
                    sample.get("website"),
                    sample.get("confirmed_task"));

//            BrowserContext context = browser.newContext();
//            Agent agent = new Agent(task, context);
//
//            // 执行测试
//            agent.run(MAX_STEPS);
//
//            // TODO: 添加验证逻辑
        }
    }
}
