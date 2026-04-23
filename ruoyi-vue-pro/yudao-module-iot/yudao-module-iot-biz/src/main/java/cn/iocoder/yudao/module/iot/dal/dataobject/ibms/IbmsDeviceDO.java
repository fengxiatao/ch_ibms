package cn.iocoder.yudao.module.iot.dal.dataobject.ibms;

import cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler;
import cn.iocoder.yudao.framework.mybatis.core.type.LongSetTypeHandler;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Set;

/**
 * IBMS 设备 DO
 *
 * 对应表：ibms_device
 *
 * 编码段说明：
 * - deviceCode：{系统}-{型号码}-{设备类型}-{品牌}-{流水}，例 VI-NV-NVR-DAH-001（空间不参与编码）
 * - groupCode：专业分组码，ibms_group.value
 * - systemCode：系统码，ibms_system.value
 * - deviceTypeCode：设备类型码，ibms_device_type.value
 *
 * 设备仅保存与编码规范和管理展示相关的核心字段，运行状态类字段后续可扩展到 extra JSON 中。
 */
@TableName(value = "ibms_device", autoResultMap = true)
@KeySequence("ibms_device_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsDeviceDO extends TenantBaseDO {

    /** 主键 ID */
    @TableId
    private Long id;

    /** 设备编码：VI-NV-NVR-DAH-001 */
    private String deviceCode;

    /** 设备名称 */
    private String name;

    /** 备注名称（收敛自 iot_device.nickname，需执行 ibms_device_extend_convergence.sql） */
    private String nickname;

    /** 设备图片 */
    private String picUrl;

    /** 设备唯一标识（MQTT 等），列名 device_key */
    private String deviceKey;

    /** 设备密钥（列优先；亦可与 extra 并存，以迁移阶段代码为准） */
    private String deviceSecret;

    /** 认证类型 */
    private String authType;

    /** 子系统代码 */
    private String subsystemCode;

    @TableField(typeHandler = BooleanToIntTypeHandler.class)
    private Boolean subsystemOverride;

    /** 关联菜单 ID 列表 JSON */
    private String menuIds;

    private Long primaryMenuId;

    @TableField(typeHandler = BooleanToIntTypeHandler.class)
    private Boolean menuOverride;

    /** DXF 实体 ID */
    private String dxfEntityId;

    /** 数值型设备类型（网关/物模型兼容） */
    private Integer deviceType;

    @TableField(typeHandler = LongSetTypeHandler.class)
    private Set<Long> groupIds;

    /** 专业分组码：SA/ST/SB/SE/SF/GW */
    private String groupCode;

    /** 系统码：VI/AC/BA/... */
    private String systemCode;

    /** 设备类型码：CAM/NVR/CTR/SENSOR/... */
    private String deviceTypeCode;

    /** 产品型号，例如 DS-2CD3T26 */
    private String productModel;

    /** 品牌码，例如 HIK/DAH/ZKT/JOH */
    private String brand;

    /** 接入类型：IP/RS485/韦根/无线/模拟量/开关量 */
    private String accessType;

    /** IP 地址 */
    private String ip;

    /** 接入协议：ONVIF/GB28181/Modbus TCP/BACnet/MQTT 等 */
    private String protocol;

    /** 设备序列号 */
    private String sn;

    /** ProductKey（对接云平台等场景） */
    private String productKey;

    /** 关联 ibms_product.id */
    private Long ibmsProductId;

    /** 通道总数 */
    private Integer pointCount;

    /** 在线通道数 */
    private Integer pointsOnline;

    /** 告警通道数 */
    private Integer pointsAlarm;

    /** 空间位置展示文案，例如 F01 大堂 */
    private String space;

    /**
     * 扩展字段 JSON（字符串存储）。
     * <p>
     * 接入参数（IP 类设备）：建议写入 {@code ip}/{@code host}、{@code tcpPort}/{@code port}、{@code username}、{@code password} 等，
     * 与 IBMS 设备编辑页「接入参数」保存结构一致；网关 RPC 与 NVR 自动登录均优先读此 JSON。
     * </p>
     * <p>
     * 运行态（由 Biz 消费者写入，勿手改）：{@code gatewayRuntimeState}、{@code gatewayRuntimeAt}。
     * </p>
     */
    private String extra;

    public Long getId() {
        return id;
    }
}

