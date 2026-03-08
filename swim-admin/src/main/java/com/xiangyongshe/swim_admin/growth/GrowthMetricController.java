package com.xiangyongshe.swim_admin.growth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyongshe.swim_admin.baby.BabyProfile;
import com.xiangyongshe.swim_admin.baby.BabyProfileService;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.common.response.PageResult;
import com.xiangyongshe.swim_admin.growth.dto.GrowthMetricCreateRequest;
import com.xiangyongshe.swim_admin.growth.dto.GrowthMetricUpdateRequest;
import com.xiangyongshe.swim_admin.growth.dto.GrowthMetricVO;
import com.xiangyongshe.swim_admin.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/growth-metrics")
public class GrowthMetricController {
    private final GrowthMetricService growthMetricService;
    private final BabyProfileService babyProfileService;

    public GrowthMetricController(GrowthMetricService growthMetricService, BabyProfileService babyProfileService) {
        this.growthMetricService = growthMetricService;
        this.babyProfileService = babyProfileService;
    }

    @PostMapping
    public ApiResponse<GrowthMetricVO> create(@Valid @RequestBody GrowthMetricCreateRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        BabyProfile baby = babyProfileService.getById(request.getBabyId());
        if (baby == null) {
            throw new BizException(404, "宝宝档案不存在");
        }
        if (!SecurityUtils.isAdmin() && !baby.getUserId().equals(userId)) {
            throw new BizException(403, "无权限");
        }
        GrowthMetric metric = new GrowthMetric();
        metric.setUserId(userId);
        metric.setBabyId(request.getBabyId());
        metric.setSourceLogId(request.getSourceLogId());
        metric.setMetricType(request.getMetricType());
        metric.setMetricValue(request.getMetricValue());
        metric.setUnit(request.getUnit());
        metric.setRecordedAt(request.getRecordedAt());
        metric.setNote(request.getNote());
        growthMetricService.save(metric);
        return ApiResponse.ok(toVO(metric));
    }

    @PutMapping("/{id}")
    public ApiResponse<GrowthMetricVO> update(@PathVariable Long id, @RequestBody GrowthMetricUpdateRequest request) {
        GrowthMetric metric = growthMetricService.getById(id);
        if (metric == null) {
            throw new BizException(404, "指标不存在");
        }
        if (!SecurityUtils.isAdmin() && !metric.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        if (request.getMetricType() != null) {
            metric.setMetricType(request.getMetricType());
        }
        if (request.getMetricValue() != null) {
            metric.setMetricValue(request.getMetricValue());
        }
        if (request.getUnit() != null) {
            metric.setUnit(request.getUnit());
        }
        if (request.getRecordedAt() != null) {
            metric.setRecordedAt(request.getRecordedAt());
        }
        if (request.getNote() != null) {
            metric.setNote(request.getNote());
        }
        growthMetricService.updateById(metric);
        return ApiResponse.ok(toVO(metric));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        GrowthMetric metric = growthMetricService.getById(id);
        if (metric == null) {
            throw new BizException(404, "指标不存在");
        }
        if (!SecurityUtils.isAdmin() && !metric.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        growthMetricService.removeById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<GrowthMetricVO> detail(@PathVariable Long id) {
        GrowthMetric metric = growthMetricService.getById(id);
        if (metric == null) {
            throw new BizException(404, "指标不存在");
        }
        if (!SecurityUtils.isAdmin() && !metric.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        return ApiResponse.ok(toVO(metric));
    }

    @GetMapping
    public ApiResponse<PageResult<GrowthMetricVO>> page(@RequestParam(defaultValue = "1") long page,
                                                        @RequestParam(defaultValue = "10") long size,
                                                        @RequestParam(required = false) Long babyId,
                                                        @RequestParam(required = false) Integer metricType,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                        @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<GrowthMetric> wrapper = new LambdaQueryWrapper<GrowthMetric>()
                .eq(GrowthMetric::getDeleted, 0)
                .orderByDesc(GrowthMetric::getRecordedAt);
        if (SecurityUtils.isAdmin()) {
            if (userId != null) {
                wrapper.eq(GrowthMetric::getUserId, userId);
            }
        } else {
            Long currentUserId = SecurityUtils.getUserId();
            if (currentUserId == null) {
                throw new BizException(401, "未登录");
            }
            wrapper.eq(GrowthMetric::getUserId, currentUserId);
        }
        if (babyId != null) {
            wrapper.eq(GrowthMetric::getBabyId, babyId);
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
        Page<GrowthMetric> result = growthMetricService.page(new Page<>(page, size), wrapper);
        List<GrowthMetricVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return ApiResponse.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    private GrowthMetricVO toVO(GrowthMetric metric) {
        return new GrowthMetricVO(metric.getId(), metric.getUserId(), metric.getBabyId(), metric.getSourceLogId(),
                metric.getMetricType(), metric.getMetricValue(), metric.getUnit(), metric.getRecordedAt(), metric.getNote(), metric.getCreatedAt());
    }
}
