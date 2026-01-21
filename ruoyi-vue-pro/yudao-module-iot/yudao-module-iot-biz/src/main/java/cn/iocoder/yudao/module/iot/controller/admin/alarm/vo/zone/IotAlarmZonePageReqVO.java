package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 防区分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmZonePageReqVO extends PageParam {

    @Schema(description = "所属主机ID", example = "1")
    private Long hostId;

    @Schema(description = "防区名称", example = "1号防区")
    private String zoneName;

    @Schema(description = "防区类型", example = "DOOR")
    private String zoneType;

    @Schema(description = "防区状态：ARM-布防, DISARM-撤防, BYPASS-旁路", example = "DISARM")
    private String zoneStatus;

    @Schema(description = "在线状态：0-离线, 1-在线", example = "1")
    private Integer onlineStatus;

    @Schema(description = "是否重要防区", example = "true")
    private Boolean isImportant;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
