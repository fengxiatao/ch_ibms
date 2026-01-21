package cn.iocoder.yudao.module.iot.dal.dataobject.vehicle;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@TableName(value = "iot_vehicle_whitelist", autoResultMap = true)
@KeySequence("iot_vehicle_whitelist_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleWhitelistDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String plateNumber;
    private String vehicleOwner;
    private String contactPhone;
    private Integer vehicleType;
    private LocalDateTime effectiveTime;
    private LocalDateTime expiryTime;
    private Integer status;
    private String remark;
}


























