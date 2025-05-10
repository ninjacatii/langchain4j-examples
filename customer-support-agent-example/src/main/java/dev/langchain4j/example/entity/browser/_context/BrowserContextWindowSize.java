package dev.langchain4j.example.entity.browser._context;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.microsoft.playwright.options.RecordVideoSize;
import lombok.Data;

public class BrowserContextWindowSize extends RecordVideoSize {
    public BrowserContextWindowSize(int width, int height) {
        super(width, height);
    }
    public JSONObject modelDump() {
        return JSONUtil.parseObj(this);
    }
}
