package cn.iocoder.yudao.module.iot.controller.admin.camera.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 摄像头设备信息 Response VO（供网关使用）
 * 
 * <p>用于网关启动时批量拉取设备信息
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 摄像头设备信息（供网关使用） Response VO")
@Data
public class IotCameraForGatewayRespVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "82")
    private Long deviceId;

    @Schema(description = "设备IP地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "192.168.1.200")
    private String ipAddress;

    @Schema(description = "ONVIF端口", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Integer onvifPort;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "密码", example = "admin123")
    private String password;

}
