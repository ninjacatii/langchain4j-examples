package dev.langchain4j.example.entity.agent._views;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StepMetadata {
    private int stepNumber;
    private DateTime stepStartTime;
    private DateTime stepEndTime;
    private int inputTokens;

    public double getDurationSeconds() {
        return DateUtil.between(stepStartTime, stepEndTime, DateUnit.SECOND);
    }
}
