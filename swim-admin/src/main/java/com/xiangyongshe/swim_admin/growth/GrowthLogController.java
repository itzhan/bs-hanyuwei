package com.xiangyongshe.swim_admin.growth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyongshe.swim_admin.baby.BabyProfile;
import com.xiangyongshe.swim_admin.baby.BabyProfileService;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.common.response.PageResult;
import com.xiangyongshe.swim_admin.growth.dto.GrowthLogCreateRequest;
import com.xiangyongshe.swim_admin.growth.dto.GrowthLogUpdateRequest;
import com.xiangyongshe.swim_admin.growth.dto.GrowthLogVO;
import com.xiangyongshe.swim_admin.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/growth-logs")
public class GrowthLogController {
    private final GrowthLogService growthLogService;
    private final BabyProfileService babyProfileService;

    public GrowthLogController(GrowthLogService growthLogService, BabyProfileService babyProfileService) {
        this.growthLogService = growthLogService;
        this.babyProfileService = babyProfileService;
    }

    @PostMapping
    public ApiResponse<GrowthLogVO> create(@Valid @RequestBody GrowthLogCreateRequest request) {
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
        GrowthLog log = new GrowthLog();
        log.setUserId(userId);
        log.setBabyId(request.getBabyId());
        log.setLogType(request.getLogType());
        log.setTitle(request.getTitle());
        log.setContent(request.getContent());
        log.setLogTime(request.getLogTime());
        growthLogService.save(log);
        return ApiResponse.ok(toVO(log));
    }

    @PutMapping("/{id}")
    public ApiResponse<GrowthLogVO> update(@PathVariable Long id, @RequestBody GrowthLogUpdateRequest request) {
        GrowthLog log = growthLogService.getById(id);
        if (log == null) {
            throw new BizException(404, "日志不存在");
        }
        if (!SecurityUtils.isAdmin() && !log.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        if (request.getLogType() != null) {
            log.setLogType(request.getLogType());
        }
        if (request.getTitle() != null) {
            log.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            log.setContent(request.getContent());
        }
        if (request.getLogTime() != null) {
            log.setLogTime(request.getLogTime());
        }
        growthLogService.updateById(log);
        return ApiResponse.ok(toVO(log));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        GrowthLog log = growthLogService.getById(id);
        if (log == null) {
            throw new BizException(404, "日志不存在");
        }
        if (!SecurityUtils.isAdmin() && !log.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        growthLogService.removeById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<GrowthLogVO> detail(@PathVariable Long id) {
        GrowthLog log = growthLogService.getById(id);
        if (log == null) {
            throw new BizException(404, "日志不存在");
        }
        if (!SecurityUtils.isAdmin() && !log.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        return ApiResponse.ok(toVO(log));
    }

    @GetMapping
    public ApiResponse<PageResult<GrowthLogVO>> page(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "10") long size,
                                                     @RequestParam(required = false) Long babyId,
                                                     @RequestParam(required = false) Integer logType,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                     @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<GrowthLog> wrapper = new LambdaQueryWrapper<GrowthLog>()
                .eq(GrowthLog::getDeleted, 0)
                .orderByDesc(GrowthLog::getLogTime);
        if (SecurityUtils.isAdmin()) {
            if (userId != null) {
                wrapper.eq(GrowthLog::getUserId, userId);
            }
        } else {
            Long currentUserId = SecurityUtils.getUserId();
            if (currentUserId == null) {
                throw new BizException(401, "未登录");
            }
            wrapper.eq(GrowthLog::getUserId, currentUserId);
        }
        if (babyId != null) {
            wrapper.eq(GrowthLog::getBabyId, babyId);
        }
        if (logType != null) {
            wrapper.eq(GrowthLog::getLogType, logType);
        }
        if (startTime != null) {
            wrapper.ge(GrowthLog::getLogTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(GrowthLog::getLogTime, endTime);
        }
        Page<GrowthLog> result = growthLogService.page(new Page<>(page, size), wrapper);
        List<GrowthLogVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return ApiResponse.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    private GrowthLogVO toVO(GrowthLog log) {
        return new GrowthLogVO(log.getId(), log.getUserId(), log.getBabyId(), log.getLogType(), log.getTitle(),
                log.getContent(), log.getLogTime(), log.getCreatedAt());
    }
}
