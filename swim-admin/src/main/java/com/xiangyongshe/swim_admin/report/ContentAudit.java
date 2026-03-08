package com.xiangyongshe.swim_admin.report;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_audit")
public class ContentAudit {
    @TableId
    private Long id;
    private Long operatorId;
    private Long postId;
    private Long commentId;
    private Long reportId;
    private Integer action;
    private String reason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
