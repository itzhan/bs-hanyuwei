package com.xiangyongshe.swim_admin.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.common.response.PageResult;
import com.xiangyongshe.swim_admin.user.dto.UserCreateRequest;
import com.xiangyongshe.swim_admin.user.dto.UserStatusRequest;
import com.xiangyongshe.swim_admin.user.dto.UserUpdateRequest;
import com.xiangyongshe.swim_admin.user.dto.UserVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(SysUserService sysUserService, PasswordEncoder passwordEncoder) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ApiResponse<PageResult<UserVO>> page(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer role,
                                                @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getDisplayName, keyword));
        }
        if (role != null) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        Page<SysUser> result = sysUserService.page(new Page<>(page, size), wrapper);
        List<UserVO> records = result.getRecords().stream()
                .map(u -> new UserVO(u.getId(), u.getUsername(), u.getRole(), u.getStatus(), u.getDisplayName(), u.getAvatarPath(), u.getCreatedAt()))
                .collect(Collectors.toList());
        return ApiResponse.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records));
    }

    @PostMapping
    public ApiResponse<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        SysUser exist = sysUserService.lambdaQuery().eq(SysUser::getUsername, request.getUsername()).eq(SysUser::getDeleted, 0).one();
        if (exist != null) {
            throw new BizException(400, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setDisplayName(request.getDisplayName());
        sysUserService.save(user);
        return ApiResponse.ok(new UserVO(user.getId(), user.getUsername(), user.getRole(), user.getStatus(), user.getDisplayName(), user.getAvatarPath(), user.getCreatedAt()));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserVO> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getAvatarPath() != null) {
            user.setAvatarPath(request.getAvatarPath());
        }
        sysUserService.updateById(user);
        return ApiResponse.ok(new UserVO(user.getId(), user.getUsername(), user.getRole(), user.getStatus(), user.getDisplayName(), user.getAvatarPath(), user.getCreatedAt()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setStatus(request.getStatus());
        sysUserService.updateById(user);
        return ApiResponse.ok();
    }
}
