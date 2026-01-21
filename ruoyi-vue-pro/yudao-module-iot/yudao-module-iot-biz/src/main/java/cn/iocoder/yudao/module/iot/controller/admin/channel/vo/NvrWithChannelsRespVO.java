package cn.iocoder.yudao.module.iot.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NVR设备及其通道响应 VO
 * 用于多屏预览功能
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - NVR设备及其通道响应 VO")
@Data
public class NvrWithChannelsRespVO {

    @Schema(description = "NVR设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deviceId;

    @Schema(description = "NVR设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1号楼NVR")
    private String deviceName;

    @Schema(description = "NVR设备编码", example = "NVR001")
    private String deviceCode;

    @Schema(description = "设备类型", example = "NVR")
    private String deviceType;

    @Schema(description = "设备IP", example = "192.168.1.100")
    private String deviceIp;

    @Schema(description = "设备端口", example = "8000")
    private Integer devicePort;

    @Schema(description = "在线状态（0:离线 1:在线）", example = "1")
    private Integer onlineStatus;

    @Schema(description = "通道总数", example = "16")
    private Integer totalChannels;

    @Schema(description = "在线通道数", example = "12")
    private Integer onlineChannels;

    @Schema(description = "通道列表")
    private List<IotDeviceChannelRespVO> channels;

    @Schema(description = "最后同步时间")
    private LocalDateTime lastSyncTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
