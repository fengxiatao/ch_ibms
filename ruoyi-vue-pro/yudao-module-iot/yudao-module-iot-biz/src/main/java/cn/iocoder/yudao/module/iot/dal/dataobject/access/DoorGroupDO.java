package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 门组 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_door_group", autoResultMap = true)
@KeySequence("iot_door_group_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoorGroupDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 门组名称
     */
    private String groupName;

    /**
     * 描述
     */
    private String description;

}


























