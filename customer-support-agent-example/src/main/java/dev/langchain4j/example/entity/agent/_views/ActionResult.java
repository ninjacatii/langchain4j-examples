package dev.langchain4j.example.entity.agent._views;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionResult {
    @Nullable
    private Boolean isDone;
    @Nullable private Boolean success;
    @Nullable private String extractedContent;
    @Nullable private String error;
    private boolean includeInMemory;
}
