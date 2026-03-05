package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 给排水设备 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_water_device")
@KeySequence("ibms_water_device_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsWaterDeviceDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型：1-生活水泵 2-排污泵 3-水箱水池
     */
    private Integer deviceType;

    /**
     * 所属区域ID
     */
    private Long areaId;

    /**
     * 所属区域名称
     */
    private String areaName;

    /**
     * 详细位置
     */
    private String location;

    /**
     * 状态：0-停止 1-运行 2-待机 3-故障
     */
    private Integer status;

    /**
     * 运行模式：1-自动 2-手动
     */
    private Integer runMode;

    /**
     * 出口压力(MPa)
     */
    private BigDecimal pressure;

    /**
     * 液位(%)
     */
    private BigDecimal waterLevel;

    /**
     * 累计运行时长(小时)
     */
    private Integer runHours;

    /**
     * 维护状态
     */
    private String maintainStatus;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 备注
     */
    private String remark;

}
