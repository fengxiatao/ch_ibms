package cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 按设备类型统计响应 VO
 * 
 * <p>Requirements: 3.2</p>
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 按设备类型统计响应 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTypeStatisticsRespVO {

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer deviceType;

    @Schema(description = "设备类型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "摄像头")
    private String deviceTypeName;

    @Schema(description = "设备总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer totalCount;

    @Schema(description = "在线设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    private Integer onlineCount;

    @Schema(description = "离线设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Integer offlineCount;

    @Schema(description = "未激活设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer inactiveCount;

    @Schema(description = "离线率（百分比）", requiredMode = Schema.RequiredMode.REQUIRED, example = "16.00")
    private BigDecimal offlineRate;

}
