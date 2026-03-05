package cn.iocoder.yudao.module.iot.dal.dataobject.parking;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 停车场黑名单 DO
 *
 * @author 芋道源码
 */
@TableName("iot_parking_blacklist")
@KeySequence("iot_parking_blacklist_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingBlacklistDO extends TenantBaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 拉黑原因
     */
    private String reason;

    /**
     * 黑名单结束时间
     */
    private LocalDateTime endTime;

    /**
     * 适用车场ID（null表示所有车场）
     */
    private Long lotId;

    /**
     * 状态：0-生效中，1-已解除
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
