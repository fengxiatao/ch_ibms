package cn.iocoder.yudao.module.iot.dal.dataobject.camera;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 摄像头录像记录 DO
 *
 * @author 长辉信息
 */
@TableName("iot_camera_recording")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotCameraRecordingDO extends BaseDO {

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
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 时长(秒)
     */
    private Integer duration;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 录像类型(1:手动 2:定时 3:报警触发 4:移动侦测)
     */
    private Integer recordingType;

    /**
     * 状态(0:录像中 1:已完成 2:已停止 3:异常)
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

}

