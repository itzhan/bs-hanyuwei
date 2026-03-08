package com.xiangyongshe.swim_admin.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateRequest {
    private Long parentId;
    private Long replyToUserId;
    @NotBlank
    private String content;
}
