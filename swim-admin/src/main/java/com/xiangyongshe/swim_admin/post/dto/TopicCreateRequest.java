package com.xiangyongshe.swim_admin.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicCreateRequest {
    @NotBlank
    private String name;
    private String description;
    private Integer status;
}
