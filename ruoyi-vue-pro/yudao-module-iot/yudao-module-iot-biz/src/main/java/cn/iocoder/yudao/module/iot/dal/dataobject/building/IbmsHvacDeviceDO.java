package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 暖通设备 DO（空调机组、新风机组、送排风机）
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_hvac_device")
@KeySequence("ibms_hvac_device_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsHvacDeviceDO extends BaseDO {

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
     * 设备类型：1-空调机组 2-新风机组 3-送风机 4-排风机
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
     * 楼层
     */
    private String floor;

    /**
     * 详细位置
     */
    private String location;

    /**
     * 状态：0-停止 1-运行 2-待机 3-故障
     */
    private Integer status;

    /**
     * 运行模式：1-制冷 2-制热 3-通风 4-自动
     */
    private Integer runMode;

    /**
     * 设定温度(℃)
     */
    private BigDecimal setTemp;

    /**
     * 室内温度(℃)
     */
    private BigDecimal roomTemp;

    /**
     * 风速：1-低速 2-中速 3-高速 4-自动
     */
    private Integer windSpeed;

    /**
     * 滤网状态
     */
    private String filterStatus;

    /**
     * 风压(Pa)
     */
    private BigDecimal pressure;

    /**
     * 累计运行时长(小时)
     */
    private Integer runHours;

    /**
     * 维护状态
     */
    private String maintainStatus;

    /**
     * 下次维护日期
     */
    private LocalDate nextMaintainDate;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 备注
     */
    private String remark;

}
