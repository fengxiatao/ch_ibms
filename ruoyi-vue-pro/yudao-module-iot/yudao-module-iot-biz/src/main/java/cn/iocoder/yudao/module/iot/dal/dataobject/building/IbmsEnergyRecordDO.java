package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 能耗记录 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_energy_record")
@KeySequence("ibms_energy_record_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsEnergyRecordDO {

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
     * 抄表读数
     */
    private BigDecimal readingValue;

    /**
     * 消耗量
     */
    private BigDecimal consumption;

    /**
     * 抄表时间
     */
    private LocalDateTime readingTime;

    /**
     * 记录类型：1-自动采集 2-人工抄表
     */
    private Integer recordType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}
