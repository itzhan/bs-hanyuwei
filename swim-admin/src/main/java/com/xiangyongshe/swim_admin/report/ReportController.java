package com.xiangyongshe.swim_admin.report;

import com.xiangyongshe.swim_admin.comment.PostCommentService;
import com.xiangyongshe.swim_admin.common.constant.ReportStatus;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.post.PostService;
import com.xiangyongshe.swim_admin.report.dto.ReportCreateRequest;
import com.xiangyongshe.swim_admin.report.dto.ReportVO;
import com.xiangyongshe.swim_admin.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ContentReportService contentReportService;
    private final PostService postService;
    private final PostCommentService postCommentService;

    public ReportController(ContentReportService contentReportService, PostService postService, PostCommentService postCommentService) {
        this.contentReportService = contentReportService;
        this.postService = postService;
        this.postCommentService = postCommentService;
    }

    @PostMapping
    public ApiResponse<ReportVO> create(@Valid @RequestBody ReportCreateRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        boolean hasPost = request.getPostId() != null;
        boolean hasComment = request.getCommentId() != null;
        if (hasPost == hasComment) {
            throw new BizException(400, "必须选择帖子或评论其一");
        }
        if (hasPost && postService.getById(request.getPostId()) == null) {
            throw new BizException(404, "帖子不存在");
        }
        if (hasComment && postCommentService.getById(request.getCommentId()) == null) {
            throw new BizException(404, "评论不存在");
        }
        ContentReport report = new ContentReport();
        report.setReporterId(userId);
        report.setPostId(request.getPostId());
        report.setCommentId(request.getCommentId());
        report.setReason(request.getReason());
        report.setReasonDesc(request.getReasonDesc());
        report.setStatus(ReportStatus.PENDING.getValue());
        contentReportService.save(report);
        return ApiResponse.ok(toVO(report));
    }

    private ReportVO toVO(ContentReport report) {
        return new ReportVO(report.getId(), report.getReporterId(), report.getPostId(), report.getCommentId(),
                report.getReason(), report.getReasonDesc(), report.getStatus(), report.getHandledBy(), report.getHandledAt(),
                report.getHandleResult(), report.getHandleNote(), report.getCreatedAt());
    }
}
