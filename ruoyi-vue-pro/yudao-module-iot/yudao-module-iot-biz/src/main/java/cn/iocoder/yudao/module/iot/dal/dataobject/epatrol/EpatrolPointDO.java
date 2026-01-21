package cn.iocoder.yudao.module.iot.dal.dataobject.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 电子巡更 - 巡更点 DO
 *
 * @author 长辉信息
 */
@TableName("iot_epatrol_point")
@KeySequence("iot_epatrol_point_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpatrolPointDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 点位编号（硬件编号）
     */
    private String pointNo;

    /**
     * 点位名称
     */
    private String pointName;

    /**
     * 点位位置
     */
    private String pointLocation;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
