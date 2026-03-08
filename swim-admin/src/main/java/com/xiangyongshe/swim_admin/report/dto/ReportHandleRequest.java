package com.xiangyongshe.swim_admin.report.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportHandleRequest {
    @NotNull
    private Integer status;
    @NotNull
    private Integer handleResult;
    private String handleNote;
}
