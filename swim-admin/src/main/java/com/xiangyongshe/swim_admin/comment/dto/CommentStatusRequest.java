package com.xiangyongshe.swim_admin.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentStatusRequest {
    @NotNull
    private Integer status;
    private String reason;
}
