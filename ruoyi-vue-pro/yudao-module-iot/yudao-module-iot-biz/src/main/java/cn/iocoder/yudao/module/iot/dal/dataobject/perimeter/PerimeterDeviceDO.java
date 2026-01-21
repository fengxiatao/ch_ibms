package cn.iocoder.yudao.module.iot.dal.dataobject.perimeter;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName(value = "iot_perimeter_device", autoResultMap = true)
@KeySequence("iot_perimeter_device_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerimeterDeviceDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long zoneId;
    private Long deviceId;
    private String deviceName;
    private Integer deviceType;
    private Double longitude;
    private Double latitude;
    private Integer status;
}


























