package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 访客黑名单 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_visitor_blacklist", autoResultMap = true)
@KeySequence("iot_visitor_blacklist_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorBlacklistDO extends BaseDO {

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
     * 拉黑原因
     */
    private String blackReason;

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


























