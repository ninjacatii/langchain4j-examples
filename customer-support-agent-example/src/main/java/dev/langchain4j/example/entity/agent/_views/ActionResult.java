package dev.langchain4j.example.entity.agent._views;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Builder
public class ActionResult {
    private Boolean isDone = false;
    private Boolean success = null;
    private String extractedContent = null;
    private String error = null;
    private boolean includeInMemory = false;
}
