package com.xiangyongshe.swim_admin.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
    private final PostTopicService postTopicService;

    public TopicController(PostTopicService postTopicService) {
        this.postTopicService = postTopicService;
    }

    @GetMapping
    public ApiResponse<List<PostTopic>> list() {
        List<PostTopic> list = postTopicService.list(new LambdaQueryWrapper<PostTopic>()
                .eq(PostTopic::getDeleted, 0)
                .eq(PostTopic::getStatus, 1)
                .orderByDesc(PostTopic::getCreatedAt));
        return ApiResponse.ok(list);
    }
}
