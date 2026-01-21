package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition;

import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报警主机分区 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 报警主机分区 Response VO")
@Data
public class IotAlarmPartitionRespVO {

    @Schema(description = "分区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "报警主机ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "109")
    private Long hostId;

    @Schema(description = "分区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer partitionNo;

    @Schema(description = "分区名称", example = "一楼分区")
    private String partitionName;

    @Schema(description = "布防状态：0-撤防，1-布防", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "报警状态：0-正常, 1-报警中", example = "0")
    private Integer alarmStatus;

    @Schema(description = "分区描述", example = "一楼所有防区")
    private String description;

    @Schema(description = "防区数量", example = "8")
    private Integer zoneCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "分区下的防区列表")
    private List<IotAlarmZoneRespVO> zones;

}
