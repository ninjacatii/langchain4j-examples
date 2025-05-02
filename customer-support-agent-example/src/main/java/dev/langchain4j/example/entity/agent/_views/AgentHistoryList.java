package dev.langchain4j.example.entity.agent._views;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class AgentHistoryList {
    private List<AgentHistory> history;
    private boolean isDone;

    public double totalDurationSeconds() {
        return history.stream()
                .filter(h -> h.getMetadata() != null)
                .mapToDouble(h -> h.getMetadata().getDurationSeconds())
                .sum();
    }

    public int totalInputTokens() {
        return history.stream()
                .filter(h -> h.getMetadata() != null)
                .mapToInt(h -> h.getMetadata().getInputTokens())
                .sum();
    }

    public void saveToFile(String filepath) throws IOException {
        Path path = Path.of(filepath);
        Files.createDirectories(path.getParent());

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(JSONUtil.toJsonStr(this));

        Files.writeString(path, json);
    }

    public static AgentHistoryList loadFromFile(
            String filepath,
            Class<? extends AgentOutput> outputModel
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = Files.readString(Path.of(filepath));
        return mapper.readValue(json, AgentHistoryList.class);
    }

    public Boolean isSuccessful() {
        if (!CollUtil.isEmpty(this.history) && !this.history.get(this.history.size() - 1).getResult().isEmpty()) {
            List<ActionResult> list = this.history.get(this.history.size() - 1).getResult();
            ActionResult lastResult = list.get(list.size() - 1);
            if (lastResult.getIsDone()) {
                return lastResult.getSuccess();
            }
        }
        return null;
    }
}
