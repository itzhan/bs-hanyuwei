package com.xiangyongshe.swim_admin.growth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GrowthLogVO {
    private Long id;
    private Long userId;
    private Long babyId;
    private Integer logType;
    private String title;
    private String content;
    private LocalDateTime logTime;
    private LocalDateTime createdAt;
}
