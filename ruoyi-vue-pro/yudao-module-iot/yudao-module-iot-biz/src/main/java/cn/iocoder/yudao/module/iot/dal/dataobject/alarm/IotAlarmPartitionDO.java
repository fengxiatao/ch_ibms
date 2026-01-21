package cn.iocoder.yudao.module.iot.dal.dataobject.alarm;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 报警主机分区 DO
 *
 * @author 芋道源码
 */
@TableName("iot_alarm_partition")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAlarmPartitionDO extends BaseDO {

    /**
     * 分区ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报警主机ID
     */
    private Long hostId;

    /**
     * 分区编号
     */
    private Integer partitionNo;

    /**
     * 分区名称
     */
    private String partitionName;

    /**
     * 布防状态：0-撤防，1-布防
     */
    private Integer status;

    /**
     * 分区描述
     */
    private String description;

}
