package cn.iocoder.yudao.module.iot.dal.dataobject.changhui;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 长辉设备 DO
 * 
 * <p>统一管理长辉、德通等使用相同TCP协议的设备
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@TableName("changhui_device")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChanghuiDeviceDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 测站编码（唯一）
     * 
     * <p>10字节：行政区代码(2B) + 管理处代码(1B) + 站所代码(1B) + 桩号(3B) + 设备类型(1B) + 设备厂家(1B) + 顺序编号(1B)
     */
    private String stationCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     * 
     * <p>1-测控一体化闸门,2-测控分体式闸门,3-退水闸,4-节制闸,5-进水闸,6-水位计,7-流量计,8-流速仪,9-渗压计
     * @see cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiDeviceTypeEnum
     */
    private Integer deviceType;

    /**
     * 行政区代码（省编码）
     */
    private String provinceCode;

    /**
     * 管理处代码（管理单位编码）
     */
    private String managementCode;

    /**
     * 站所代码（测站编码部分）
     */
    private String stationCodePart;

    /**
     * 桩号（前）
     */
    private String pileFront;

    /**
     * 桩号（后）
     */
    private String pileBack;

    /**
     * 设备厂家/制造商
     */
    private String manufacturer;

    /**
     * 顺序编号/序列号
     */
    private String sequenceNo;

    /**
     * TEA加密密钥（JSON数组格式，如 [1234567890, 1234567890, 1234567890, 1234567890]）
     */
    private String teaKey;

    /**
     * 设备密码
     */
    private String password;

    /**
     * 状态：0-离线,1-在线
     */
    private Integer status;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

}
