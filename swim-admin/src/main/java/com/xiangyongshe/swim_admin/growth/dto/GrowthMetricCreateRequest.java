package com.xiangyongshe.swim_admin.growth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GrowthMetricCreateRequest {
    @NotNull
    private Long babyId;
    private Long sourceLogId;
    @NotNull
    private Integer metricType;
    @NotNull
    private BigDecimal metricValue;
    private String unit;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordedAt;
    private String note;
}
