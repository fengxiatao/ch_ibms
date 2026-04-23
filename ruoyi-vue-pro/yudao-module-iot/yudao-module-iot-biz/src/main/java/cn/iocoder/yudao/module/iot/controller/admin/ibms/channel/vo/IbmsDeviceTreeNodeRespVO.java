package cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 设备 + 通道树节点（与前端 {@code DeviceTreeNode} 字段对齐）
 */
@Data
public class IbmsDeviceTreeNodeRespVO {

    @Schema(description = "节点 ID：设备 d-{id}，通道 c-{id}")
    private String id;

    @Schema(description = "展示名称")
    private String label;

    @Schema(description = "device / channel")
    private String type;

    @Schema(description = "所属设备 ID（通道节点也有）")
    private Long deviceId;

    @Schema(description = "通道 ID")
    private Long channelId;

    @Schema(description = "通道号")
    private Integer channelNo;

    @Schema(description = "设备类型码，如 NVR/CAM")
    private String deviceType;

    @Schema(description = "通道类型码，如 VT")
    private String channelType;

    @Schema(description = "在线状态：1 在线 0 离线（与旧 iot 通道列表一致）")
    private Integer onlineStatus;

    @Schema(description = "子节点")
    private List<IbmsDeviceTreeNodeRespVO> children;

    @Schema(description = "原始数据（设备或通道）")
    private Object raw;
}
