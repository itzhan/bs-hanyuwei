package com.xiangyongshe.swim_admin.growth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GrowthLogCreateRequest {
    @NotNull
    private Long babyId;
    @NotNull
    private Integer logType;
    private String title;
    private String content;
    @NotNull
    private LocalDateTime logTime;
}
