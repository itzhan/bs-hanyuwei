package com.xiangyongshe.swim_admin.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private Long id;
    private String url;
    private String fileName;
}
