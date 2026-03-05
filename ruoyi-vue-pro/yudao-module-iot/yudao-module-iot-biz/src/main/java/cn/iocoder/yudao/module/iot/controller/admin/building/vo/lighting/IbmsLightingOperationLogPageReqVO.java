package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 照明操作日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsLightingOperationLogPageReqVO extends PageParam {

    @Schema(description = "操作类型 1-开启 2-关闭 3-调光 4-场景切换 5-定时执行")
    private Integer operationType;

    @Schema(description = "操作对象类型 1-回路 2-场景 3-区域")
    private Integer targetType;

    @Schema(description = "操作对象名称")
    private String targetName;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
