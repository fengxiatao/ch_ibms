package cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权限组关联设备 Response VO
 */
@Schema(description = "管理后台 - 权限组关联设备 Response VO")
@Data
public class IotAccessPermissionGroupDeviceRespVO {

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备IP")
    private String deviceIp;

    @Schema(description = "通道ID")
    private Long channelId;

    @Schema(description = "通道编号")
    private Integer channelNo;

    @Schema(description = "通道名称")
    private String channelName;

}
