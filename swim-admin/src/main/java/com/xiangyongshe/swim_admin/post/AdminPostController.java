package com.xiangyongshe.swim_admin.post;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyongshe.swim_admin.common.constant.AuditAction;
import com.xiangyongshe.swim_admin.common.constant.PostStatus;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.common.response.PageResult;
import com.xiangyongshe.swim_admin.post.dto.PostStatusRequest;
import com.xiangyongshe.swim_admin.post.dto.PostVO;
import com.xiangyongshe.swim_admin.report.ContentAudit;
import com.xiangyongshe.swim_admin.report.ContentAuditService;
import com.xiangyongshe.swim_admin.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/posts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostController {
    private final PostService postService;
    private final PostTagRelService postTagRelService;
    private final ContentAuditService contentAuditService;

    public AdminPostController(PostService postService, PostTagRelService postTagRelService, ContentAuditService contentAuditService) {
        this.postService = postService;
        this.postTagRelService = postTagRelService;
        this.contentAuditService = contentAuditService;
    }

    @GetMapping
    public ApiResponse<PageResult<PostVO>> page(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) Long userId,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getDeleted, 0)
                .orderByDesc(Post::getCreatedAt);
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(Post::getUserId, userId);
        }
        if (categoryId != null) {
            wrapper.eq(Post::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        Page<Post> result = postService.page(new Page<>(page, size), wrapper);
        List<Post> posts = result.getRecords();
        Map<Long, List<Long>> tagMap = posts.isEmpty() ? Map.of() : postTagRelService.list(
                        new LambdaQueryWrapper<PostTagRel>().in(PostTagRel::getPostId, posts.stream().map(Post::getId).collect(Collectors.toList())))
                .stream().collect(Collectors.groupingBy(PostTagRel::getPostId, Collectors.mapping(PostTagRel::getTagId, Collectors.toList())));
        List<PostVO> records = posts.stream()
                .map(p -> new PostVO(p.getId(), p.getUserId(), p.getCategoryId(), p.getTopicId(), p.getTitle(), p.getContent(),
                        p.getStatus(), p.getCommentCount(), p.getCreatedAt(), tagMap.getOrDefault(p.getId(), List.of())))
                .collect(Collectors.toList());
        return ApiResponse.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PostStatusRequest request) {
        Post post = postService.getById(id);
        if (post == null) {
            throw new BizException(404, "帖子不存在");
        }
        post.setStatus(request.getStatus());
        postService.updateById(post);
        ContentAudit audit = new ContentAudit();
        audit.setOperatorId(SecurityUtils.getUserId());
        audit.setPostId(id);
        audit.setAction(mapAction(request.getStatus()));
        audit.setReason(request.getReason());
        contentAuditService.save(audit);
        return ApiResponse.ok();
    }

    private Integer mapAction(Integer status) {
        if (status == null) {
            return AuditAction.APPROVE.getValue();
        }
        if (status.equals(PostStatus.PUBLISHED.getValue())) {
            return AuditAction.APPROVE.getValue();
        }
        if (status.equals(PostStatus.HIDDEN.getValue())) {
            return AuditAction.HIDE.getValue();
        }
        if (status.equals(PostStatus.REJECTED.getValue())) {
            return AuditAction.REJECT.getValue();
        }
        if (status.equals(PostStatus.BLOCKED.getValue())) {
            return AuditAction.BLOCK.getValue();
        }
        return AuditAction.APPROVE.getValue();
    }
}
