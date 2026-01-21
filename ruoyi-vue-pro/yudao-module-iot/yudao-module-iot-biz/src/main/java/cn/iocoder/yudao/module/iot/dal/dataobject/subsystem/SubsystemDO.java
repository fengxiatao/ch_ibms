package cn.iocoder.yudao.module.iot.dal.dataobject.subsystem;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT子系统定义 DO
 * 
 * @author IBMS Team
 */
@TableName("iot_subsystem")
@KeySequence("iot_subsystem_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsystemDO extends BaseDO {

    /**
     * 子系统ID
     */
    @TableId
    private Long id;
    
    /**
     * 子系统代码（如 security.video）
     */
    private String code;
    
    /**
     * 子系统名称
     */
    private String name;
    
    /**
     * 父系统代码
     */
    private String parentCode;
    
    /**
     * 层级：1-一级系统，2-二级系统
     */
    private Integer level;
    
    /**
     * 关联的菜单ID
     */
    private Long menuId;
    
    /**
     * 菜单路径
     */
    private String menuPath;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 是否启用
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean enabled;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}


















































