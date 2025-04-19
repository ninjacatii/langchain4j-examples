package dev.langchain4j.example.entity.browser._views;

import lombok.Data;

import java.util.List;

@Data
public class UngroupTabsAction {
    private List<Integer> tabIds;
}
