package com.xiangyongshe.swim_admin.post.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {
    private Long categoryId;
    private Long topicId;
    private String title;
    private String content;
    private List<Long> tagIds;
}
