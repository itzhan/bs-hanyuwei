package com.xiangyongshe.swim_admin.report;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyongshe.swim_admin.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_report")
public class ContentReport extends BaseEntity {
    private Long reporterId;
    private Long postId;
    private Long commentId;
    private Integer reason;
    private String reasonDesc;
    private Integer status;
    private Long handledBy;
    private LocalDateTime handledAt;
    private Integer handleResult;
    private String handleNote;
}
