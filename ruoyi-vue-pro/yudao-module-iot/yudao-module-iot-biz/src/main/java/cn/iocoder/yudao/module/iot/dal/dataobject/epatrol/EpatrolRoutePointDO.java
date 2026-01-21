package cn.iocoder.yudao.module.iot.dal.dataobject.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 电子巡更 - 路线点位关联 DO
 *
 * @author 长辉信息
 */
@TableName("iot_epatrol_route_point")
@KeySequence("iot_epatrol_route_point_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpatrolRoutePointDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 路线ID
     */
    private Long routeId;

    /**
     * 点位ID
     */
    private Long pointId;

    /**
     * 顺序（从1开始）
     */
    private Integer sort;

    /**
     * 到下一个点位的间隔时间（分钟）
     */
    private Integer intervalMinutes;

}
