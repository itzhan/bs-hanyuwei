package com.xiangyongshe.swim_admin.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.post.dto.TagCreateRequest;
import com.xiangyongshe.swim_admin.post.dto.TagUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTagController {
    private final PostTagService postTagService;

    public AdminTagController(PostTagService postTagService) {
        this.postTagService = postTagService;
    }

    @GetMapping
    public ApiResponse<List<PostTag>> list() {
        List<PostTag> list = postTagService.list(new LambdaQueryWrapper<PostTag>()
                .eq(PostTag::getDeleted, 0)
                .orderByDesc(PostTag::getCreatedAt));
        return ApiResponse.ok(list);
    }

    @PostMapping
    public ApiResponse<PostTag> create(@Valid @RequestBody TagCreateRequest request) {
        PostTag tag = new PostTag();
        tag.setName(request.getName());
        postTagService.save(tag);
        return ApiResponse.ok(tag);
    }

    @PutMapping("/{id}")
    public ApiResponse<PostTag> update(@PathVariable Long id, @RequestBody TagUpdateRequest request) {
        PostTag tag = postTagService.getById(id);
        if (tag == null) {
            throw new BizException(404, "标签不存在");
        }
        if (request.getName() != null) {
            tag.setName(request.getName());
        }
        postTagService.updateById(tag);
        return ApiResponse.ok(tag);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        postTagService.removeById(id);
        return ApiResponse.ok();
    }
}
