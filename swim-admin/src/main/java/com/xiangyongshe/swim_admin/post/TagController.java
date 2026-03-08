package com.xiangyongshe.swim_admin.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final PostTagService postTagService;

    public TagController(PostTagService postTagService) {
        this.postTagService = postTagService;
    }

    @GetMapping
    public ApiResponse<List<PostTag>> list() {
        List<PostTag> list = postTagService.list(new LambdaQueryWrapper<PostTag>()
                .eq(PostTag::getDeleted, 0)
                .orderByDesc(PostTag::getCreatedAt));
        return ApiResponse.ok(list);
    }
}
