package cn.iocoder.yudao.module.iot.controller.admin.device.vo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IoT 设备服务调用日志 - 响应 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备服务调用日志 Response VO")
@Data
public class IotDeviceServiceLogRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "产品ID", example = "1")
    private Long productId;

    @Schema(description = "产品标识", example = "camera_bullet")
    private String productKey;

    @Schema(description = "设备名称", example = "device_192_168_1_202")
    private String deviceName;

    @Schema(description = "服务标识符", example = "Snapshot")
    private String serviceIdentifier;

    @Schema(description = "服务名称", example = "抓拍")
    private String serviceName;

    @Schema(description = "请求ID", example = "uuid")
    private String requestId;

    @Schema(description = "请求参数", example = "{\"quality\":\"high\"}")
    private String requestParams;

    @Schema(description = "请求时间")
    private LocalDateTime requestTime;

    @Schema(description = "响应状态码", example = "200")
    private Integer statusCode;

    @Schema(description = "响应消息", example = "快照URI获取成功")
    private String responseMessage;

    @Schema(description = "响应数据", example = "{\"snapshotUri\":\"http://...\"}")
    private String responseData;

    @Schema(description = "响应时间")
    private LocalDateTime responseTime;

    @Schema(description = "执行耗时（毫秒）", example = "256")
    private Long executionTime;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人名称", example = "admin")
    private String operatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}












