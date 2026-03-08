package com.xiangyongshe.swim_admin.baby.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BabyCreateRequest {
    @NotBlank
    private String name;
    private Integer gender;
    private LocalDate birthday;
    private String relation;
    private String avatarPath;
    private String note;
}
