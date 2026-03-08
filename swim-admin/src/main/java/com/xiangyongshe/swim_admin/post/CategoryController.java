package com.xiangyongshe.swim_admin.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final PostCategoryService postCategoryService;

    public CategoryController(PostCategoryService postCategoryService) {
        this.postCategoryService = postCategoryService;
    }

    @GetMapping
    public ApiResponse<List<PostCategory>> list() {
        List<PostCategory> list = postCategoryService.list(new LambdaQueryWrapper<PostCategory>()
                .eq(PostCategory::getDeleted, 0)
                .eq(PostCategory::getStatus, 1)
                .orderByAsc(PostCategory::getSortOrder));
        return ApiResponse.ok(list);
    }
}
