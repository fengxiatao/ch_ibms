package cn.iocoder.yudao.module.iot.dal.dataobject.camera;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 监控墙配置 DO
 *
 * @author 长辉信息
 */
@TableName("iot_monitor_wall")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotMonitorWallDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 监控墙名称
     */
    private String name;

    /**
     * 布局(1x1,2x2,3x3,4x4)
     */
    private String layout;

    /**
     * 摄像头ID列表(JSON数组)
     */
    private String cameraIds;

    /**
     * 是否默认(0:否 1:是)
     */
    private Boolean isDefault;

    /**
     * 描述
     */
    private String description;

}

