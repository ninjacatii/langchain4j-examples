package dev.langchain4j.example.entity.agent.message_manager;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class Utils {
    public static JSONObject extractJsonFromModelOutput(String content) {
        // 处理代码块包裹的情况
        if (content.contains("```")) {
            String[] codeBlocks = content.split("```");
            if (codeBlocks.length >= 2) {
                content = codeBlocks[1].trim();
                // 移除可能的语言标识符
                if (content.contains("\n")) {
                    String[] langAndJson = content.split("\n", 2);
                    if (langAndJson.length > 1) {
                        content = langAndJson[1].trim();
                    }
                }
            }
        }

        return JSONUtil.parseObj(content);
    }
}
