package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 能源表具 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_energy_meter")
@KeySequence("ibms_energy_meter_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsEnergyMeterDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 表具编号
     */
    private String meterCode;

    /**
     * 表具名称
     */
    private String meterName;

    /**
     * 表具类型：1-电表 2-水表 3-燃气表 4-冷量表 5-热量表
     */
    private Integer meterType;

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
     * 安装位置
     */
    private String location;

    /**
     * 状态：0-离线 1-在线 2-故障
     */
    private Integer status;

    /**
     * 当前读数
     */
    private BigDecimal currentReading;

    /**
     * 计量单位
     */
    private String unit;

    /**
     * 倍率
     */
    private BigDecimal multiplier;

    /**
     * 最后抄表时间
     */
    private LocalDateTime lastReadingTime;

    /**
     * 通讯方式：1-RS485 2-MBUS 3-LoRa 4-NB-IoT
     */
    private Integer communicationType;

    /**
     * 安装时间
     */
    private LocalDateTime installTime;

    /**
     * 备注
     */
    private String remark;

}
