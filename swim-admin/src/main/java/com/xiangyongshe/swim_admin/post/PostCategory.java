package com.xiangyongshe.swim_admin.post;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyongshe.swim_admin.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post_category")
public class PostCategory extends BaseEntity {
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer status;
}
