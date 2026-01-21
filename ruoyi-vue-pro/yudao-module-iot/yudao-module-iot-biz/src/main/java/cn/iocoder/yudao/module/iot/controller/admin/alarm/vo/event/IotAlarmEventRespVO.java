package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报警事件 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 报警事件 Response VO")
@Data
public class IotAlarmEventRespVO {

    @Schema(description = "事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "报警主机ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long hostId;

    @Schema(description = "报警主机名称", example = "一号楼报警主机")
    private String hostName;

    @Schema(description = "事件码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1130")
    private String eventCode;

    @Schema(description = "事件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ALARM")
    private String eventType;

    @Schema(description = "事件级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "CRITICAL")
    private String eventLevel;

    @Schema(description = "分区号", example = "1")
    private Integer areaNo;

    @Schema(description = "防区号", example = "5")
    private Integer zoneNo;

    @Schema(description = "防区名称", example = "前门入侵探测器")
    private String zoneName;

    @Schema(description = "用户号", example = "1")
    private Integer userNo;

    @Schema(description = "序列号", example = "001")
    private String sequence;

    @Schema(description = "事件描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "防区报警")
    private String eventDesc;

    @Schema(description = "原始数据", example = "E1130001001001")
    private String rawData;

    @Schema(description = "是否新事件", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean isNewEvent;

    @Schema(description = "是否已处理", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean isHandled;

    @Schema(description = "处理人", example = "admin")
    private String handledBy;

    @Schema(description = "处理时间")
    private LocalDateTime handledTime;

    @Schema(description = "处理备注", example = "已确认并处理")
    private String handleRemark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
