package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 报警主机创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmHostCreateReqVO extends IotAlarmHostBaseVO {
}
