package com.xiangyongshe.swim_admin.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReportVO {
    private Long id;
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
    private LocalDateTime createdAt;
}
