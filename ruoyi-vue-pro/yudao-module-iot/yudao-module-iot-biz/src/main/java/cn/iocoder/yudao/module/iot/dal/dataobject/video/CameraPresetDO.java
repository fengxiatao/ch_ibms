package cn.iocoder.yudao.module.iot.dal.dataobject.video;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 摄像头预设点 DO
 *
 * @author 芋道源码
 */
@TableName("iot_camera_preset")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CameraPresetDO extends BaseDO {

    /**
     * 预设点ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 预设点编号（1-255）
     */
    private Integer presetNo;

    /**
     * 预设点名称
     */
    private String presetName;

    /**
     * 预设点描述
     */
    private String description;

    /**
     * 水平角度（Pan）
     */
    private BigDecimal pan;

    /**
     * 垂直角度（Tilt）
     */
    private BigDecimal tilt;

    /**
     * 变焦值（Zoom）
     */
    private BigDecimal zoom;

}
