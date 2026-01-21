package cn.iocoder.yudao.module.iot.service.device.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.service.IotDeviceServiceInvokeReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.service.IotDeviceServiceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceServiceLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceServiceLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * IoT 设备服务调用 Service 实现
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class IotDeviceServiceInvokeServiceImpl implements IotDeviceServiceInvokeService {

    /**
     * 服务调用请求缓存
     * requestId -> serviceLogId
     */
    private final Map<String, Long> requestLogMap = new ConcurrentHashMap<>();

    @Resource
    private IotDeviceMapper deviceMapper;

    @Resource
    private IotDeviceServiceLogMapper serviceLogMapper;


    @Override
    public String invokeDeviceService(IotDeviceServiceInvokeReqVO reqVO) {
        log.info("[invokeDeviceService] 调用设备服务: deviceId={}, service={}", 
                reqVO.getDeviceId(), reqVO.getServiceIdentifier());

        // 1. 验证设备是否存在
        IotDeviceDO device = deviceMapper.selectById(reqVO.getDeviceId());
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 2. 生成请求ID
        String requestId = IdUtil.fastSimpleUUID();

        // 3. 获取操作人信息
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        String operatorName = SecurityFrameworkUtils.getLoginUserNickname();

        // 4. 创建服务调用日志
        IotDeviceServiceLogDO serviceLog = IotDeviceServiceLogDO.builder()
                .deviceId(device.getId())
                .productId(device.getProductId())
                .productKey(device.getProductKey())
                .deviceName(device.getDeviceName())
                .serviceIdentifier(reqVO.getServiceIdentifier())
                .serviceName(reqVO.getServiceName())
                .requestId(requestId)
                .requestParams(JSONUtil.toJsonStr(reqVO.getServiceParams()))
                .requestTime(LocalDateTime.now())
                .operatorId(operatorId)
                .operatorName(operatorName)
                .build();

        serviceLogMapper.insert(serviceLog);

        // 5. 缓存请求ID与日志ID的映射
        requestLogMap.put(requestId, serviceLog.getId());

        // 6. 已移除 ONVIF 服务调用请求发布，保留日志与请求ID返回
        log.info("[invokeDeviceService] (占位) 已记录服务调用: requestId={}, deviceId={}, service={}",
                requestId, device.getId(), reqVO.getServiceIdentifier());

        return requestId;
    }

    @Override
    public PageResult<IotDeviceServiceLogDO> getServiceLogPage(IotDeviceServiceLogPageReqVO pageReqVO) {
        return serviceLogMapper.selectPage(pageReqVO);
    }

    @Override
    public IotDeviceServiceLogDO getServiceLog(Long id) {
        return serviceLogMapper.selectById(id);
    }

    @Override
    public void handleServiceResult(String requestId, Integer statusCode, String responseMessage, 
                                    String responseData, Long responseTime) {
        log.info("[handleServiceResult] 处理服务调用结果: requestId={}, statusCode={}", 
                requestId, statusCode);

        // 1. 查找对应的服务日志
        Long serviceLogId = requestLogMap.get(requestId);
        if (serviceLogId == null) {
            log.warn("[handleServiceResult] 未找到对应的服务日志: requestId={}", requestId);
            return;
        }

        IotDeviceServiceLogDO serviceLog = serviceLogMapper.selectById(serviceLogId);
        if (serviceLog == null) {
            log.warn("[handleServiceResult] 服务日志不存在: serviceLogId={}", serviceLogId);
            return;
        }

        // 2. 更新服务日志
        LocalDateTime responseDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(responseTime), 
                ZoneId.systemDefault()
        );
        
        serviceLog.setStatusCode(statusCode);
        serviceLog.setResponseMessage(responseMessage);
        serviceLog.setResponseData(responseData);
        serviceLog.setResponseTime(responseDateTime);
        
        // 计算执行耗时
        long executionTime = responseTime - 
                serviceLog.getRequestTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        serviceLog.setExecutionTime(executionTime);
        
        serviceLogMapper.updateById(serviceLog);

        // 3. 移除缓存
        requestLogMap.remove(requestId);

        log.info("[handleServiceResult] 服务调用结果已更新: requestId={}, executionTime={}ms", 
                requestId, executionTime);
    }
}












