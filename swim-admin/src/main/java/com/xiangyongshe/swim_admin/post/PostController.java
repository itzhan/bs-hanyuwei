package com.xiangyongshe.swim_admin.post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyongshe.swim_admin.common.constant.PostStatus;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.common.response.PageResult;
import com.xiangyongshe.swim_admin.post.dto.PostCreateRequest;
import com.xiangyongshe.swim_admin.post.dto.PostUpdateRequest;
import com.xiangyongshe.swim_admin.post.dto.PostVO;
import com.xiangyongshe.swim_admin.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final PostTagRelService postTagRelService;

    public PostController(PostService postService, PostTagRelService postTagRelService) {
        this.postService = postService;
        this.postTagRelService = postTagRelService;
    }

    @PostMapping
    public ApiResponse<PostVO> create(@Valid @RequestBody PostCreateRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setCategoryId(request.getCategoryId());
        post.setTopicId(request.getTopicId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setStatus(PostStatus.PENDING.getValue());
        post.setCommentCount(0);
        postService.save(post);
        saveTags(post.getId(), request.getTagIds());
        return ApiResponse.ok(toVO(post, request.getTagIds()));
    }

    @PutMapping("/{id}")
    public ApiResponse<PostVO> update(@PathVariable Long id, @RequestBody PostUpdateRequest request) {
        Post post = postService.getById(id);
        if (post == null) {
            throw new BizException(404, "帖子不存在");
        }
        if (!SecurityUtils.isAdmin() && !post.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        if (request.getCategoryId() != null) {
            post.setCategoryId(request.getCategoryId());
        }
        if (request.getTopicId() != null) {
            post.setTopicId(request.getTopicId());
        }
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (!SecurityUtils.isAdmin()) {
            post.setStatus(PostStatus.PENDING.getValue());
        }
        postService.updateById(post);
        if (request.getTagIds() != null) {
            saveTags(post.getId(), request.getTagIds());
        }
        List<Long> tagIds = request.getTagIds();
        if (tagIds == null) {
            tagIds = getTagIds(post.getId());
        }
        return ApiResponse.ok(toVO(post, tagIds));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            throw new BizException(404, "帖子不存在");
        }
        if (!SecurityUtils.isAdmin() && !post.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        postService.removeById(id);
        postTagRelService.remove(new LambdaQueryWrapper<PostTagRel>().eq(PostTagRel::getPostId, id));
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<PostVO> detail(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            throw new BizException(404, "帖子不存在");
        }
        if (!SecurityUtils.isAdmin()) {
            Long userId = SecurityUtils.getUserId();
            boolean isOwner = userId != null && userId.equals(post.getUserId());
            if (!isOwner && !Objects.equals(post.getStatus(), PostStatus.PUBLISHED.getValue())) {
                throw new BizException(403, "无权限");
            }
        }
        return ApiResponse.ok(toVO(post, getTagIds(post.getId())));
    }

    @GetMapping
    public ApiResponse<PageResult<PostVO>> page(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) Long topicId,
                                                @RequestParam(required = false) Long tagId,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "false") boolean mine) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getDeleted, 0)
                .orderByDesc(Post::getCreatedAt);
        if (categoryId != null) {
            wrapper.eq(Post::getCategoryId, categoryId);
        }
        if (topicId != null) {
            wrapper.eq(Post::getTopicId, topicId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        if (mine) {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                throw new BizException(401, "未登录");
            }
            wrapper.eq(Post::getUserId, userId);
        } else {
            wrapper.eq(Post::getStatus, PostStatus.PUBLISHED.getValue());
        }
        if (tagId != null) {
            wrapper.inSql(Post::getId, "select post_id from post_tag_rel where tag_id = " + tagId);
        }
        Page<Post> result = postService.page(new Page<>(page, size), wrapper);
        List<Post> posts = result.getRecords();
        Map<Long, List<Long>> tagMap = getTagMap(posts);
        List<PostVO> records = posts.stream()
                .map(p -> toVO(p, tagMap.getOrDefault(p.getId(), List.of())))
                .collect(Collectors.toList());
        return ApiResponse.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    private void saveTags(Long postId, List<Long> tagIds) {
        postTagRelService.remove(new LambdaQueryWrapper<PostTagRel>().eq(PostTagRel::getPostId, postId));
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<PostTagRel> rels = tagIds.stream()
                .distinct()
                .map(tagId -> {
                    PostTagRel rel = new PostTagRel();
                    rel.setPostId(postId);
                    rel.setTagId(tagId);
                    return rel;
                })
                .collect(Collectors.toList());
        postTagRelService.saveBatch(rels);
    }

    private List<Long> getTagIds(Long postId) {
        List<PostTagRel> rels = postTagRelService.list(new LambdaQueryWrapper<PostTagRel>().eq(PostTagRel::getPostId, postId));
        return rels.stream().map(PostTagRel::getTagId).collect(Collectors.toList());
    }

    private Map<Long, List<Long>> getTagMap(List<Post> posts) {
        if (posts.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        List<PostTagRel> rels = postTagRelService.list(new LambdaQueryWrapper<PostTagRel>().in(PostTagRel::getPostId, postIds));
        Map<Long, List<Long>> map = new HashMap<>();
        for (PostTagRel rel : rels) {
            map.computeIfAbsent(rel.getPostId(), k -> new ArrayList<>()).add(rel.getTagId());
        }
        return map;
    }

    private PostVO toVO(Post post, List<Long> tagIds) {
        return new PostVO(post.getId(), post.getUserId(), post.getCategoryId(), post.getTopicId(),
                post.getTitle(), post.getContent(), post.getStatus(), post.getCommentCount(), post.getCreatedAt(), tagIds);
    }
}
