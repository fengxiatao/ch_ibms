package cn.iocoder.yudao.module.iot.dal.dataobject.ibms;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IBMS 产品定义 DO
 *
 * 对应表：ibms_product
 *
 * 编码段说明：
 * - productCode：{系统}-{型号码}-{设备类型}-{品牌}-{流水}，例 VI-NV-NVR-DAH-001
 * - groupCode：专业分组码，ibms_group.value
 * - systemCode：系统码，ibms_system.value
 * - modelCode：型号码，ibms_device_model.value
 * - deviceTypeCode：设备类型码，ibms_device_type.value
 *
 * @author
 */
@TableName(value = "ibms_product", autoResultMap = true)
@KeySequence("ibms_product_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsProductDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 产品编码：VI-NV-NVR-DAH-001
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 专业分组码：SA/ST/SB/SE/SF
     */
    private String groupCode;

    /**
     * 系统码：VI/AC/BA/...
     */
    private String systemCode;

    /**
     * 型号码：DS/DP/...
     */
    private String modelCode;

    /**
     * 设备类型码：CAM/READER/...
     */
    private String deviceTypeCode;

    /**
     * 厂商品牌
     */
    private String manufacturer;

    /**
     * 产品型号
     */
    private String modelNumber;

    /**
     * 通信协议，如 ONVIF/Modbus/GB28181
     */
    private String protocol;

    /**
     * 图标 class，如 fa-video
     */
    private String icon;

    /**
     * 展示颜色（冗余自 ibms_group.remark.color）
     */
    private String color;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 扩展字段 JSON（字符串存储）
     */
    private String extra;
}

