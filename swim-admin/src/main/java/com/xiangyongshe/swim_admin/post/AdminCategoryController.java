package com.xiangyongshe.swim_admin.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.post.dto.CategoryCreateRequest;
import com.xiangyongshe.swim_admin.post.dto.CategoryUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
    private final PostCategoryService postCategoryService;

    public AdminCategoryController(PostCategoryService postCategoryService) {
        this.postCategoryService = postCategoryService;
    }

    @GetMapping
    public ApiResponse<List<PostCategory>> list() {
        List<PostCategory> list = postCategoryService.list(new LambdaQueryWrapper<PostCategory>()
                .eq(PostCategory::getDeleted, 0)
                .orderByAsc(PostCategory::getSortOrder));
        return ApiResponse.ok(list);
    }

    @PostMapping
    public ApiResponse<PostCategory> create(@Valid @RequestBody CategoryCreateRequest request) {
        PostCategory category = new PostCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        postCategoryService.save(category);
        return ApiResponse.ok(category);
    }

    @PutMapping("/{id}")
    public ApiResponse<PostCategory> update(@PathVariable Long id, @RequestBody CategoryUpdateRequest request) {
        PostCategory category = postCategoryService.getById(id);
        if (category == null) {
            throw new BizException(404, "分类不存在");
        }
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }
        postCategoryService.updateById(category);
        return ApiResponse.ok(category);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        postCategoryService.removeById(id);
        return ApiResponse.ok();
    }
}
