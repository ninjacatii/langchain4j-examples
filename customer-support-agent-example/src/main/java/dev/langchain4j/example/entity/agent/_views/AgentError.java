package dev.langchain4j.example.entity.agent._views;

import java.util.Arrays;

public class AgentError {
    public static final String VALIDATION_ERROR = "Invalid model output format. Please follow the correct schema.";
    public static final String RATE_LIMIT_ERROR = "Rate limit reached. Waiting before retry.";
    public static final String NO_VALID_ACTION = "No valid action found";

    public static String formatError(Throwable error, boolean includeTrace) {
//        if (error instanceof ValidationError) {
//            return VALIDATION_ERROR + "\nDetails: " + error.getMessage();
//        } else if (error instanceof RateLimitError) {
//            return RATE_LIMIT_ERROR;
//        } else if (includeTrace) {
//            return error.getMessage() + "\nStacktrace:\n" + Arrays.toString(error.getStackTrace());
//        } else {
//            return error.getMessage();
//        }
        if (includeTrace) {
            return error.getMessage() + "\nStacktrace:\n" + Arrays.toString(error.getStackTrace());
        } else {
            return error.getMessage();
        }
    }
}

