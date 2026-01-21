package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 长辉设备报警保存 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备报警保存 Request VO")
@Data
public class ChanghuiAlarmSaveReqVO {

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotBlank(message = "测站编码不能为空")
    private String stationCode;

    @Schema(description = "报警类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "OVER_TORQUE")
    @NotBlank(message = "报警类型不能为空")
    private String alarmType;

    @Schema(description = "报警值", example = "120.5")
    private String alarmValue;

    @Schema(description = "报警时间", example = "2024-01-01 12:00:00")
    private LocalDateTime alarmTime;

}
