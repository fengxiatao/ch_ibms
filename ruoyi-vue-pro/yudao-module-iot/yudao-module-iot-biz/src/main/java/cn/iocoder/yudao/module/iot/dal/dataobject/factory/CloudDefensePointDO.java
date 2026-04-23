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

import java.math.BigDecimal;

@TableName(value = "iot_cloud_defense_point", autoResultMap = true)
@KeySequence("iot_cloud_defense_point_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CloudDefensePointDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long areaId;

    private Long deviceId;

    private Long channelId;

    private String pointCode;

    private String pointName;

    private String pointType;

    private BigDecimal layoutX;

    private BigDecimal layoutY;

    private Integer armedStatus;

    private Integer alarmStatus;

    private Integer onlineStatus;

    private Integer sort;

    private Integer enabled;
}
