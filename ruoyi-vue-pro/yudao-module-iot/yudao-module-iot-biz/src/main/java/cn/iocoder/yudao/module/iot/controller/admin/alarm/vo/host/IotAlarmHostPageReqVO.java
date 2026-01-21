package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 报警主机分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmHostPageReqVO extends PageParam {

    @Schema(description = "主机名称", example = "1号楼报警主机")
    private String hostName;

    @Schema(description = "关联设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "在线状态：0-离线, 1-在线", example = "1")
    private Integer onlineStatus;

    @Schema(description = "布防状态：DISARM-撤防, ARM_ALL-全部布防, ARM_EMERGENCY-紧急布防", example = "DISARM")
    private String armStatus;

    @Schema(description = "报警状态：0-正常, 1-报警中", example = "0")
    private Integer alarmStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
