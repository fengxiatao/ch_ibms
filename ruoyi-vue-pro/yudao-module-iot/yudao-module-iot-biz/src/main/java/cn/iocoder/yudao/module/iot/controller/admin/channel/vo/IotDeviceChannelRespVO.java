package cn.iocoder.yudao.module.iot.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * IoT 设备通道响应 Response VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - IoT 设备通道响应 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceChannelRespVO extends IotDeviceChannelBaseVO {

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long id;

    @Schema(description = "设备名称", example = "前台NVR")
    private String deviceName;

    @Schema(description = "目标设备名称", example = "前台全景相机")
    private String targetDeviceName;

    @Schema(description = "最后在线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "最后同步时间")
    private LocalDateTime lastSyncTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
