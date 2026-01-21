package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门岗设备关联 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_door_post_device", autoResultMap = true)
@KeySequence("iot_door_post_device_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoorPostDeviceDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private Long deviceId;
    
    /**
     * 设备类型
     * 1-门禁通道，2-视频监控
     */
    private Integer deviceType;
    
    private LocalDateTime createTime;
    private Long tenantId;

}


























