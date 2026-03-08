package com.xiangyongshe.swim_admin.growth.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GrowthMetricUpdateRequest {
    private Integer metricType;
    private BigDecimal metricValue;
    private String unit;
    private LocalDateTime recordedAt;
    private String note;
}
