package com.xiangyongshe.swim_admin.file;

import com.xiangyongshe.swim_admin.common.exception.BizException;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import com.xiangyongshe.swim_admin.file.dto.FileUploadResponse;
import com.xiangyongshe.swim_admin.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileObjectService fileObjectService;

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    public FileController(FileObjectService fileObjectService) {
        this.fileObjectService = fileObjectService;
    }

    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> upload(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(required = false) Integer bizType,
                                                  @RequestParam(required = false) Long bizId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "文件为空");
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf('.'));
            }
            String fileName = UUID.randomUUID() + ext;
            Path target = dir.resolve(fileName);
            Files.copy(file.getInputStream(), target);

            FileObject fileObject = new FileObject();
            fileObject.setOwnerId(userId);
            fileObject.setBizType(bizType == null ? 0 : bizType);
            fileObject.setBizId(bizId);
            fileObject.setOriginalName(originalName == null ? fileName : originalName);
            fileObject.setFileName(fileName);
            fileObject.setFilePath(target.toString());
            fileObject.setMimeType(file.getContentType());
            fileObject.setSize(file.getSize());
            fileObjectService.save(fileObject);

            String url = "/uploads/" + fileName;
            return ApiResponse.ok(new FileUploadResponse(fileObject.getId(), url, fileName));
        } catch (IOException e) {
            throw new BizException(500, "文件上传失败");
        }
    }
}
