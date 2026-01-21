package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门禁人员凭证 DO
 *
 * @author 芋道源码
 */
@TableName("iot_access_person_credential")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessPersonCredentialDO extends TenantBaseDO {

    /**
     * 凭证ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 凭证类型：PASSWORD-密码，CARD-卡片，FINGERPRINT-指纹，FACE-人脸
     */
    private String credentialType;

    /**
     * 凭证数据（密码加密存储，卡号明文，指纹/人脸为文件路径）
     */
    private String credentialData;

    /**
     * 卡号（仅卡片类型）
     */
    private String cardNo;

    /**
     * 指纹序号（仅指纹类型，0-9）
     */
    private Integer fingerIndex;

    /**
     * 指纹名称（如：右手食指）
     */
    private String fingerName;

    /**
     * 发卡时间
     */
    private LocalDateTime issueTime;

    /**
     * 换卡时间（最近一次换卡）
     */
    private LocalDateTime replaceTime;

    /**
     * 卡状态：0-正常，1-挂失，2-注销
     */
    private Integer cardStatus;

    /**
     * 旧卡号（换卡时记录）
     */
    private String oldCardNo;

    /**
     * 是否已同步到设备
     */
    private Boolean deviceSynced;

    /**
     * 同步时间
     */
    private LocalDateTime syncTime;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

}
