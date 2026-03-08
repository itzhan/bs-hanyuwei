package com.xiangyongshe.swim_admin.post.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostVO {
    private Long id;
    private Long userId;
    private Long categoryId;
    private Long topicId;
    private String title;
    private String content;
    private Integer status;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private List<Long> tagIds;
}
