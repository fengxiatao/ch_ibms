package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 门禁权限组 DO
 *
 * @author 芋道源码
 */
@TableName("iot_access_permission_group")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessPermissionGroupDO extends TenantBaseDO {

    /**
     * 权限组ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限组名称
     */
    private String groupName;

    /**
     * 时间模板ID
     */
    private Long timeTemplateId;

    /**
     * 认证方式：CARD-刷卡，FACE-人脸，FINGERPRINT-指纹，PASSWORD-密码，MULTI-多重认证
     */
    private String authMode;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

}
