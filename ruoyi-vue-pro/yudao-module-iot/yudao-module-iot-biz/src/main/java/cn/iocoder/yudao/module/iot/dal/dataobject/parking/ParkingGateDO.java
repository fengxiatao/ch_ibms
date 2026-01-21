package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 道闸设备 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_gate")
@KeySequence("iot_parking_gate_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingGateDO extends TenantBaseDO {

    /**
     * 道闸ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 道闸名称
     */
    private String gateName;

    /**
     * 道闸编码
     */
    private String gateCode;

    /**
     * 所属车场ID
     */
    private Long lotId;

    /**
     * 所属车道ID
     */
    private Long laneId;

    /**
     * 关联的IOT设备ID
     */
    private Long deviceId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 厂商
     */
    private String manufacturer;

    /**
     * 型号
     */
    private String model;

    /**
     * 道闸类型：1-车牌识别一体机，2-普通道闸
     */
    private Integer gateType;

    /**
     * 方向：1-入口，2-出口，3-出入口
     */
    private Integer direction;

    /**
     * 在线状态：0-离线，1-在线
     */
    private Integer onlineStatus;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
