package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.operationlog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报警主机操作记录 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 报警主机操作记录 Response VO")
@Data
public class IotAlarmOperationLogRespVO {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "报警主机ID", example = "1")
    private Long hostId;

    @Schema(description = "报警主机名称", example = "主机1")
    private String hostName;

    @Schema(description = "分区ID", example = "1")
    private Long partitionId;

    @Schema(description = "分区名称", example = "分区1")
    private String partitionName;

    @Schema(description = "防区ID", example = "1")
    private Long zoneId;

    @Schema(description = "防区名称", example = "防区1")
    private String zoneName;

    @Schema(description = "操作类型", example = "ARM_ALL")
    private String operationType;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "张三")
    private String operatorName;

    @Schema(description = "操作结果", example = "SUCCESS")
    private String result;

    @Schema(description = "错误信息", example = "操作失败")
    private String errorMessage;

    @Schema(description = "请求ID", example = "abc123")
    private String requestId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
