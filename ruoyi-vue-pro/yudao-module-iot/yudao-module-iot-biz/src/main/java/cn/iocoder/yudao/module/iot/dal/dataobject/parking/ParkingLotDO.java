package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 停车场/车场 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_lot")
@KeySequence("iot_parking_lot_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotDO extends TenantBaseDO {

    /**
     * 车场ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 车场名称
     */
    private String lotName;

    /**
     * 车场编码
     */
    private String lotCode;

    /**
     * 车场类型：1-收费，2-免费
     */
    private Integer lotType;

    /**
     * 总车位数
     */
    private Integer totalSpaces;

    /**
     * 月租费用
     */
    private BigDecimal monthlyFee;

    /**
     * 车场地址
     */
    private String address;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 支付后免费出场时间(分钟)
     */
    private Integer freeExitMinutes;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
