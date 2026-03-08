package com.xiangyongshe.swim_admin.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyongshe.swim_admin.auth.dto.LoginRequest;
import com.xiangyongshe.swim_admin.auth.dto.LoginResponse;
import com.xiangyongshe.swim_admin.auth.dto.RegisterRequest;
import com.xiangyongshe.swim_admin.auth.dto.UserInfo;
import com.xiangyongshe.swim_admin.common.constant.UserRole;
import com.xiangyongshe.swim_admin.common.constant.UserStatus;
import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.security.AuthUser;
import com.xiangyongshe.swim_admin.security.JwtTokenProvider;
import com.xiangyongshe.swim_admin.security.SecurityUtils;
import com.xiangyongshe.swim_admin.user.SysUser;
import com.xiangyongshe.swim_admin.user.SysUserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          SysUserService sysUserService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        SysUser user = sysUserService.getById(authUser.getId());
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        user.setLastLoginAt(LocalDateTime.now());
        sysUserService.updateById(user);
        String token = tokenProvider.generateToken(authUser);
        UserInfo info = new UserInfo(user.getId(), user.getUsername(), user.getRole(), user.getDisplayName(), user.getAvatarPath());
        return ApiResponse.ok(new LoginResponse(token, "Bearer", info));
    }

    @PostMapping("/register")
    public ApiResponse<UserInfo> register(@Valid @RequestBody RegisterRequest request) {
        SysUser exist = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0));
        if (exist != null) {
            throw new BizException(400, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.PARENT.getValue());
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setDisplayName(request.getDisplayName());
        sysUserService.save(user);
        UserInfo info = new UserInfo(user.getId(), user.getUsername(), user.getRole(), user.getDisplayName(), user.getAvatarPath());
        return ApiResponse.ok(info);
    }

    @GetMapping("/me")
    public ApiResponse<UserInfo> me() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return ApiResponse.ok(new UserInfo(user.getId(), user.getUsername(), user.getRole(), user.getDisplayName(), user.getAvatarPath()));
    }
}
