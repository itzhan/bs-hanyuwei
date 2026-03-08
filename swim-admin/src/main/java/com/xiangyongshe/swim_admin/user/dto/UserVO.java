package com.xiangyongshe.swim_admin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserVO {
    private Long id;
    private String username;
    private Integer role;
    private Integer status;
    private String displayName;
    private String avatarPath;
    private LocalDateTime createdAt;
}
