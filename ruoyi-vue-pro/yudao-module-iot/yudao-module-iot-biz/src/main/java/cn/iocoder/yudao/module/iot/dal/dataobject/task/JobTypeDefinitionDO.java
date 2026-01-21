package cn.iocoder.yudao.module.iot.dal.dataobject.task;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 定时任务类型定义 DO
 *
 * @author 芋道源码
 */
@TableName("iot_job_type_definition")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobTypeDefinitionDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务编码，唯一标识
     */
    private String code;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 业务类型 (IOT_DEVICE, SPATIAL, ENERGY, SECURITY, HVAC, SYSTEM)
     */
    private String businessType;

    /**
     * 适用实体类型 (PRODUCT, DEVICE, CAMPUS, BUILDING, FLOOR, AREA)，逗号分隔
     */
    private String applicableEntities;

    /**
     * 默认配置模板 (JSON)
     */
    private String defaultConfigTemplate;

    /**
     * 状态 (0-禁用, 1-启用)
     */
    private Integer status;

}




