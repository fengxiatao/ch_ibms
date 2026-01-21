package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 长辉设备报警 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备报警 Response VO")
@Data
public class ChanghuiAlarmRespVO {

    @Schema(description = "报警ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    private String stationCode;

    @Schema(description = "报警类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "OVER_TORQUE")
    private String alarmType;

    @Schema(description = "报警类型名称", example = "过力矩")
    private String alarmTypeName;

    @Schema(description = "报警值", example = "120.5")
    private String alarmValue;

    @Schema(description = "报警时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01 12:00:00")
    private LocalDateTime alarmTime;

    @Schema(description = "状态：0-未确认,1-已确认", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "状态名称", example = "未确认")
    private String statusName;

    @Schema(description = "确认时间", example = "2024-01-01 13:00:00")
    private LocalDateTime ackTime;

    @Schema(description = "确认人", example = "admin")
    private String ackUser;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
