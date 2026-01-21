package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 收费规则 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_charge_rule")
@KeySequence("iot_parking_charge_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingChargeRuleDO extends TenantBaseDO {

    /**
     * 规则ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 收费模式：1-按次收费，2-按时收费
     */
    private Integer chargeMode;

    /**
     * 按次收费金额
     */
    private BigDecimal perTimeFee;

    /**
     * 免费时长(分钟)
     */
    private Integer freeMinutes;

    /**
     * 首小时费用
     */
    private BigDecimal firstHourFee;

    /**
     * 超出后每小时费用
     */
    private BigDecimal extraHourFee;

    /**
     * 每日最高收费
     */
    private BigDecimal maxDailyFee;

    /**
     * 循环收费方式：1-24小时循环，2-自然日循环
     */
    private Integer cycleType;

    /**
     * 夜间折扣(0.5表示5折)
     */
    private BigDecimal nightDiscount;

    /**
     * 夜间开始时间
     */
    private LocalTime nightStartTime;

    /**
     * 夜间结束时间
     */
    private LocalTime nightEndTime;

    /**
     * 规则详细配置(JSON格式)
     */
    private String ruleConfig;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
