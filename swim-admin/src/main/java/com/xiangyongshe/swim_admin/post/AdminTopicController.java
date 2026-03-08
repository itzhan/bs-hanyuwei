package com.xiangyongshe.swim_admin.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.post.dto.TopicCreateRequest;
import com.xiangyongshe.swim_admin.post.dto.TopicUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/topics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTopicController {
    private final PostTopicService postTopicService;

    public AdminTopicController(PostTopicService postTopicService) {
        this.postTopicService = postTopicService;
    }

    @GetMapping
    public ApiResponse<List<PostTopic>> list() {
        List<PostTopic> list = postTopicService.list(new LambdaQueryWrapper<PostTopic>()
                .eq(PostTopic::getDeleted, 0)
                .orderByDesc(PostTopic::getCreatedAt));
        return ApiResponse.ok(list);
    }

    @PostMapping
    public ApiResponse<PostTopic> create(@Valid @RequestBody TopicCreateRequest request) {
        PostTopic topic = new PostTopic();
        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        topic.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        postTopicService.save(topic);
        return ApiResponse.ok(topic);
    }

    @PutMapping("/{id}")
    public ApiResponse<PostTopic> update(@PathVariable Long id, @RequestBody TopicUpdateRequest request) {
        PostTopic topic = postTopicService.getById(id);
        if (topic == null) {
            throw new BizException(404, "话题不存在");
        }
        if (request.getName() != null) {
            topic.setName(request.getName());
        }
        if (request.getDescription() != null) {
            topic.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            topic.setStatus(request.getStatus());
        }
        postTopicService.updateById(topic);
        return ApiResponse.ok(topic);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        postTopicService.removeById(id);
        return ApiResponse.ok();
    }
}
