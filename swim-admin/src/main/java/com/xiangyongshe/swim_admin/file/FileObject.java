package com.xiangyongshe.swim_admin.file;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_object")
public class FileObject {
    @TableId
    private Long id;
    private Long ownerId;
    private Integer bizType;
    private Long bizId;
    private String originalName;
    private String fileName;
    private String filePath;
    private String mimeType;
    private Long size;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
