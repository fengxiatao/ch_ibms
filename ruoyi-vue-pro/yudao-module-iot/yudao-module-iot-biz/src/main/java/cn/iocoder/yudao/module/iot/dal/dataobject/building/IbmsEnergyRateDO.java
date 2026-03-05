package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 能耗费率设置 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_energy_rate")
@KeySequence("ibms_energy_rate_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsEnergyRateDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 费率名称
     */
    private String rateName;

    /**
     * 能源类型：1-电力 2-水 3-燃气 4-冷 5-热
     */
    private Integer energyType;

    /**
     * 费率类型：1-单一费率 2-阶梯费率 3-峰谷电价
     */
    private Integer rateType;

    /**
     * 阶梯等级
     */
    private Integer tierLevel;

    /**
     * 阶梯起始用量
     */
    private BigDecimal tierStart;

    /**
     * 阶梯结束用量
     */
    private BigDecimal tierEnd;

    /**
     * 时段类型：peak-峰 valley-谷 flat-平
     */
    private String timePeriod;

    /**
     * 开始时间（峰谷电价时使用）
     */
    private LocalTime startTime;

    /**
     * 结束时间（峰谷电价时使用）
     */
    private LocalTime endTime;

    /**
     * 单价（元）
     */
    private BigDecimal unitPrice;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 生效日期
     */
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    private LocalDate expireDate;

    /**
     * 备注
     */
    private String remark;

}
