package com.xiangyongshe.swim_admin.post.dto;

import lombok.Data;

@Data
public class TopicUpdateRequest {
    private String name;
    private String description;
    private Integer status;
}
