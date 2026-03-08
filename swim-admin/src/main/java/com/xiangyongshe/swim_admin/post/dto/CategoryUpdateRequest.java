package com.xiangyongshe.swim_admin.post.dto;

import lombok.Data;

@Data
public class CategoryUpdateRequest {
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer status;
}
