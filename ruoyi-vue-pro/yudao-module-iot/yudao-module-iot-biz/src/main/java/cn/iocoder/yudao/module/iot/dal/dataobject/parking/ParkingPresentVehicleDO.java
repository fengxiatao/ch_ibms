package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 在场车辆 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_present_vehicle")
@KeySequence("iot_parking_present_vehicle_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingPresentVehicleDO extends TenantBaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 车辆类型：1-小型车，2-中型车，3-新能源车，4-大型车，5-超大型车
     */
    private Integer vehicleType;

    /**
     * 车辆类别：free-免费车，monthly-月租车，temporary-临时车
     */
    private String vehicleCategory;

    /**
     * 车场ID
     */
    private Long lotId;

    /**
     * 入场车道ID
     */
    private Long entryLaneId;

    /**
     * 入场道闸ID
     */
    private Long entryGateId;

    /**
     * 入场时间
     */
    private LocalDateTime entryTime;

    /**
     * 入场照片URL
     */
    private String entryPhotoUrl;

    /**
     * 入场操作员
     */
    private String entryOperator;

    /**
     * 停车时长(分钟)
     */
    private Integer parkingDuration;

    /**
     * 长期停车标识：0-否，1-超一个月，2-超三个月
     */
    private Integer longTermFlag;

    /**
     * 状态：0-正常在场
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
