package cn.iocoder.yudao.module.iot.dal.dataobject.perimeter;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName(value = "iot_perimeter_zone", autoResultMap = true)
@KeySequence("iot_perimeter_zone_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerimeterZoneDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String zoneName;
    private String zoneCode;
    private Integer zoneType;
    private String boundaryCoordinates;
    private Integer alarmLevel;
    private Integer status;
    private String remark;
}


























