package cn.iocoder.yudao.module.iot.controller.admin.access.vo.operationlog;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 门禁操作日志分页 Request VO
 * 
 * 支持两类操作日志查询：
 * 1. 门控操作日志：开门、关门、常开、常闭等
 * 2. 授权操作日志：授权下发、授权撤销等（Requirements: 12.4）
 */
@Schema(description = "管理后台 - 门禁操作日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotAccessOperationLogPageReqVO extends PageParam {

    @Schema(description = "操作类型", example = "open_door")
    private String operationType;

    @Schema(description = "设备ID", example = "110")
    private Long deviceId;

    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作结果（0失败 1成功）", example = "1")
    private Integer result;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;
    
    // ========== 授权操作日志扩展筛选字段（Requirements: 12.4） ==========
    
    @Schema(description = "目标人员ID（授权操作）", example = "1001")
    private Long targetPersonId;
    
    @Schema(description = "授权任务ID", example = "2001")
    private Long authTaskId;
    
    @Schema(description = "权限组ID", example = "3001")
    private Long permissionGroupId;

}
