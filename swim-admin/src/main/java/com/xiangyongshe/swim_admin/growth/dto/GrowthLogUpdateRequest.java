package com.xiangyongshe.swim_admin.growth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GrowthLogUpdateRequest {
    private Integer logType;
    private String title;
    private String content;
    private LocalDateTime logTime;
}
