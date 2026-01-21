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
 * 停车进出记录 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_record")
@KeySequence("iot_parking_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingRecordDO extends TenantBaseDO {

    /**
     * 记录ID
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
     * 出场车道ID
     */
    private Long exitLaneId;

    /**
     * 出场道闸ID
     */
    private Long exitGateId;

    /**
     * 出场时间
     */
    private LocalDateTime exitTime;

    /**
     * 出场照片URL
     */
    private String exitPhotoUrl;

    /**
     * 出场操作员
     */
    private String exitOperator;

    /**
     * 停车时长(分钟)
     */
    private Integer parkingDuration;

    /**
     * 应收金额
     */
    private BigDecimal chargeAmount;

    /**
     * 实收金额
     */
    private BigDecimal paidAmount;

    /**
     * 支付方式：cash-现金，wechat-微信，alipay-支付宝，card-刷卡
     */
    private String paymentMethod;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 支付状态：0-未支付，1-已支付，2-免费
     */
    private Integer paymentStatus;

    /**
     * 放行方式：normal-正常，force-强制出场，free-免费放行
     */
    private String exitType;

    /**
     * 使用的收费规则ID
     */
    private Long chargeRuleId;

    /**
     * 使用的放行规则ID
     */
    private Long passRuleId;

    /**
     * 记录状态：1-在场，2-已出场
     */
    private Integer recordStatus;

    /**
     * 备注
     */
    private String remark;
}
