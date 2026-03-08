package com.xiangyongshe.swim_admin.common.constant;

import com.xiangyongshe.swim_admin.common.model.DictOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ReportStatus {
    PENDING(1, "待处理"),
    PROCESSED(2, "已处理"),
    REJECTED(3, "已驳回");

    private final int value;
    private final String label;

    ReportStatus(int value, String label) {
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
