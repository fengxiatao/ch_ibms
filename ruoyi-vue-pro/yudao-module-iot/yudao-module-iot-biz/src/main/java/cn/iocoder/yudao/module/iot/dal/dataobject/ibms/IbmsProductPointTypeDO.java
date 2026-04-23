package cn.iocoder.yudao.module.iot.dal.dataobject.ibms;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IBMS 产品点位类型定义 DO
 *
 * 对应表：ibms_product_point_type
 *
 * 一条记录表示某产品下的一类点位，例如：
 * - VT 视频通道 x 1
 * - DI 数字输入 x 8
 *
 * @author
 */
@TableName("ibms_product_point_type")
@KeySequence("ibms_product_point_type_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsProductPointTypeDO extends TenantBaseDO {

    @TableId
    private Long id;

    /**
     * 产品 ID，关联 ibms_product.id
     */
    private Long productId;

    /**
     * 点位类型码，ibms_point_type.value，例如 VT/AI/DR
     */
    private String pointTypeCode;

    /**
     * 点位名称，可覆盖字典 label
     */
    private String name;

    /**
     * 该类型点位数量
     */
    private Integer count;

    /**
     * 数据类型，冗余自 ibms_point_type.remark.dataType
     */
    private String dataType;

    /**
     * 扩展字段 JSON（字符串存储）
     */
    private String remark;
}

