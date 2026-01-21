package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 长辉设备数据 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备数据 Response VO")
@Data
public class ChanghuiDataRespVO {

    @Schema(description = "数据ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    private String stationCode;

    @Schema(description = "指标类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "waterLevel")
    private String indicator;

    @Schema(description = "指标名称", example = "水位")
    private String indicatorName;

    @Schema(description = "数值", requiredMode = Schema.RequiredMode.REQUIRED, example = "12.5")
    private BigDecimal value;

    @Schema(description = "单位", example = "m")
    private String unit;

    @Schema(description = "采集时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01 12:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
