package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 免费车 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_free_vehicle")
@KeySequence("iot_parking_free_vehicle_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingFreeVehicleDO extends TenantBaseDO {

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
     * 特殊车类型：武警车、警车等
     */
    private String specialType;

    /**
     * 有效期开始
     */
    private LocalDateTime validStart;

    /**
     * 有效期结束
     */
    private LocalDateTime validEnd;

    /**
     * 适用车场ID列表(JSON数组)
     */
    private String lotIds;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
