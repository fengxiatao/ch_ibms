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
 * 月卡充值记录 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_monthly_recharge")
@KeySequence("iot_parking_monthly_recharge_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingRechargeRecordDO extends TenantBaseDO {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 月租车ID
     */
    private Long monthlyVehicleId;

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
     * 充值月数
     */
    private Integer rechargeMonths;

    /**
     * 有效期开始
     */
    private LocalDateTime validStart;

    /**
     * 有效期结束
     */
    private LocalDateTime validEnd;

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
     * 支付状态：0-未支付，1-已支付
     */
    private Integer paymentStatus;

    /**
     * 操作员
     */
    private String operator;

    /**
     * 备注
     */
    private String remark;
}
