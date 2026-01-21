package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 防区创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmZoneCreateReqVO extends IotAlarmZoneBaseVO {
}
