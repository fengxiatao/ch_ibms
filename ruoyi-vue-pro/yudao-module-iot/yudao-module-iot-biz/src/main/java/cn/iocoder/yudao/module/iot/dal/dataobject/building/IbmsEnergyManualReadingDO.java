package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人工抄表记录 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_energy_manual_reading")
@KeySequence("ibms_energy_manual_reading_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsEnergyManualReadingDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 仪表ID
     */
    private Long meterId;

    /**
     * 仪表编码
     */
    private String meterCode;

    /**
     * 仪表名称
     */
    private String meterName;

    /**
     * 抄表日期
     */
    private LocalDate readingDate;

    /**
     * 抄表时间
     */
    private LocalDateTime readingTime;

    /**
     * 上期读数
     */
    private BigDecimal lastReading;

    /**
     * 本期读数
     */
    private BigDecimal currentReading;

    /**
     * 本期用量
     */
    private BigDecimal consumption;

    /**
     * 抄表人
     */
    private String reader;

    /**
     * 状态：0-待复核 1-已确认 2-已作废
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 复核人
     */
    private String reviewer;

    /**
     * 复核时间
     */
    private LocalDateTime reviewTime;

}
