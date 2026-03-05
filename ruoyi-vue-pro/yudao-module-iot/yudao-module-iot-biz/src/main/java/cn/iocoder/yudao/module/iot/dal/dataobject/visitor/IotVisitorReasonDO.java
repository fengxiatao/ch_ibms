package cn.iocoder.yudao.module.iot.dal.dataobject.visitor;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 访客来访事由 DO
 *
 * @author 芋道源码
 */
@TableName("iot_visitor_reason")
@KeySequence("iot_visitor_reason_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVisitorReasonDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 来访事由名称
     */
    private String reasonName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0-正常 1-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
