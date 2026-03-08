package com.xiangyongshe.swim_admin.comment;

import java.util.List;
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
import com.xiangyongshe.swim_admin.comment.dto.CommentStatusRequest;
import com.xiangyongshe.swim_admin.comment.dto.CommentVO;
import com.xiangyongshe.swim_admin.common.constant.AuditAction;
import com.xiangyongshe.swim_admin.common.constant.CommentStatus;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.common.response.PageResult;
import com.xiangyongshe.swim_admin.report.ContentAudit;
import com.xiangyongshe.swim_admin.report.ContentAuditService;
import com.xiangyongshe.swim_admin.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/comments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {
    private final PostCommentService postCommentService;
    private final ContentAuditService contentAuditService;

    public AdminCommentController(PostCommentService postCommentService, ContentAuditService contentAuditService) {
        this.postCommentService = postCommentService;
        this.contentAuditService = contentAuditService;
    }

    @GetMapping
    public ApiResponse<PageResult<CommentVO>> page(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) Long postId,
                                                   @RequestParam(required = false) Long userId,
                                                   @RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getDeleted, 0)
                .orderByDesc(PostComment::getCreatedAt);
        if (postId != null) {
            wrapper.eq(PostComment::getPostId, postId);
        }
        if (userId != null) {
            wrapper.eq(PostComment::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(PostComment::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(PostComment::getContent, keyword);
        }
        Page<PostComment> result = postCommentService.page(new Page<>(page, size), wrapper);
        List<CommentVO> records = result.getRecords().stream()
                .map(c -> new CommentVO(c.getId(), c.getPostId(), c.getUserId(), c.getParentId(), c.getReplyToUserId(),
                        c.getContent(), c.getStatus(), c.getCreatedAt()))
                .collect(Collectors.toList());
        return ApiResponse.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody CommentStatusRequest request) {
        PostComment comment = postCommentService.getById(id);
        if (comment == null) {
            throw new BizException(404, "评论不存在");
        }
        comment.setStatus(request.getStatus());
        postCommentService.updateById(comment);
        ContentAudit audit = new ContentAudit();
        audit.setOperatorId(SecurityUtils.getUserId());
        audit.setCommentId(id);
        audit.setAction(mapAction(request.getStatus()));
        audit.setReason(request.getReason());
        contentAuditService.save(audit);
        return ApiResponse.ok();
    }

    private Integer mapAction(Integer status) {
        if (status == null) {
            return AuditAction.APPROVE.getValue();
        }
        if (status.equals(CommentStatus.PUBLISHED.getValue())) {
            return AuditAction.APPROVE.getValue();
        }
        if (status.equals(CommentStatus.HIDDEN.getValue())) {
            return AuditAction.HIDE.getValue();
        }
        if (status.equals(CommentStatus.DELETED.getValue())) {
            return AuditAction.BLOCK.getValue();
        }
        return AuditAction.APPROVE.getValue();
    }
}
