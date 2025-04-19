package dev.langchain4j.example.entity.browser._views;

import lombok.Data;

import java.util.List;

@Data
public class GroupTabsAction {
    private List<Integer> tabIds;
    private String title;
    private String color = "blue";
}
