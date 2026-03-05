package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 环境传感器 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_env_sensor")
@KeySequence("ibms_env_sensor_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsEnvSensorDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 传感器编号
     */
    private String sensorCode;

    /**
     * 传感器名称
     */
    private String sensorName;

    /**
     * 传感器类型：1-温湿度 2-空气质量 3-光照 4-噪音 5-压力
     */
    private Integer sensorType;

    /**
     * 所属区域ID
     */
    private Long areaId;

    /**
     * 所属区域名称
     */
    private String areaName;

    /**
     * 楼层
     */
    private String floor;

    /**
     * 详细位置
     */
    private String location;

    /**
     * 状态：0-离线 1-在线 2-告警 3-故障
     */
    private Integer status;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 安装时间
     */
    private LocalDateTime installTime;

    /**
     * 备注
     */
    private String remark;

}
