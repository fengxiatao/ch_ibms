package cn.iocoder.yudao.module.iot.dal.dataobject.ibms;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IBMS 产品属性定义 DO
 *
 * 对应表：ibms_product_property
 *
 * 用于描述一个产品下的“配置模板”字段，例如：
 * - resolution / 分辨率 / select / ["1080P","720P"]
 * - night_vision / 夜视距离 / number / 米
 */
@TableName("ibms_product_property")
@KeySequence("ibms_product_property_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsProductPropertyDO extends TenantBaseDO {

    @TableId
    private Long id;

    /**
     * 产品 ID，关联 ibms_product.id
     */
    private Long productId;

    /**
     * 属性英文名 key，如 resolution/night_vision
     */
    private String propName;

    /**
     * 显示名称，如 分辨率
     */
    private String label;

    /**
     * 属性类型：text/number/select/checkbox
     */
    private String type;

    /**
     * 可选项 JSON 字符串，仅 type=select 时使用
     */
    private String options;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 单位，如 米/个
     */
    private String unit;

    /**
     * 扩展字段 JSON（字符串存储）
     */
    private String remark;
}

