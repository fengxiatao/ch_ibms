package cn.iocoder.yudao.module.iot.dal.dataobject.ibms;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IBMS 通道（点位）DO
 *
 * 对应表：ibms_channel
 */
@TableName(value = "ibms_channel", autoResultMap = true)
@KeySequence("ibms_channel_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsChannelDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 空间ID，可为空表示未分配 */
    private Long spaceId;

    /** 设备ID，可为空 */
    private Long deviceId;

    /** 通道编码，如 F01-LBY-VI-VT-001 */
    private String code;

    /** 通道号 */
    private Integer channelNo;

    /** 通道名称 */
    private String name;

    /** 业务分类：security/access/alarm/parking/building/environment/lighting/energy */
    private String business;

    /** 通道类型码（点位类型码） */
    private String typeCode;

    /** 通道类别（展示文案） */
    private String category;

    /** 系统类型，如 VI/AC/AL/BA/EN... */
    private String systemType;

    /** 数据源，如 NVR/CTR/GW/DDC/Meter */
    private String dataSource;

    /** IP 地址 */
    private String ip;

    /** MAC 地址 */
    private String mac;

    /** 设备序列号（冗余） */
    private String deviceSn;

    /** 所属设备名称（冗余） */
    private String deviceName;

    /** 空间位置文案（冗余） */
    private String space;

    /** 当前值 */
    private String currentValue;

    /** 状态：online/offline/warning/armed */
    private String status;

    /** 扩展 JSON 字符串 */
    private String extra;
}

