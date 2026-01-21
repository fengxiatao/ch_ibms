package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host;

import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition.IotAlarmPartitionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 报警主机详细信息（含分区和防区） Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmHostWithDetailsRespVO extends IotAlarmHostRespVO {

    @Schema(description = "分区列表（每个分区包含其下的防区）")
    private List<IotAlarmPartitionRespVO> partitions;

    @Schema(description = "所有防区列表（扁平化）")
    private List<IotAlarmZoneRespVO> zones;
}
