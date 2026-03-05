package cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 楼宇自控系统日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsBacSystemLogPageReqVO extends PageParam {

    @Schema(description = "日志类型 1-远程控制 2-系统事件 3-设备状态变更")
    private Integer logType;

    @Schema(description = "设备类型 1-暖通 2-给排水")
    private Integer deviceType;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
