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

@TableName(value = "iot_cloud_defense_area", autoResultMap = true)
@KeySequence("iot_cloud_defense_area_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CloudDefenseAreaDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String areaCode;

    private String areaName;

    private String areaType;

    private Long spaceId;

    private BigDecimal layoutX;

    private BigDecimal layoutY;

    private BigDecimal layoutWidth;

    private BigDecimal layoutHeight;

    private String detailText;

    private Integer sort;

    private Integer enabled;
}
