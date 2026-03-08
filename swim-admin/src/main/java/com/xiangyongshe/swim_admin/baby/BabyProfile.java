package com.xiangyongshe.swim_admin.baby;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyongshe.swim_admin.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("baby_profile")
public class BabyProfile extends BaseEntity {
    private Long userId;
    private String name;
    private Integer gender;
    private LocalDate birthday;
    private String relation;
    private String avatarPath;
    private String note;
}
