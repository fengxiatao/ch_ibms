package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 能耗统计-日 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_energy_statistics_daily")
@KeySequence("ibms_energy_statistics_daily_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsEnergyStatisticsDailyDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 表具ID
     */
    private Long meterId;

    /**
     * 表具编号
     */
    private String meterCode;

    /**
     * 表具类型
     */
    private Integer meterType;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 消耗量
     */
    private BigDecimal consumption;

    /**
     * 费用
     */
    private BigDecimal cost;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}
