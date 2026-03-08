package com.xiangyongshe.swim_admin.growth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyongshe.swim_admin.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("growth_log")
public class GrowthLog extends BaseEntity {
    private Long userId;
    private Long babyId;
    private Integer logType;
    private String title;
    private String content;
    private LocalDateTime logTime;
}
