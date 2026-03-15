package com.xiangyongshe.swim_admin.growth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logTime;
}
