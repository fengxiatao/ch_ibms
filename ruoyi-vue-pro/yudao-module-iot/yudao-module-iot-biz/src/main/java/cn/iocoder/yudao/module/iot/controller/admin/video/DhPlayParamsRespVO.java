package cn.iocoder.yudao.module.iot.controller.admin.video;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DhPlayParamsRespVO {

    @Schema(description = "WebSocket地址: ws://{ip}:{httpPort}/rtspoverwebsocket")
    private String wsURL;

    @Schema(description = "RTSP地址: rtsp://{ip}:{rtspPort}/cam/realmonitor?channel={1}&subtype={0}&proto=Private3")
    private String rtspURL;

    @Schema(description = "登录用户名")
    private String username;

    @Schema(description = "登录密码")
    private String password;

    @Schema(description = "目标设备 http 访问地址 ip:port，用于 RPC2/Login 代理")
    private String target;
}
