package com.xiangyongshe.swim_admin.common.constant;

import com.xiangyongshe.swim_admin.common.model.DictOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum LogType {
    FEEDING(1, "喂养"),
    SLEEP(2, "睡眠"),
    ILLNESS(3, "疾病"),
    MILESTONE(4, "里程碑"),
    MEASUREMENT(5, "测量"),
    OTHER(6, "其他");

    private final int value;
    private final String label;

    LogType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static List<DictOption> options() {
        return Arrays.stream(values())
                .map(v -> new DictOption(v.value, v.label))
                .collect(Collectors.toList());
    }
}
