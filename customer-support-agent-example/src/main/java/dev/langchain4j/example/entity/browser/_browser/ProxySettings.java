package dev.langchain4j.example.entity.browser._browser;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProxySettings {
    private String server;
    private String bypass;
    private String username;
    private String password;

    public JSONObject modelDump() {
        return JSONUtil.parseObj(this);
    }
}
