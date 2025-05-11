package dev.langchain4j.example.entity.agent.message_manager;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class Utils {
    public static JSONObject extractJsonFromModelOutput(String content) throws Exception {
        String start = "```json";
        content = content.substring(content.indexOf(start) + start.length());
        String end = "```";
        content = content.substring(0, content.lastIndexOf(end));

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
        if (JSONUtil.isTypeJSONObject(content)) {
            return JSONUtil.parseObj(content);
        } else {
            log.error("JSONUtil.parseObj error, content:" + content);
            throw new Exception("JSONUtil.parseObj error");
        }
    }
}
