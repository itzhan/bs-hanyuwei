package com.xiangyongshe.swim_admin.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    private Long categoryId;
    private Long topicId;
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private List<Long> tagIds;
}
