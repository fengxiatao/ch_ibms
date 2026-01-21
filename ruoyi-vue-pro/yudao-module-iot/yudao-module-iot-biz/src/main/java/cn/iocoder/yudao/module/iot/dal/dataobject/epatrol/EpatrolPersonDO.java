package cn.iocoder.yudao.module.iot.dal.dataobject.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 电子巡更 - 巡更人员 DO
 *
 * @author 长辉信息
 */
@TableName("iot_epatrol_person")
@KeySequence("iot_epatrol_person_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpatrolPersonDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 人员姓名
     */
    private String name;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 巡更棒编号（硬件编号）
     */
    private String patrolStickNo;

    /**
     * 人员卡编号（硬件编号）
     */
    private String personCardNo;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
