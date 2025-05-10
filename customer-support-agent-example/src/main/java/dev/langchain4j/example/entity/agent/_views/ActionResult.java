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
    @Builder.Default
    private Boolean isDone = false;
    @Builder.Default
    private Boolean success = null;
    @Builder.Default
    private String extractedContent = null;
    @Builder.Default
    private String error = null;
    @Builder.Default
    private boolean includeInMemory = false;
}
