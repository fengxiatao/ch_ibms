package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 照明网关 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_lighting_gateway")
@KeySequence("ibms_lighting_gateway_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsLightingGatewayDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 网关编号
     */
    private String gatewayCode;

    /**
     * 网关名称
     */
    private String gatewayName;

    /**
     * 网关型号
     */
    private String gatewayModel;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * MAC地址
     */
    private String macAddress;

    /**
     * 安装位置
     */
    private String areaName;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 接入设备数
     */
    private Integer deviceCount;

    /**
     * 信号强度
     */
    private String signalStrength;

    /**
     * 状态：0-离线 1-在线 2-故障
     */
    private Integer status;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 备注
     */
    private String remark;

}
