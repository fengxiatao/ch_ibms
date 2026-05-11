package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 照明回路 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_lighting_circuit")
@KeySequence("ibms_lighting_circuit_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsLightingCircuitDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 回路编号
     */
    private String circuitCode;

    /**
     * 回路名称
     */
    private String circuitName;

    /**
     * 回路类型：1-普通照明 2-调光回路 3-应急照明
     */
    private Integer circuitType;

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
     * 负载描述
     */
    private String loadDesc;

    /**
     * 额定功率(W)
     */
    private BigDecimal ratedPower;

    /**
     * 灯具数量
     */
    private Integer lightCount;

    /**
     * 状态：0-关闭 1-开启 2-故障
     */
    private Integer status;

    /**
     * 亮度(0-100)
     */
    private Integer brightness;

    /**
     * 色温(K)
     */
    private Integer colorTemp;

    /**
     * 控制器ID
     */
    private Long controllerId;

    /**
     * 网关ID
     */
    private Long gatewayId;

    /**
     * 最后操作时间
     */
    private LocalDateTime lastOperateTime;

    /**
     * 备注
     */
    private String remark;

}
