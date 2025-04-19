package dev.langchain4j.example.entity.agent._prompts;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.example.entity.agent._views.ActionResult;
import dev.langchain4j.example.entity.agent._views.AgentStepInfo;
import dev.langchain4j.example.entity.browser._views.BrowserState;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AgentMessagePrompt {
    private BrowserState state;
    private List<ActionResult> result;
    private List<String> includeAttributes;
    private AgentStepInfo stepInfo;

    public AgentMessagePrompt(
            BrowserState state,
            @Nullable List<ActionResult> result,
    @Nullable List<String> includeAttributes,
    @Nullable
    AgentStepInfo stepInfo
    ) {
        this.state = state;
        this.result = result;
        this.includeAttributes = includeAttributes != null ? includeAttributes : List.of();
        this.stepInfo = stepInfo;
    }

    public UserMessage getUserMessage(boolean useVision) {
        String elementsText = this.state.getElementTree().clickableElementsToString(this.includeAttributes);

        boolean hasContentAbove = this.state.getPixelsAbove() != null && this.state.getPixelsAbove() > 0;
        boolean hasContentBelow = this.state.getPixelsBelow() != null && this.state.getPixelsBelow() > 0;

        if (!elementsText.isEmpty()) {
            if (hasContentAbove) {
                elementsText = "... " + this.state.getPixelsAbove() + " pixels above - scroll or extract content to see more ...\n" + elementsText;
            } else {
                elementsText = "[Start of page]\n" + elementsText;
            }
            if (hasContentBelow) {
                elementsText = elementsText + "\n... " + this.state.getPixelsBelow() + " pixels below - scroll or extract content to see more ...";
            } else {
                elementsText = elementsText + "\n[End of page]";
            }
        } else {
            elementsText = "empty page";
        }

        String stepInfoDescription = "";
        if (this.stepInfo != null) {
            stepInfoDescription = "Current step: " + (this.stepInfo.getStepNumber() + 1) + "/" + this.stepInfo.getMaxSteps();
        }
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        stepInfoDescription += "Current date and time: " + timeStr;

        String stateDescription = """
[Task history memory ends]
[Current state starts here]
The following is one-time information - if you need to remember it write it to memory:
Current url: """ + this.state.getUrl() + """
Available tabs:
""" + this.state.getTabs() + """
Interactive elements from top layer of the current page inside the viewport:
""" + elementsText + """
""" + stepInfoDescription;

        if (this.result != null) {
            for (int i = 0; i < this.result.size(); i++) {
                ActionResult result = this.result.get(i);
                if (result.getExtractedContent() != null) {
                    stateDescription += "\nAction result " + (i + 1) + "/" + this.result.size() + ": " + result.getExtractedContent();
                }
                if (result.getError() != null) {
                    String error = result.getError().split("\n")[result.getError().split("\n").length - 1];
                    stateDescription += "\nAction error " + (i + 1) + "/" + this.result.size() + ": ..." + error;
                }
            }
        }

        if (this.state.getScreenshot() != null && useVision) {
            return new UserMessage(
                    List.of(
                            TextContent.from(stateDescription),
                            ImageContent.from(this.state.getScreenshot(), "image/png")
                    )
            );
        }

        return new UserMessage(stateDescription);
    }
}
