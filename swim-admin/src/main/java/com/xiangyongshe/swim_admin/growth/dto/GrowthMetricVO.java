package com.xiangyongshe.swim_admin.growth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GrowthMetricVO {
    private Long id;
    private Long userId;
    private Long babyId;
    private Long sourceLogId;
    private Integer metricType;
    private BigDecimal metricValue;
    private String unit;
    private LocalDateTime recordedAt;
    private String note;
    private LocalDateTime createdAt;
}
