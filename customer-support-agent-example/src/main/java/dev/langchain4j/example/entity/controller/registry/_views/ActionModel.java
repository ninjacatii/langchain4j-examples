package dev.langchain4j.example.entity.controller.registry._views;

import cn.hutool.core.convert.Convert;

import java.util.HashMap;

public class ActionModel extends HashMap<String, HashMap<String, Object>> {
    public Integer getIndex() {
        // Get the index of the action
		// {'clicked_element': {'index':5}}
        for (HashMap<String, Object> params: this.values()) {
            if (params.containsKey("index")) {
                return Convert.toInt(params.get("index"));
            }
        }
        return null;
    }

    public void setIndex(int index) {
        for (HashMap<String, Object> params: this.values()) {
            if (params.containsKey("index")) {
                params.put("index", index);
            }
        }
    }
}
