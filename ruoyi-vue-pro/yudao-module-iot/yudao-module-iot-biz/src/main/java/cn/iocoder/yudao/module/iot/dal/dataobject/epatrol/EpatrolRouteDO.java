package cn.iocoder.yudao.module.iot.dal.dataobject.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 电子巡更 - 巡更路线 DO
 *
 * @author 长辉信息
 */
@TableName("iot_epatrol_route")
@KeySequence("iot_epatrol_route_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpatrolRouteDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 路线名称
     */
    private String routeName;

    /**
     * 包含巡更点数量
     */
    private Integer pointCount;

    /**
     * 路线总耗时（分钟）
     */
    private Integer totalDuration;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
