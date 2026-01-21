package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 门岗 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_door_post", autoResultMap = true)
@KeySequence("iot_door_post_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoorPostDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 门岗名称
     */
    private String postName;

    /**
     * 描述
     */
    private String description;

}


























