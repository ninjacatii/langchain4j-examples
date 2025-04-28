package dev.langchain4j.example.entity.controller.registry._views;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public record RegisteredAction(
        String name,
        String description,
        Class[] paraType,
        String[] paraName,

        List<String> domains,
        Function<Page, Boolean> pageFilter
) {
    @JsonIgnore
    public String promptDescription() {
        String s = this.description + ": \n";

        JSONObject jo = JSONUtil.createObj();
        JSONObject para = JSONUtil.createObj();
        for (int i = 0; i < paraType.length; i++) {
            para.set(paraName[i], JSONUtil.createObj().set("type", String.valueOf(paraType[i])));
        }
        jo.set(name, para);
        s += jo.toStringPretty();

        return s;
    }
}
