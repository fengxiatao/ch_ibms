package cn.iocoder.yudao.module.iot.dal.dataobject.changhui;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 长辉设备数据采集 DO
 * 
 * <p>存储设备上报的各类指标数据，包括水位、流量、流速、闸位、温度、渗压、荷载等
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@TableName("changhui_data")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChanghuiDataDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 测站编码
     */
    private String stationCode;

    /**
     * 指标类型
     * 
     * <p>waterLevel-水位(m), instantFlow-瞬时流量(L/s), instantVelocity-瞬时流速(m/s),
     * cumulativeFlow-累计流量(m³), gatePosition-闸位(mm), temperature-温度(°C),
     * seepagePressure-渗压(kPa), load-荷载(kN)
     * @see cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiIndicatorConstants
     */
    private String indicator;

    /**
     * 数值
     */
    private BigDecimal value;

    /**
     * 采集时间/数据时间戳
     */
    private LocalDateTime timestamp;

}
