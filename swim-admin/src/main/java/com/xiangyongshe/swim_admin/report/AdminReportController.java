package com.xiangyongshe.swim_admin.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyongshe.swim_admin.comment.PostComment;
import com.xiangyongshe.swim_admin.comment.PostCommentService;
import com.xiangyongshe.swim_admin.common.constant.AuditAction;
import com.xiangyongshe.swim_admin.common.constant.CommentStatus;
import com.xiangyongshe.swim_admin.common.constant.HandleResult;
import com.xiangyongshe.swim_admin.common.constant.PostStatus;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.common.response.PageResult;
import com.xiangyongshe.swim_admin.post.Post;
import com.xiangyongshe.swim_admin.post.PostService;
import com.xiangyongshe.swim_admin.report.dto.ReportHandleRequest;
import com.xiangyongshe.swim_admin.report.dto.ReportVO;
import com.xiangyongshe.swim_admin.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {
    private final ContentReportService contentReportService;
    private final ContentAuditService contentAuditService;
    private final PostService postService;
    private final PostCommentService postCommentService;

    public AdminReportController(ContentReportService contentReportService,
                                 ContentAuditService contentAuditService,
                                 PostService postService,
                                 PostCommentService postCommentService) {
        this.contentReportService = contentReportService;
        this.contentAuditService = contentAuditService;
        this.postService = postService;
        this.postCommentService = postCommentService;
    }

    @GetMapping
    public ApiResponse<PageResult<ReportVO>> page(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<ContentReport> wrapper = new LambdaQueryWrapper<ContentReport>()
                .eq(ContentReport::getDeleted, 0)
                .orderByDesc(ContentReport::getCreatedAt);
        if (status != null) {
            wrapper.eq(ContentReport::getStatus, status);
        }
        Page<ContentReport> result = contentReportService.page(new Page<>(page, size), wrapper);
        List<ReportVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return ApiResponse.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    @PatchMapping("/{id}/handle")
    public ApiResponse<Void> handle(@PathVariable Long id, @Valid @RequestBody ReportHandleRequest request) {
        ContentReport report = contentReportService.getById(id);
        if (report == null) {
            throw new BizException(404, "举报不存在");
        }
        report.setStatus(request.getStatus());
        report.setHandleResult(request.getHandleResult());
        report.setHandleNote(request.getHandleNote());
        report.setHandledBy(SecurityUtils.getUserId());
        report.setHandledAt(LocalDateTime.now());
        contentReportService.updateById(report);

        Integer action = null;
        if (report.getPostId() != null) {
            Post post = postService.getById(report.getPostId());
            if (post != null) {
                if (request.getHandleResult().equals(HandleResult.DELETE.getValue())) {
                    post.setStatus(PostStatus.HIDDEN.getValue());
                    action = AuditAction.HIDE.getValue();
                } else if (request.getHandleResult().equals(HandleResult.BLOCK.getValue())) {
                    post.setStatus(PostStatus.BLOCKED.getValue());
                    action = AuditAction.BLOCK.getValue();
                }
                if (action != null) {
                    postService.updateById(post);
                }
            }
        }
        if (report.getCommentId() != null) {
            PostComment comment = postCommentService.getById(report.getCommentId());
            if (comment != null) {
                if (request.getHandleResult().equals(HandleResult.DELETE.getValue())) {
                    comment.setStatus(CommentStatus.DELETED.getValue());
                    action = AuditAction.HIDE.getValue();
                } else if (request.getHandleResult().equals(HandleResult.BLOCK.getValue())) {
                    comment.setStatus(CommentStatus.HIDDEN.getValue());
                    action = AuditAction.BLOCK.getValue();
                }
                if (action != null) {
                    postCommentService.updateById(comment);
                }
            }
        }
        if (action != null) {
            ContentAudit audit = new ContentAudit();
            audit.setOperatorId(SecurityUtils.getUserId());
            audit.setPostId(report.getPostId());
            audit.setCommentId(report.getCommentId());
            audit.setReportId(report.getId());
            audit.setAction(action);
            audit.setReason(request.getHandleNote());
            contentAuditService.save(audit);
        }
        return ApiResponse.ok();
    }

    private ReportVO toVO(ContentReport report) {
        return new ReportVO(report.getId(), report.getReporterId(), report.getPostId(), report.getCommentId(),
                report.getReason(), report.getReasonDesc(), report.getStatus(), report.getHandledBy(), report.getHandledAt(),
                report.getHandleResult(), report.getHandleNote(), report.getCreatedAt());
    }
}
