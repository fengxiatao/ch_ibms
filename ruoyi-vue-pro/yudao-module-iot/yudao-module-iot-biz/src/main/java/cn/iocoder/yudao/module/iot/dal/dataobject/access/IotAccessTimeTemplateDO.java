package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 门禁时间模板 DO
 *
 * @author 芋道源码
 */
@TableName("iot_access_time_template")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessTimeTemplateDO extends TenantBaseDO {

    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 周配置（JSON格式，包含周一到周日的时间段）
     */
    private String weekConfig;

    /**
     * 节假日配置（JSON格式）
     */
    private String holidayConfig;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

}
