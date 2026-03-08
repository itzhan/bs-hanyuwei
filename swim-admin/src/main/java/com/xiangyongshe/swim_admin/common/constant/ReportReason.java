package com.xiangyongshe.swim_admin.common.constant;

import com.xiangyongshe.swim_admin.common.model.DictOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ReportReason {
    SPAM(1, "垃圾广告"),
    ABUSE(2, "辱骂攻击"),
    PRIVACY(3, "隐私泄露"),
    ILLEGAL(4, "违法违规"),
    FALSE_INFO(5, "虚假信息"),
    OTHER(6, "其他");

    private final int value;
    private final String label;

    ReportReason(int value, String label) {
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
