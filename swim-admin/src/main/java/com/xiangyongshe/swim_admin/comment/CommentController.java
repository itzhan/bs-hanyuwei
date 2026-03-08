package com.xiangyongshe.swim_admin.comment;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyongshe.swim_admin.comment.dto.CommentCreateRequest;
import com.xiangyongshe.swim_admin.comment.dto.CommentVO;
import com.xiangyongshe.swim_admin.common.constant.CommentStatus;
import com.xiangyongshe.swim_admin.common.constant.PostStatus;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.common.response.PageResult;
import com.xiangyongshe.swim_admin.post.Post;
import com.xiangyongshe.swim_admin.post.PostService;
import com.xiangyongshe.swim_admin.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CommentController {
    private final PostCommentService postCommentService;
    private final PostService postService;

    public CommentController(PostCommentService postCommentService, PostService postService) {
        this.postCommentService = postCommentService;
        this.postService = postService;
    }

    @PostMapping("/posts/{postId}/comments")
    @Transactional
    public ApiResponse<CommentVO> create(@PathVariable Long postId, @Valid @RequestBody CommentCreateRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BizException(404, "帖子不存在");
        }
        if (!SecurityUtils.isAdmin() && (post.getStatus() == null || post.getStatus() != PostStatus.PUBLISHED.getValue())) {
            throw new BizException(403, "帖子未发布");
        }
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setContent(request.getContent());
        comment.setStatus(CommentStatus.PUBLISHED.getValue());
        postCommentService.save(comment);
        post.setCommentCount(post.getCommentCount() == null ? 1 : post.getCommentCount() + 1);
        postService.updateById(post);
        return ApiResponse.ok(toVO(comment));
    }

    @DeleteMapping("/comments/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        PostComment comment = postCommentService.getById(id);
        if (comment == null) {
            throw new BizException(404, "评论不存在");
        }
        if (!SecurityUtils.isAdmin() && !comment.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        postCommentService.removeById(id);
        Post post = postService.getById(comment.getPostId());
        if (post != null && post.getCommentCount() != null && post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postService.updateById(post);
        }
        return ApiResponse.ok();
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<PageResult<CommentVO>> page(@PathVariable Long postId,
                                                   @RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getDeleted, 0)
                .eq(PostComment::getPostId, postId)
                .orderByAsc(PostComment::getCreatedAt);
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(PostComment::getStatus, CommentStatus.PUBLISHED.getValue());
        }
        Page<PostComment> result = postCommentService.page(new Page<>(page, size), wrapper);
        List<CommentVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return ApiResponse.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    private CommentVO toVO(PostComment comment) {
        return new CommentVO(comment.getId(), comment.getPostId(), comment.getUserId(), comment.getParentId(),
                comment.getReplyToUserId(), comment.getContent(), comment.getStatus(), comment.getCreatedAt());
    }
}
