package com.xiangyongshe.swim_admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyongshe.swim_admin.common.constant.UserRole;
import com.xiangyongshe.swim_admin.common.constant.UserStatus;
import com.xiangyongshe.swim_admin.user.SysUser;
import com.xiangyongshe.swim_admin.user.SysUserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final SysUserService sysUserService;

    public CustomUserDetailsService(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        String role = user.getRole() == UserRole.ADMIN.getValue() ? "ROLE_ADMIN" : "ROLE_PARENT";
        boolean enabled = user.getStatus() == UserStatus.ACTIVE.getValue();
        return new AuthUser(user.getId(), user.getUsername(), user.getPassword(),
                List.of(new SimpleGrantedAuthority(role)), enabled);
    }
}
