package com.xiangyongshe.swim_admin.baby.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BabyVO {
    private Long id;
    private Long userId;
    private String name;
    private Integer gender;
    private LocalDate birthday;
    private String relation;
    private String avatarPath;
    private String note;
    private LocalDateTime createdAt;
}
