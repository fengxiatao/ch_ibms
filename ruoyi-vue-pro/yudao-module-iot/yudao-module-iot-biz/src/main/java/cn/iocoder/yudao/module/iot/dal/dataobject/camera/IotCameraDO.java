package cn.iocoder.yudao.module.iot.dal.dataobject.camera;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 摄像头扩展信息 DO
 *
 * @author 长辉信息
 */
@TableName("iot_camera")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotCameraDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 主码流地址
     */
    private String streamUrlMain;

    /**
     * 子码流地址
     */
    private String streamUrlSub;

    /**
     * RTSP端口
     */
    private Integer rtspPort;

    /**
     * ONVIF端口
     */
    private Integer onvifPort;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码（加密）
     */
    private String password;

    /**
     * 厂商
     */
    private String manufacturer;

    /**
     * 型号
     */
    private String model;

    /**
     * 是否支持PTZ
     */
    private Boolean ptzSupport;

    /**
     * 是否支持音频
     */
    private Boolean audioSupport;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 帧率(FPS)
     */
    private Integer frameRate;

    /**
     * 码率(Kbps)
     */
    private Integer bitRate;

    /**
     * 预置位数量
     */
    private Integer presetCount;

    /**
     * 亮度(0-100)
     */
    private Integer brightness;

    /**
     * 对比度(0-100)
     */
    private Integer contrast;

    /**
     * 饱和度(0-100)
     */
    private Integer saturation;

}

