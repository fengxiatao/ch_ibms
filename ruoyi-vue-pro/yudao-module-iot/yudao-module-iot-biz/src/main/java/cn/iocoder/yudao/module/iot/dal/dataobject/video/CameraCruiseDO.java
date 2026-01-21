package cn.iocoder.yudao.module.iot.dal.dataobject.video;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 摄像头巡航路线 DO
 *
 * @author 芋道源码
 */
@TableName("iot_camera_cruise")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CameraCruiseDO extends BaseDO {

    /**
     * 巡航路线ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 巡航路线名称
     */
    private String cruiseName;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态（0:停止 1:运行中）
     */
    private Integer status;

    /**
     * 每个预设点停留时间（秒）
     */
    private Integer dwellTime;

    /**
     * 是否循环（0:否 1:是）
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean loopEnabled;

}
