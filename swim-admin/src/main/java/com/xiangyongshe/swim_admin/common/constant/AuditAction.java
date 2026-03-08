package com.xiangyongshe.swim_admin.common.constant;

import com.xiangyongshe.swim_admin.common.model.DictOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AuditAction {
    APPROVE(1, "通过"),
    REJECT(2, "驳回"),
    BLOCK(3, "拉黑"),
    HIDE(4, "隐藏"),
    RESTORE(5, "恢复");

    private final int value;
    private final String label;

    AuditAction(int value, String label) {
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
