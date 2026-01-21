package cn.iocoder.yudao.module.iot.dal.dataobject.camera;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 摄像头预置位 DO
 *
 * @author 长辉信息
 */
@TableName("iot_camera_preset")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotCameraPresetDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 摄像头ID
     */
    private Long cameraId;

    /**
     * 预置位编号(1-255)
     */
    private Integer presetId;

    /**
     * 预置位名称
     */
    private String name;

    /**
     * 水平角度(-180~180)
     */
    private Float panAngle;

    /**
     * 垂直角度(-90~90)
     */
    private Float tiltAngle;

    /**
     * 变倍级别(1-10)
     */
    private Integer zoomLevel;

    /**
     * 描述
     */
    private String description;

}

