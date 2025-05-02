package dev.langchain4j.example.entity.controller.registry._views;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Hashtable;

public class ActionModel extends HashMap<String, HashMap<String, Object>> {
    public Integer getIndex() {
        // Get the index of the action
		// {'clicked_element': {'index':5}}
        for (HashMap<String, Object> params: this.values()) {
            for (String param : params.keySet()) {
                if ("index".equals(param) && params.get(param) instanceof Integer) {
                    return (Integer) params.get(param);
                }
            }
        }
        return null;
    }
}
