package com.xiangyongshe.swim_admin.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequest {
    @NotBlank
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer status;
}
