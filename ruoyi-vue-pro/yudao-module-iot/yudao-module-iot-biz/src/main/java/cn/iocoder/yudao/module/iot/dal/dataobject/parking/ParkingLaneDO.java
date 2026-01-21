package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 车道 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_lane")
@KeySequence("iot_parking_lane_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLaneDO extends TenantBaseDO {

    /**
     * 车道ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 车道名称
     */
    private String laneName;

    /**
     * 车道编码
     */
    private String laneCode;

    /**
     * 所属车场ID
     */
    private Long lotId;

    /**
     * 出入口配置：1-入口，2-出口，3-出入口
     */
    private Integer direction;

    /**
     * 主相机设备ID
     */
    private Long mainCameraId;

    /**
     * 主显屏幕设备ID
     */
    private Long mainScreenId;

    /**
     * 辅助相机设备ID
     */
    private Long auxCameraId;

    /**
     * 辅显屏幕设备ID
     */
    private Long auxScreenId;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
