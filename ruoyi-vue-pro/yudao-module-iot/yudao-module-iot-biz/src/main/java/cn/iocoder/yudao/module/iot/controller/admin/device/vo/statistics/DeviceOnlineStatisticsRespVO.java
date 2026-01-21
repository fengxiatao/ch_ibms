package cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 设备在线统计响应 VO
 * 
 * <p>Requirements: 3.1, 3.2, 3.3</p>
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 设备在线统计响应 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceOnlineStatisticsRespVO {

    @Schema(description = "设备总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalCount;

    @Schema(description = "在线设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Integer onlineCount;

    @Schema(description = "离线设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Integer offlineCount;

    @Schema(description = "未激活设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer inactiveCount;

    @Schema(description = "离线率（百分比）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15.00")
    private BigDecimal offlineRate;

}
