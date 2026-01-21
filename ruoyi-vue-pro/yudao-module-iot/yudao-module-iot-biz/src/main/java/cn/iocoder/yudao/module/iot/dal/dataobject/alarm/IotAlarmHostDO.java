package cn.iocoder.yudao.module.iot.dal.dataobject.alarm;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 报警主机 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_alarm_host")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAlarmHostDO extends TenantBaseDO {

    /**
     * 主机ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联设备ID
     */
    private Long deviceId;

    /**
     * 主机名称
     */
    private String hostName;

    /**
     * 主机型号
     */
    private String hostModel;

    /**
     * 主机序列号
     */
    private String hostSn;

    /**
     * 防区数量
     */
    private Integer zoneCount;

    /**
     * 在线状态：0-离线, 1-在线
     */
    private Integer onlineStatus;

    /**
     * 布防状态：DISARM-撤防, ARM_ALL-全部布防, ARM_EMERGENCY-紧急布防
     */
    private String armStatus;

    /**
     * 报警状态：0-正常, 1-报警中
     */
    private Integer alarmStatus;

    /**
     * 安装位置
     */
    private String location;

    /**
     * 备注
     */
    private String remark;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 主机账号
     */
    private String account;

    /**
     * 主机密码
     */
    private String password;

    /**
     * 系统状态：0-撤防，1-布防，2-居家布防
     */
    private Integer systemStatus;

    /**
     * 最后查询时间
     */
    private LocalDateTime lastQueryTime;
}
