package com.xiangyongshe.swim_admin.baby;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyongshe.swim_admin.baby.dto.BabyCreateRequest;
import com.xiangyongshe.swim_admin.baby.dto.BabyUpdateRequest;
import com.xiangyongshe.swim_admin.baby.dto.BabyVO;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.common.response.PageResult;
import com.xiangyongshe.swim_admin.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/babies")
public class BabyController {
    private final BabyProfileService babyProfileService;

    public BabyController(BabyProfileService babyProfileService) {
        this.babyProfileService = babyProfileService;
    }

    @PostMapping
    public ApiResponse<BabyVO> create(@Valid @RequestBody BabyCreateRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        BabyProfile baby = new BabyProfile();
        baby.setUserId(userId);
        baby.setName(request.getName());
        baby.setGender(request.getGender());
        baby.setBirthday(request.getBirthday());
        baby.setRelation(request.getRelation());
        baby.setAvatarPath(request.getAvatarPath());
        baby.setNote(request.getNote());
        babyProfileService.save(baby);
        return ApiResponse.ok(toVO(baby));
    }

    @PutMapping("/{id}")
    public ApiResponse<BabyVO> update(@PathVariable Long id, @RequestBody BabyUpdateRequest request) {
        BabyProfile baby = babyProfileService.getById(id);
        if (baby == null) {
            throw new BizException(404, "档案不存在");
        }
        if (!SecurityUtils.isAdmin() && !baby.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        if (request.getName() != null) {
            baby.setName(request.getName());
        }
        if (request.getGender() != null) {
            baby.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            baby.setBirthday(request.getBirthday());
        }
        if (request.getRelation() != null) {
            baby.setRelation(request.getRelation());
        }
        if (request.getAvatarPath() != null) {
            baby.setAvatarPath(request.getAvatarPath());
        }
        if (request.getNote() != null) {
            baby.setNote(request.getNote());
        }
        babyProfileService.updateById(baby);
        return ApiResponse.ok(toVO(baby));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        BabyProfile baby = babyProfileService.getById(id);
        if (baby == null) {
            throw new BizException(404, "档案不存在");
        }
        if (!SecurityUtils.isAdmin() && !baby.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        babyProfileService.removeById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<BabyVO> detail(@PathVariable Long id) {
        BabyProfile baby = babyProfileService.getById(id);
        if (baby == null) {
            throw new BizException(404, "档案不存在");
        }
        if (!SecurityUtils.isAdmin() && !baby.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BizException(403, "无权限");
        }
        return ApiResponse.ok(toVO(baby));
    }

    @GetMapping
    public ApiResponse<PageResult<BabyVO>> page(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<BabyProfile> wrapper = new LambdaQueryWrapper<BabyProfile>()
                .eq(BabyProfile::getDeleted, 0)
                .orderByDesc(BabyProfile::getCreatedAt);
        if (SecurityUtils.isAdmin()) {
            if (userId != null) {
                wrapper.eq(BabyProfile::getUserId, userId);
            }
        } else {
            Long currentUserId = SecurityUtils.getUserId();
            if (currentUserId == null) {
                throw new BizException(401, "未登录");
            }
            wrapper.eq(BabyProfile::getUserId, currentUserId);
        }
        Page<BabyProfile> result = babyProfileService.page(new Page<>(page, size), wrapper);
        List<BabyVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return ApiResponse.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    private BabyVO toVO(BabyProfile baby) {
        return new BabyVO(baby.getId(), baby.getUserId(), baby.getName(), baby.getGender(), baby.getBirthday(),
                baby.getRelation(), baby.getAvatarPath(), baby.getNote(), baby.getCreatedAt());
    }
}
