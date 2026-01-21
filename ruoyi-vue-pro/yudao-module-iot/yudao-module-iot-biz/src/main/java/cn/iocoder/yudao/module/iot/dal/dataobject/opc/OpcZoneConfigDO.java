package cn.iocoder.yudao.module.iot.dal.dataobject.opc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * OPC 防区配置 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_opc_zone_config")
@KeySequence("iot_opc_zone_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpcZoneConfigDO extends TenantBaseDO {

    /**
     * 配置ID
     */
    @TableId
    private Long id;

    /**
     * 设备ID（关联iot_device.id）
     */
    private Long deviceId;

    /**
     * 防区号（01-99）
     */
    private Integer area;

    /**
     * 点位号（001-999）
     */
    private Integer point;

    /**
     * 防区名称（手动配置）
     */
    private String zoneName;

    /**
     * 防区类型
     * instant1 - 立即1
     * instant2 - 立即2
     * delay1 - 延时1
     * delay2 - 延时2
     * follow - 跟随
     * 24hour - 24小时
     */
    private String zoneType;

    /**
     * 位置信息（手动配置）
     */
    private String location;

    /**
     * 关联摄像头ID（手动配置）
     */
    private Long cameraId;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备注
     */
    private String remark;
}
