package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 大华回放参数 Response VO")
@Data
public class DahuaPlaybackParamsRespVO {

    @Schema(description = "WebSocket URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "ws://192.168.1.200:80/rtspoverwebsocket")
    private String wsURL;

    @Schema(description = "RTSP URL（包含文件路径）", requiredMode = Schema.RequiredMode.REQUIRED, example = "rtsp://192.168.1.200:80/mnt/sd/2025-11-25/001/dav/09/09.30.00-09.35.00[M][0@0][0].dav")
    private String rtspURL;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "密码", example = "admin123")
    private String password;

    @Schema(description = "目标地址", example = "192.168.1.200:80")
    private String target;

    @Schema(description = "录像文件路径", example = "/mnt/sd/2025-11-25/001/dav/09/09.30.00-09.35.00[M][0@0][0].dav")
    private String filePath;

    @Schema(description = "文件开始时间", example = "2025-11-25 09:30:00")
    private String startTime;

    @Schema(description = "文件结束时间", example = "2025-11-25 09:35:00")
    private String endTime;

    @Schema(description = "时间偏移量（秒）", example = "120")
    private Integer timeOffset;
}
