package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 长辉设备数据保存 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备数据保存 Request VO")
@Data
public class ChanghuiDataSaveReqVO {

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotBlank(message = "测站编码不能为空")
    private String stationCode;

    @Schema(description = "指标类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "waterLevel")
    @NotBlank(message = "指标类型不能为空")
    private String indicator;

    @Schema(description = "数值", requiredMode = Schema.RequiredMode.REQUIRED, example = "12.5")
    @NotNull(message = "数值不能为空")
    private BigDecimal value;

    @Schema(description = "采集时间", example = "2024-01-01 12:00:00")
    private LocalDateTime timestamp;

}
