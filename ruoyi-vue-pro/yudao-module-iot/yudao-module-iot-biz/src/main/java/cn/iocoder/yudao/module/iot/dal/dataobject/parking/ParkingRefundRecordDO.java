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
 * 停车退款记录 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_refund_record")
@KeySequence("iot_parking_refund_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingRefundRecordDO extends TenantBaseDO {

    /**
     * 退款记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的停车记录ID
     */
    private Long recordId;

    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 原支付订单号（商户订单号）
     */
    private String outTradeNo;

    /**
     * 微信支付交易号
     */
    private String transactionId;

    /**
     * 退款单号（商户退款单号）
     */
    private String outRefundNo;

    /**
     * 微信退款单号
     */
    private String refundId;

    /**
     * 原订单金额（元）
     */
    private BigDecimal totalFee;

    /**
     * 退款金额（元）
     */
    private BigDecimal refundFee;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款状态：0-申请中，1-退款成功，2-退款失败，3-退款关闭
     */
    private Integer refundStatus;

    /**
     * 退款申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 退款完成时间
     */
    private LocalDateTime refundTime;

    /**
     * 申请人
     */
    private String applyUser;

    /**
     * 审核人
     */
    private String auditUser;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 备注
     */
    private String remark;
}
