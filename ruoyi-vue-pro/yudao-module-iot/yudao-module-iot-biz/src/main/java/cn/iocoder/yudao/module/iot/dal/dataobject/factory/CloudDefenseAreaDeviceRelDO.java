package cn.iocoder.yudao.module.iot.dal.dataobject.factory;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@TableName(value = "iot_cloud_defense_area_device_rel", autoResultMap = true)
@KeySequence("iot_cloud_defense_area_device_rel_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CloudDefenseAreaDeviceRelDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long areaId;

    private Long deviceId;

    private Long channelId;

    private Integer sort;
}
