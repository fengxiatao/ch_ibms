package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 月租车 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_monthly_vehicle")
@KeySequence("iot_parking_monthly_vehicle_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingMonthlyVehicleDO extends TenantBaseDO {

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
     * 车主姓名
     */
    private String ownerName;

    /**
     * 车主电话
     */
    private String ownerPhone;

    /**
     * 车辆类型：1-小型车，2-中型车，3-新能源车，4-大型车，5-超大型车
     */
    private Integer vehicleType;

    /**
     * 所属车场ID
     */
    private Long lotId;

    /**
     * 月卡有效期开始
     */
    private LocalDateTime validStart;

    /**
     * 月卡有效期结束
     */
    private LocalDateTime validEnd;

    /**
     * 月租费
     */
    private BigDecimal monthlyFee;

    /**
     * 最后充值时间
     */
    private LocalDateTime lastChargeTime;

    /**
     * 最后充值月数
     */
    private Integer lastChargeMonths;

    /**
     * 状态：0-正常，1-停用，2-过期
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
