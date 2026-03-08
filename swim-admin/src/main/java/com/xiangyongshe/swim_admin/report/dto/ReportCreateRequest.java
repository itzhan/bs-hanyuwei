package com.xiangyongshe.swim_admin.report.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportCreateRequest {
    private Long postId;
    private Long commentId;
    @NotNull
    private Integer reason;
    private String reasonDesc;
}
