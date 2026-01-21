package cn.iocoder.yudao.module.iot.job.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.quartz.core.enums.JobBusinessType;
import cn.iocoder.yudao.framework.quartz.core.enums.JobPriority;
import cn.iocoder.yudao.module.iot.framework.job.IotBusinessJobHandler;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT 设备数据采集任务
 * 
 * <p>功能描述：
 * 定时采集设备的传感器数据、状态数据等
 * 支持按设备ID、产品ID或全部在线设备进行采集
 * 
 * <p>任务参数格式（JSON）：
 * <pre>
 * {
 *   "deviceIds": [1001, 1002, 1003],  // 可选：指定设备ID列表
 *   "productId": 101,                  // 可选：指定产品ID，采集该产品下所有在线设备
 *   "batchSize": 50                    // 可选：批处理大小，默认50
 * }
 * </pre>
 * 
 * <p>如果不提供参数或参数为空，则采集所有在线设备
 * 
 * <p>执行频率：建议每 10 分钟执行一次
 * <p>优先级：NORMAL（普通优先级）
 * <p>冲突策略：SKIP（如果上次采集还在进行，跳过本次）
 * <p>并发支持：支持并发（可以同时采集不同批次的设备）
 * 
 * @author IBMS Team
 */
@Component
@Slf4j
public class IotDeviceDataCollectJob extends IotBusinessJobHandler {
    
    @Resource
    private IotDeviceService deviceService;
    
    @Override
    public JobBusinessType getBusinessType() {
        return JobBusinessType.IOT_DEVICE_DATA_COLLECT;
    }
    
    @Override
    public int getPriority() {
        return JobPriority.NORMAL; // 普通优先级
    }
    
    @Override
    public boolean isConcurrent() {
        return true; // 支持并发执行（不同批次的设备可以并发采集）
    }
    
    @Override
    public Integer getMaxConcurrentCount() {
        return 3; // 最多同时执行3个采集任务
    }
    
    @Override
    protected String doExecute(String param) throws Exception {
        // 1. 解析任务参数
        JobParam jobParam = parseParam(param);
        log.info("任务参数: {}", jobParam);
        
        // 2. 获取需要采集数据的设备列表
        List<Long> deviceIds = getTargetDevices(jobParam);
        
        if (CollUtil.isEmpty(deviceIds)) {
            return JsonUtils.toJsonString(Map.of(
                "message", "无需采集的设备",
                "totalCount", 0
            ));
        }
        
        log.info("准备采集 {} 个设备的数据", deviceIds.size());
        
        // 3. 分批采集设备数据（避免一次处理太多设备）
        int batchSize = jobParam.getBatchSize() != null ? jobParam.getBatchSize() : 50;
        int totalCount = deviceIds.size();
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> failedDevices = new ArrayList<>();
        
        // 分批处理
        for (int i = 0; i < totalCount; i += batchSize) {
            int end = Math.min(i + batchSize, totalCount);
            List<Long> batchIds = deviceIds.subList(i, end);
            
            log.info("采集第 {}/{} 批，设备数: {}", (i / batchSize + 1), 
                     (totalCount + batchSize - 1) / batchSize, batchIds.size());
            
            // 逐个采集设备数据
            for (Long deviceId : batchIds) {
                try {
                    // 调用设备服务采集数据
                    // TODO: 需要在 IotDeviceService 中实现 collectDeviceData 方法
                    // deviceService.collectDeviceData(deviceId);
                    // 运行态禁止“模拟采集成功”：未实现则按失败处理，避免产生误导数据
                    throw new UnsupportedOperationException("collectDeviceData 未实现");
                    
                } catch (Exception e) {
                    failCount++;
                    log.error("采集设备数据失败: deviceId={}, 错误: {}", deviceId, e.getMessage());
                    
                    // 记录失败的设备信息
                    failedDevices.add(Map.of(
                        "deviceId", deviceId,
                        "error", e.getMessage()
                    ));
                }
            }
        }
        
        // 4. 返回执行结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("successRate", totalCount > 0 ? 
                   String.format("%.2f%%", (double) successCount / totalCount * 100) : "0%");
        
        // 只返回失败的设备（避免结果太大）
        if (!failedDevices.isEmpty()) {
            result.put("failedDevices", failedDevices.size() > 10 ? 
                       failedDevices.subList(0, 10) : failedDevices);
            if (failedDevices.size() > 10) {
                result.put("note", "仅显示前10个失败设备");
            }
        }
        
        return JsonUtils.toJsonString(result);
    }
    
    /**
     * 解析任务参数
     * 
     * @param param JSON格式的参数字符串
     * @return 解析后的参数对象
     */
    private JobParam parseParam(String param) {
        if (StrUtil.isBlank(param)) {
            return new JobParam(); // 返回默认参数
        }
        
        try {
            return JsonUtils.parseObject(param, JobParam.class);
        } catch (Exception e) {
            log.warn("参数解析失败，使用默认参数: param={}, error={}", param, e.getMessage());
            return new JobParam();
        }
    }
    
    /**
     * 获取目标设备列表
     * 
     * @param param 任务参数
     * @return 设备ID列表
     */
    private List<Long> getTargetDevices(JobParam param) {
        // 1. 优先使用指定的设备ID列表
        if (CollUtil.isNotEmpty(param.getDeviceIds())) {
            log.info("使用指定的设备ID列表，数量: {}", param.getDeviceIds().size());
            return param.getDeviceIds();
        }
        
        // 2. 如果指定了产品ID，获取该产品下的所有在线设备
        if (param.getProductId() != null) {
            log.info("采集产品ID为 {} 的所有在线设备", param.getProductId());
            // TODO: 需要在 IotDeviceService 中实现 getOnlineDeviceIdsByProduct 方法
            // return deviceService.getOnlineDeviceIdsByProduct(param.getProductId());
            
            // 这里先返回空列表
            return new ArrayList<>();
        }
        
        // 3. 默认采集所有在线设备
        log.info("采集所有在线设备");
        // TODO: 需要在 IotDeviceService 中实现 getAllOnlineDeviceIds 方法
        // return deviceService.getAllOnlineDeviceIds();
        
        // 这里先返回空列表
        return new ArrayList<>();
    }
    
    /**
     * 任务参数
     */
    @Data
    private static class JobParam {
        /**
         * 指定设备ID列表
         * 如果指定了，则只采集这些设备的数据
         */
        private List<Long> deviceIds;
        
        /**
         * 指定产品ID
         * 如果指定了，则采集该产品下所有在线设备的数据
         */
        private Long productId;
        
        /**
         * 批处理大小
         * 默认 50，即每次采集 50 个设备的数据
         */
        private Integer batchSize;
    }
}

