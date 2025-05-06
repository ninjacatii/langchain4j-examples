package dev.langchain4j.example.entity.agent._views;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.example.entity.controller.registry._views.ActionModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentHistoryList {
    private List<AgentHistory> history;

    public boolean isDone() {
        if (this.history != null && this.history.get(this.history.size() - 1).getResult().size() > 0) {
            List<ActionResult> list = this.history.get(this.history.size() - 1).getResult();
            ActionResult lastResult = list.get(list.size() - 1);
            return lastResult.getIsDone();
        }
        return false;
    }

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

    public JSONObject lastAction() {
        if (this.history != null && this.history.get(this.history.size() - 1).getModelOutput() != null) {
            List<ActionModel> lastAction = this.history.get(this.history.size() - 1).getModelOutput().getAction();
            if (lastAction != null) {
                return lastAction.get(lastAction.size() - 1).modelDump(true);
            }
        }
        return null;
    }

    public List<String> errors() {
        var errors = new ArrayList<String>();
        for (AgentHistory h: this.history) {
            if (h.getResult() != null) {
                for (ActionResult r: h.getResult()) {
                    if (StrUtil.isNotBlank(r.getError())) {
                        errors.add(r.getError());
                    }
                }
            }
        }
        return errors;
    }

    public String finalResult() {
        if (!CollUtil.isEmpty(this.history)) {
            List<ActionResult> result = this.history.get(this.history.size() - 1).getResult();
            if (result != null) {
                return result.get(result.size() - 1).getExtractedContent();
            }
        }
        return null;
    }
}
