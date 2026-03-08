package com.xiangyongshe.swim_admin.dict;

import com.xiangyongshe.swim_admin.common.constant.*;
import com.xiangyongshe.swim_admin.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dicts")
public class DictController {

    @GetMapping
    public ApiResponse<Map<String, Object>> all() {
        Map<String, Object> data = new HashMap<>();
        data.put("userRole", UserRole.options());
        data.put("userStatus", UserStatus.options());
        data.put("postStatus", PostStatus.options());
        data.put("commentStatus", CommentStatus.options());
        data.put("reportStatus", ReportStatus.options());
        data.put("auditAction", AuditAction.options());
        data.put("reportReason", ReportReason.options());
        data.put("handleResult", HandleResult.options());
        data.put("logType", LogType.options());
        data.put("metricType", MetricType.options());
        return ApiResponse.ok(data);
    }
}
