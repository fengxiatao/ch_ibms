package cn.iocoder.yudao.module.iot.controller.admin.device.vo.status;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备状态响应 VO
 * 
 * <p>用于返回设备的在线状态信息</p>
 * 
 * <p>Requirements: 5.1 - 返回设备的当前状态和最后活跃时间</p>
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 设备状态 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatusRespVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceId;

    @Schema(description = "设备名称", example = "门禁控制器-A栋")
    private String deviceName;

    @Schema(description = "设备状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer state;

    @Schema(description = "设备状态名称", example = "在线")
    private String stateName;

    @Schema(description = "连接模式", example = "ACTIVE")
    private ConnectionMode connectionMode;

    @Schema(description = "最后活跃时间戳（毫秒）", example = "1703145600000")
    private Long lastSeenTimestamp;

    @Schema(description = "最后上线时间")
    private LocalDateTime onlineTime;

    @Schema(description = "最后离线时间")
    private LocalDateTime offlineTime;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "产品编号", example = "2048")
    private Long productId;

}
