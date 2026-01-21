package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 设备服务调用日志 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName(value = "iot_device_service_log", autoResultMap = true)
@KeySequence("iot_device_service_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceServiceLogDO extends BaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;

    /**
     * 设备编号
     */
    private Long deviceId;

    /**
     * 产品编号
     */
    private Long productId;

    /**
     * 产品标识
     */
    private String productKey;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 服务标识符（对应物模型）
     */
    private String serviceIdentifier;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 响应状态码（200-成功，其他-失败）
     */
    private Integer statusCode;

    /**
     * 响应消息
     */
    private String responseMessage;

    /**
     * 响应数据（JSON格式）
     */
    private String responseData;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;
}












