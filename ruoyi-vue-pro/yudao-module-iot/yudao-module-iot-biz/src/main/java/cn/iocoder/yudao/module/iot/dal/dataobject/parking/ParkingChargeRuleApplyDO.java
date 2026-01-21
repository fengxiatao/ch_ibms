package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 收费规则应用 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_charge_rule_apply")
@KeySequence("iot_parking_charge_rule_apply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingChargeRuleApplyDO extends TenantBaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 应用名称
     */
    private String applyName;

    /**
     * 适用车场ID列表(JSON数组)
     */
    private String lotIds;

    /**
     * 车辆类型：temporary-临时车
     */
    private String vehicleCategory;

    /**
     * 收费车型(JSON数组)：小型车、中型车等
     */
    private String chargeVehicleTypes;

    /**
     * 关联的收费规则ID
     */
    private Long ruleId;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 是否启用：0-关闭，1-启用
     */
    private Integer enabled;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
