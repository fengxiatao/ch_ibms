package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 访客信息 DO
 *
 * @author 芋道源码
 */
@TableName("iot_visitor")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVisitorDO extends TenantBaseDO {

    /**
     * 访客ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 访客编号（系统生成）
     */
    private String visitorCode;

    /**
     * 访客姓名
     */
    private String visitorName;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 所属单位
     */
    private String company;

    /**
     * 人脸照片URL
     */
    private String faceUrl;

    /**
     * 备注
     */
    private String remark;

}
