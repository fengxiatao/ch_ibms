package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 访客白名单 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_visitor_whitelist", autoResultMap = true)
@KeySequence("iot_visitor_whitelist_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorWhitelistDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 访客姓名
     */
    private String visitorName;

    /**
     * 访客证件号
     */
    private String idNumber;

    /**
     * 访客手机号
     */
    private String phone;

    /**
     * 访客类型
     */
    private Integer visitorType;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expiryTime;

    /**
     * 状态
     * 1-启用，2-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}


























