package cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备趋势数据响应 VO
 * 
 * <p>Requirements: 3.4</p>
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 设备趋势数据响应 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTrendRespVO {

    @Schema(description = "时间点", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime timestamp;

    @Schema(description = "在线设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Integer onlineCount;

    @Schema(description = "离线设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Integer offlineCount;

    @Schema(description = "设备总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalCount;

}
