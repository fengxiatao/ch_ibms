package cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 设备统计 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 设备统计 Response VO")
@Data
public class DeviceStatisticsRespVO {

    @Schema(description = "设备总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long totalDevices;

    @Schema(description = "在线设备数", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Long onlineDevices;

    @Schema(description = "离线设备数", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Long offlineDevices;

    @Schema(description = "故障设备数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long faultDevices;

    @Schema(description = "在线率", requiredMode = Schema.RequiredMode.REQUIRED, example = "80.0")
    private Double onlineRate;

    @Schema(description = "各产品设备数量分布", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Long> devicesByProduct;

    @Schema(description = "各状态设备数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Long> devicesByStatus;

    @Schema(description = "今日新增设备数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long todayNewDevices;

    @Schema(description = "本周新增设备数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long weekNewDevices;
}












