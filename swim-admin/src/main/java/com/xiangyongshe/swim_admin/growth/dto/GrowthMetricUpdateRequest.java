package com.xiangyongshe.swim_admin.growth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GrowthMetricUpdateRequest {
    private Integer metricType;
    private BigDecimal metricValue;
    private String unit;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordedAt;
    private String note;
}
