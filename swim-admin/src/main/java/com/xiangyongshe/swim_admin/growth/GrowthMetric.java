package com.xiangyongshe.swim_admin.growth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyongshe.swim_admin.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("growth_metric")
public class GrowthMetric extends BaseEntity {
    private Long userId;
    private Long babyId;
    private Long sourceLogId;
    private Integer metricType;
    private BigDecimal metricValue;
    private String unit;
    private LocalDateTime recordedAt;
    private String note;
}
