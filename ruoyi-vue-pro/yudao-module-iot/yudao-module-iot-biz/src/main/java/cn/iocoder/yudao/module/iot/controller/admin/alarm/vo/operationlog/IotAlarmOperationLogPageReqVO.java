package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.operationlog;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 报警主机操作记录分页查询 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 报警主机操作记录分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmOperationLogPageReqVO extends PageParam {

    @Schema(description = "报警主机ID", example = "1")
    private Long hostId;

    @Schema(description = "分区ID", example = "1")
    private Long partitionId;

    @Schema(description = "防区ID", example = "1")
    private Long zoneId;

    @Schema(description = "操作类型", example = "ARM_ALL")
    private String operationType;

    @Schema(description = "操作时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] operationTime;

}
