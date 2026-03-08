package com.xiangyongshe.swim_admin.common.constant;

import com.xiangyongshe.swim_admin.common.model.DictOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum HandleResult {
    IGNORE(1, "忽略"),
    DELETE(2, "删除"),
    BLOCK(3, "拉黑"),
    WARN(4, "警告");

    private final int value;
    private final String label;

    HandleResult(int value, String label) {
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
