package dev.langchain4j.example.entity.controller.registry._views;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

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

    public ActionModel getAction(String name, JSONObject paramValues) throws Exception {
        if (!this.containsKey(name)) {
            throw new Exception("action[" + name + "] isn't founded.");
        }

        var actionModel = new ActionModel();
        var paras = new HashMap<String, Object>();
        actionModel.put(name, paras);

        HashMap<String, Object> map = this.get(name);
        for (String key: map.keySet()) {
            Object val = paramValues.getOrDefault(key, null);
            if (val == null) {
                paras.remove(key);
            } else {
                paras.put(key, val);
            }
        }
        return actionModel;
    }

    public JSONObject modelDump(boolean excludeNone) {
        return JSONUtil.parseObj(this);
    }
}
