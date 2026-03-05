package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 环境监测数据记录 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_env_data_record")
@KeySequence("ibms_env_data_record_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsEnvDataRecordDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 传感器ID
     */
    private Long sensorId;

    /**
     * 传感器编号
     */
    private String sensorCode;

    /**
     * 温度(℃)
     */
    private BigDecimal temperature;

    /**
     * 湿度(%RH)
     */
    private BigDecimal humidity;

    /**
     * PM2.5(μg/m³)
     */
    private BigDecimal pm25;

    /**
     * PM10(μg/m³)
     */
    private BigDecimal pm10;

    /**
     * CO2浓度(ppm)
     */
    private BigDecimal co2;

    /**
     * 甲醛(mg/m³)
     */
    private BigDecimal formaldehyde;

    /**
     * 光照度(lux)
     */
    private BigDecimal illuminance;

    /**
     * 噪音(dB)
     */
    private BigDecimal noise;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}
