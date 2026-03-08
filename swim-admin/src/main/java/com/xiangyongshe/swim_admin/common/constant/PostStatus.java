package com.xiangyongshe.swim_admin.common.constant;

import com.xiangyongshe.swim_admin.common.model.DictOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PostStatus {
    PUBLISHED(1, "已发布"),
    PENDING(2, "待审核"),
    HIDDEN(3, "已隐藏"),
    REJECTED(4, "已驳回"),
    BLOCKED(5, "已拉黑");

    private final int value;
    private final String label;

    PostStatus(int value, String label) {
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
