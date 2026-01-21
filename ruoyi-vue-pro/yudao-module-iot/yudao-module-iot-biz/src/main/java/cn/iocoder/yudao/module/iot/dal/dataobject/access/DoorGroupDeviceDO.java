package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门组设备关联 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_door_group_device", autoResultMap = true)
@KeySequence("iot_door_group_device_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoorGroupDeviceDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;
    private Long deviceId;
    private LocalDateTime createTime;
    private Long tenantId;

}


























