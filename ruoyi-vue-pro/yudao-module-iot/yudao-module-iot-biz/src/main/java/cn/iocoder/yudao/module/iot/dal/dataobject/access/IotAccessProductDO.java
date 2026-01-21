package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 门禁产品 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName(value = "iot_access_product", autoResultMap = true)
@KeySequence("iot_access_product_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessProductDO extends TenantBaseDO {

    /**
     * 产品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品型号（如：DH-ASI7203S-W、DH-ASC2202B-S）
     */
    private String productModel;

    /**
     * 厂商名称
     */
    private String manufacturer;

    /**
     * 设备类型：FINGERPRINT-指纹一体机, FACE-人脸一体机, CONTROLLER-门禁控制器
     */
    private String deviceType;

    /**
     * 设备代数：1-一代设备, 2-二代设备
     */
    private Integer generation;

    /**
     * 是否支持指纹识别
     */
    private Boolean supportFingerprint;

    /**
     * 是否支持人脸识别
     */
    private Boolean supportFace;

    /**
     * 是否支持刷卡
     */
    private Boolean supportCard;

    /**
     * 是否支持密码
     */
    private Boolean supportPassword;

    /**
     * 是否支持二维码
     */
    private Boolean supportQrcode;

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 最大卡片数
     */
    private Integer maxCards;

    /**
     * 最大人脸数
     */
    private Integer maxFaces;

    /**
     * 最大指纹数
     */
    private Integer maxFingerprints;

    /**
     * 最大门数量
     */
    private Integer maxDoors;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean enabled;

}