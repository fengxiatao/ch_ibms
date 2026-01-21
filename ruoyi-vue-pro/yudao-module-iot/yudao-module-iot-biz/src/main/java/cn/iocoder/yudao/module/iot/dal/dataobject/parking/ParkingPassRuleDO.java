package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 放行规则 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_pass_rule")
@KeySequence("iot_parking_pass_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingPassRuleDO extends TenantBaseDO {

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
     * 适用车场ID列表(JSON数组)
     */
    private String lotIds;

    /**
     * 特殊车类型(JSON数组)：武警车、警车等
     */
    private String specialVehicleTypes;

    /**
     * 车辆类型(JSON数组)：免费车、月租车、临时车
     */
    private String vehicleCategories;

    /**
     * 收费车型(JSON数组)：小型车、中型车等
     */
    private String chargeVehicleTypes;

    /**
     * 入场确认规则：1-自动放行，2-人工确认
     */
    private Integer entryConfirmRule;

    /**
     * 出场确认规则：1-自动放行，2-人工确认
     */
    private Integer exitConfirmRule;

    /**
     * 车道权限配置(JSON数组)
     */
    private String laneIds;

    /**
     * 优先级(数值越大优先级越高)
     */
    private Integer priority;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
