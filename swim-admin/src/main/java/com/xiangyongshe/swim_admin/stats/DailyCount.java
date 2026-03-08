package com.xiangyongshe.swim_admin.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyCount {
    private String day;
    private Long total;
}
