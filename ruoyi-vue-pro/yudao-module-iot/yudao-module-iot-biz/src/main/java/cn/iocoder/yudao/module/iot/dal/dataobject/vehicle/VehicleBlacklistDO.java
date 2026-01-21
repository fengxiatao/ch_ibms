package cn.iocoder.yudao.module.iot.dal.dataobject.vehicle;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName(value = "iot_vehicle_blacklist", autoResultMap = true)
@KeySequence("iot_vehicle_blacklist_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleBlacklistDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String plateNumber;
    private String blackReason;
    private Integer status;
    private String remark;
}


























