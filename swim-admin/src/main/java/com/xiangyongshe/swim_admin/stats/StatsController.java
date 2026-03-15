package com.xiangyongshe.swim_admin.stats;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.growth.GrowthMetric;
import com.xiangyongshe.swim_admin.growth.GrowthMetricService;
import com.xiangyongshe.swim_admin.growth.dto.GrowthMetricVO;
import com.xiangyongshe.swim_admin.security.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final GrowthMetricService growthMetricService;
    private final StatsMapper statsMapper;

    public StatsController(GrowthMetricService growthMetricService, StatsMapper statsMapper) {
        this.growthMetricService = growthMetricService;
        this.statsMapper = statsMapper;
    }

    @GetMapping("/baby/{babyId}/metrics")
    public ApiResponse<List<GrowthMetricVO>> babyMetrics(@PathVariable Long babyId,
                                                         @RequestParam(required = false) Integer metricType,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        LambdaQueryWrapper<GrowthMetric> wrapper = new LambdaQueryWrapper<GrowthMetric>()
                .eq(GrowthMetric::getDeleted, 0)
                .eq(GrowthMetric::getBabyId, babyId)
                .orderByAsc(GrowthMetric::getRecordedAt);
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(GrowthMetric::getUserId, userId);
        }
        if (metricType != null) {
            wrapper.eq(GrowthMetric::getMetricType, metricType);
        }
        if (startTime != null) {
            wrapper.ge(GrowthMetric::getRecordedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(GrowthMetric::getRecordedAt, endTime);
        }
        List<GrowthMetricVO> records = growthMetricService.list(wrapper).stream()
                .map(m -> new GrowthMetricVO(m.getId(), m.getUserId(), m.getBabyId(), m.getSourceLogId(),
                        m.getMetricType(), m.getMetricValue(), m.getUnit(), m.getRecordedAt(), m.getNote(), m.getCreatedAt()))
                .collect(Collectors.toList());
        return ApiResponse.ok(records);
    }

    @GetMapping("/community/overview")
    public ApiResponse<Map<String, Object>> communityOverview(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> data = new HashMap<>();
        data.put("postDaily", statsMapper.postDailyCount(days));
        data.put("commentDaily", statsMapper.commentDailyCount(days));
        return ApiResponse.ok(data);
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", statsMapper.userCount());
        data.put("postCount", statsMapper.postCount());
        data.put("babyCount", statsMapper.babyCount());
        data.put("commentCount", statsMapper.commentCount());
        data.put("categoryStats", statsMapper.categoryPostStats());
        data.put("recentPosts", statsMapper.recentPosts());
        data.put("postDaily", statsMapper.postDailyCount(30));
        data.put("commentDaily", statsMapper.commentDailyCount(30));
        return ApiResponse.ok(data);
    }
}
