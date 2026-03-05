package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 照明场景 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_lighting_scene")
@KeySequence("ibms_lighting_scene_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsLightingSceneDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 场景编号
     */
    private String sceneCode;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 场景图标
     */
    private String sceneIcon;

    /**
     * 场景描述
     */
    private String sceneDesc;

    /**
     * 适用区域ID
     */
    private Long areaId;

    /**
     * 适用区域名称
     */
    private String areaName;

    /**
     * 是否激活
     */
    private Boolean isActive;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

}
