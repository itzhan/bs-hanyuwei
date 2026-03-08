package com.xiangyongshe.swim_admin.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {
    private Long id;
    private String username;
    private Integer role;
    private String displayName;
    private String avatarPath;
}
