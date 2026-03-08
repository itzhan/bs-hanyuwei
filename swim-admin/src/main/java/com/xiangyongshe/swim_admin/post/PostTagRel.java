package com.xiangyongshe.swim_admin.post;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("post_tag_rel")
public class PostTagRel {
    @TableId
    private Long id;
    private Long postId;
    private Long tagId;
}
