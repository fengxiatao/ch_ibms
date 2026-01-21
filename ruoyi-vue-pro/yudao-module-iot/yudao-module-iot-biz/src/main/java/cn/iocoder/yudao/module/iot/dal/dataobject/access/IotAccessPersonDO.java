package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门禁人员 DO
 *
 * @author 芋道源码
 */
@TableName("iot_access_person")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessPersonDO extends TenantBaseDO {

    /**
     * 人员ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 人员编号
     */
    private String personCode;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 人员类型：1-员工，2-访客，3-临时人员
     */
    private Integer personType;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 人脸照片URL
     */
    private String faceUrl;

    /**
     * 有效期开始
     */
    private LocalDateTime validStart;

    /**
     * 有效期结束
     */
    private LocalDateTime validEnd;

    /**
     * 状态：0-正常，1-停用，2-过期
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
