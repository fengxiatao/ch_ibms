package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 报警事件分页查询 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 报警事件分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmEventPageReqVO extends PageParam {

    @Schema(description = "报警主机ID", example = "1")
    private Long hostId;

    @Schema(description = "事件类型：ALARM-报警, ARM-布防, DISARM-撤防, FAULT-故障, BYPASS-旁路", example = "ALARM")
    private String eventType;

    @Schema(description = "事件级别：INFO-信息, WARNING-警告, ERROR-错误, CRITICAL-严重", example = "CRITICAL")
    private String eventLevel;

    @Schema(description = "是否已处理", example = "false")
    private Boolean isHandled;

    @Schema(description = "分区号", example = "1")
    private Integer areaNo;

    @Schema(description = "防区号", example = "5")
    private Integer zoneNo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
