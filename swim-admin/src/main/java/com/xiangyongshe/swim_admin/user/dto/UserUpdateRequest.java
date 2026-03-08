package com.xiangyongshe.swim_admin.user.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String displayName;
    private Integer role;
    private Integer status;
    private String avatarPath;
}
