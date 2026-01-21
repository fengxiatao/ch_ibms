package cn.iocoder.yudao.module.iot.dal.dataobject.video;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 摄像头巡航点 DO
 *
 * @author 芋道源码
 */
@TableName("iot_camera_cruise_point")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CameraCruisePointDO extends BaseDO {

    /**
     * 巡航点ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 巡航路线ID
     */
    private Long cruiseId;

    /**
     * 预设点ID
     */
    private Long presetId;

    /**
     * 顺序（从1开始）
     */
    private Integer sortOrder;

    /**
     * 停留时间（秒，为空则使用路线默认值）
     */
    private Integer dwellTime;

}
