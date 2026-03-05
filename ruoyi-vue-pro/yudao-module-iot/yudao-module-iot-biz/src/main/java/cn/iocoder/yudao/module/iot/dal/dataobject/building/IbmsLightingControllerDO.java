package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 照明控制器 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_lighting_controller")
@KeySequence("ibms_lighting_controller_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsLightingControllerDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 控制器编号
     */
    private String controllerCode;

    /**
     * 控制器名称
     */
    private String controllerName;

    /**
     * 控制器型号
     */
    private String controllerModel;

    /**
     * 安装位置
     */
    private String areaName;

    /**
     * 通道数
     */
    private Integer channelCount;

    /**
     * 额定负载
     */
    private String ratedLoad;

    /**
     * 当前负载率
     */
    private String currentLoad;

    /**
     * 所属网关ID
     */
    private Long gatewayId;

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
