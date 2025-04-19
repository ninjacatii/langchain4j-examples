package dev.langchain4j.example.entity.agent._views;

import lombok.Data;

@Data
public class AgentHistoryList {
    private List<AgentHistory> history;

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
        String json = mapper.writeValueAsString(this.modelDump());

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

    // Additional methods omitted for brevity...
}
