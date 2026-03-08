package com.xiangyongshe.swim_admin.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostStatusRequest {
    @NotNull
    private Integer status;
    private String reason;
}
