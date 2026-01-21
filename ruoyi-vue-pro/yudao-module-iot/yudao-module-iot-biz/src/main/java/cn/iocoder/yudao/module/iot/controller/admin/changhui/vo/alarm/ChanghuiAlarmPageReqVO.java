package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 长辉设备报警分页查询 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备报警分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ChanghuiAlarmPageReqVO extends PageParam {

    @Schema(description = "测站编码", example = "1234567890")
    private String stationCode;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "报警类型", example = "OVER_TORQUE")
    private String alarmType;

    @Schema(description = "状态：0-未确认,1-已确认", example = "0")
    private Integer status;

    @Schema(description = "开始时间", example = "2024-01-01 00:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-01-31 23:59:59")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
