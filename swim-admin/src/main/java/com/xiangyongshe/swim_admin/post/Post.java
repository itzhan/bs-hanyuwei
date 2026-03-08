package com.xiangyongshe.swim_admin.post;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyongshe.swim_admin.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post")
public class Post extends BaseEntity {
    private Long userId;
    private Long categoryId;
    private Long topicId;
    private String title;
    private String content;
    private Integer status;
    private Integer commentCount;
}
