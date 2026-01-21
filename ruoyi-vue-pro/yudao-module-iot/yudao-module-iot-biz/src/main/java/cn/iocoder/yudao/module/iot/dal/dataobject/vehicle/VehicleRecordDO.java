package cn.iocoder.yudao.module.iot.dal.dataobject.vehicle;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@TableName(value = "iot_vehicle_record", autoResultMap = true)
@KeySequence("iot_vehicle_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRecordDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String plateNumber;
    private String vehicleType;
    private String vehicleColor;
    private String driverName;
    private String driverPhone;
    private String purposeDescription;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private String entryPhotoUrl;
    private String exitPhotoUrl;
    private Integer recordStatus;
    private String remark;
}


























