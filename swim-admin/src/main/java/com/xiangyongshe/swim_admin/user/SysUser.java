package com.xiangyongshe.swim_admin.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyongshe.swim_admin.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private Integer role;
    private Integer status;
    private String displayName;
    private String avatarPath;
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;
}
