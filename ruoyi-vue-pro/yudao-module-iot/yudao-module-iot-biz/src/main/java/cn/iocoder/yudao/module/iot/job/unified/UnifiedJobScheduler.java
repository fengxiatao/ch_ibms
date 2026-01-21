package cn.iocoder.yudao.module.iot.job.unified;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.quartz.core.enums.JobBusinessType;
import cn.iocoder.yudao.framework.quartz.core.enums.JobPriority;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.framework.job.IotBusinessJobHandler;
import cn.iocoder.yudao.module.iot.job.unified.config.JobConfig;
import cn.iocoder.yudao.module.iot.job.unified.config.JobTaskConfig;
import cn.iocoder.yudao.module.iot.job.unified.executor.JobExecutor;
import cn.iocoder.yudao.module.iot.job.unified.model.JobExecutionRecord;
import cn.iocoder.yudao.module.iot.job.unified.model.ScheduledTask;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.job.JobExecutionRecordService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一任务调度器
 * 
 * <p>核心职责：
 * <ul>
 *   <li>读取所有实体（产品、设备、专业系统、空间设施）的 jobConfig</li>
 *   <li>根据配置的间隔时间，判断是否需要执行</li>
 *   <li>调用对应的执行器执行任务</li>
 *   <li>记录执行结果和下次执行时间</li>
 * </ul>
 * 
 * <p>设计思路：
 * <ul>
 *   <li>一个调度器，多个执行器（OfflineCheckExecutor, HealthCheckExecutor等）</li>
 *   <li>支持产品级配置（应用到该产品下所有设备）</li>
 *   <li>支持设备级配置（覆盖产品配置）</li>
 *   <li>支持优先级调度</li>
 * </ul>
 * 
 * @author IBMS Team
 */
@Component
@Slf4j
public class UnifiedJobScheduler extends IotBusinessJobHandler {
    
    @Resource
    private IotProductService productService;
    
    @Resource
    private IotDeviceService deviceService;
    
    @Resource
    private JobExecutionRecordService executionRecordService;
    
    // 空间设施Service
    @Resource
    private cn.iocoder.yudao.module.iot.service.gis.CampusService campusService;
    
    @Resource
    private cn.iocoder.yudao.module.iot.service.gis.BuildingService buildingService;
    
    @Resource
    private cn.iocoder.yudao.module.iot.service.gis.IotGisFloorService floorService;
    
    @Resource
    private cn.iocoder.yudao.module.iot.service.gis.AreaService areaService;
    
    // 任务执行器映射
    private final Map<String, JobExecutor> executors = new ConcurrentHashMap<>();
    
    /**
     * 注册任务执行器
     */
    public UnifiedJobScheduler() {
        // TODO: 通过Spring自动注入所有 JobExecutor 实现
    }
    
    /**
     * 手动注册执行器（或通过 @PostConstruct 自动注入）
     */
    public void registerExecutor(String jobType, JobExecutor executor) {
        executors.put(jobType, executor);
        log.info("注册任务执行器: {} -> {}", jobType, executor.getClass().getSimpleName());
    }
    
    @Override
    public JobBusinessType getBusinessType() {
        return JobBusinessType.IOT_DEVICE_OFFLINE_CHECK;
    }
    
    @Override
    public int getPriority() {
        return JobPriority.HIGH; // 统一调度器使用高优先级
    }
    
    @Override
    protected String doExecute(String param) throws Exception {
        log.info("=== 统一任务调度器开始执行 ===");
        
        long startTime = System.currentTimeMillis();
        Map<String, Integer> statistics = new HashMap<>();
        
        try {
            // 1. 收集所有需要执行的任务
            List<ScheduledTask> tasks = collectScheduledTasks();
            log.info("收集到 {} 个待执行任务", tasks.size());
            
            if (CollUtil.isEmpty(tasks)) {
                return "无待执行任务";
            }
            
            // 2. 按优先级排序
            tasks.sort(Comparator.comparingInt(ScheduledTask::getPriority));
            
            // 3. 执行任务
            for (ScheduledTask task : tasks) {
                try {
                    executeTask(task, statistics);
                } catch (Exception e) {
                    log.error("执行任务失败: {}", task, e);
                    statistics.merge(task.getJobType() + "_error", 1, Integer::sum);
                }
            }
            
            // 4. 统计结果
            long duration = System.currentTimeMillis() - startTime;
            String result = buildResultMessage(statistics, duration);
            
            log.info("=== 统一任务调度器执行完成，耗时: {}ms ===", duration);
            return result;
            
        } catch (Exception e) {
            log.error("统一任务调度器执行异常", e);
            throw e;
        }
    }
    
    /**
     * 收集所有需要执行的任务
     * 
     * <p>优先级规则：
     * <ul>
     *   <li>设备配置优先于产品配置</li>
     *   <li>设备启用任务：无论产品配置如何，都执行设备的配置</li>
     *   <li>设备禁用任务：即使产品启用，也不执行该任务</li>
     *   <li>设备未配置：继承产品的配置（如果产品有配置）</li>
     * </ul>
     */
    private List<ScheduledTask> collectScheduledTasks() {
        // 使用 Map 存储任务，key = entityType:entityId:jobType，确保唯一性
        Map<String, ScheduledTask> taskMap = new LinkedHashMap<>();
        
        // 1. 先收集产品级任务，并应用到该产品下的所有设备
        collectAndApplyProductTasks(taskMap);
        
        // 2. 收集设备级任务，覆盖产品配置
        collectDeviceTasksWithOverride(taskMap);
        
        // 3. 收集空间设施任务（空间设施没有继承关系）
        taskMap.putAll(collectSpatialTasksMap());
        
        // 4. 返回所有任务
        return new ArrayList<>(taskMap.values());
    }
    
    /**
     * 收集产品级任务并应用到设备
     * 
     * <p>对于每个产品配置的任务，将其应用到该产品下的所有设备（设备未单独配置时）
     */
    private void collectAndApplyProductTasks(Map<String, ScheduledTask> taskMap) {
        // 获取所有配置了 jobConfig 的产品
        List<IotProductDO> products = productService.getProductsWithJobConfig();
        
        for (IotProductDO product : products) {
            Long productId = product.getId();
            String jobConfigStr = product.getJobConfig();
            
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }
            
            try {
                // 解析 jobConfig
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);
                
                // 获取该产品下的所有设备
                List<IotDeviceDO> devices = deviceService.getDeviceListByProductId(productId);
                
                if (CollUtil.isEmpty(devices)) {
                    continue;
                }
                
                // 为每个设备应用产品的任务配置
                for (IotDeviceDO device : devices) {
                    Long deviceId = device.getId();
                    
                    // 处理离线检查任务
                    if (jobConfig.getOfflineCheck() != null && jobConfig.getOfflineCheck().isEnabled()) {
                        JobTaskConfig config = jobConfig.getOfflineCheck();
                        String taskKey = buildTaskKey("DEVICE", deviceId, "offlineCheck");
                        
                        if (shouldExecute("DEVICE", deviceId, "offlineCheck", config)) {
                            taskMap.put(taskKey, ScheduledTask.builder()
                                    .entityType("DEVICE")
                                    .entityId(deviceId)
                                    .jobType("offlineCheck")
                                    .config(config)
                                    .priority(config.getPriority())
                                    .fromProduct(true) // 标记为来自产品配置
                                    .build());
                        }
                    }
                    
                    // 处理健康检查任务
                    if (jobConfig.getHealthCheck() != null && jobConfig.getHealthCheck().isEnabled()) {
                        JobTaskConfig config = jobConfig.getHealthCheck();
                        String taskKey = buildTaskKey("DEVICE", deviceId, "healthCheck");
                        
                        if (shouldExecute("DEVICE", deviceId, "healthCheck", config)) {
                            taskMap.put(taskKey, ScheduledTask.builder()
                                    .entityType("DEVICE")
                                    .entityId(deviceId)
                                    .jobType("healthCheck")
                                    .config(config)
                                    .priority(config.getPriority())
                                    .fromProduct(true)
                                    .build());
                        }
                    }
                    
                    // 处理数据采集任务
                    if (jobConfig.getDataCollect() != null && jobConfig.getDataCollect().isEnabled()) {
                        JobTaskConfig config = jobConfig.getDataCollect();
                        String taskKey = buildTaskKey("DEVICE", deviceId, "dataCollect");
                        
                        if (shouldExecute("DEVICE", deviceId, "dataCollect", config)) {
                            taskMap.put(taskKey, ScheduledTask.builder()
                                    .entityType("DEVICE")
                                    .entityId(deviceId)
                                    .jobType("dataCollect")
                                    .config(config)
                                    .priority(config.getPriority())
                                    .fromProduct(true)
                                    .build());
                        }
                    }
                }
                
            } catch (Exception e) {
                log.error("[collectAndApplyProductTasks] 解析产品[{}] jobConfig 失败", productId, e);
            }
        }
    }
    
    /**
     * 收集设备级任务，覆盖产品配置
     * 
     * <p>如果设备有自己的配置（无论启用还是禁用），使用设备的配置替代产品配置
     */
    private void collectDeviceTasksWithOverride(Map<String, ScheduledTask> taskMap) {
        // 获取所有配置了 jobConfig 的设备
        List<IotDeviceDO> devices = deviceService.getDevicesWithJobConfig();
        
        for (IotDeviceDO device : devices) {
            Long deviceId = device.getId();
            String jobConfigStr = device.getJobConfig();
            
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }
            
            try {
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);
                
                // 处理离线检查任务
                if (jobConfig.getOfflineCheck() != null) {
                    JobTaskConfig config = jobConfig.getOfflineCheck();
                    String taskKey = buildTaskKey("DEVICE", deviceId, "offlineCheck");
                    
                    // 如果设备禁用了任务，移除该任务（即使产品启用了）
                    if (!config.isEnabled()) {
                        taskMap.remove(taskKey);
                        log.debug("设备[{}]禁用了离线检查任务，移除产品配置的任务", deviceId);
                    } else {
                        // 设备启用了任务，覆盖产品配置
                        if (shouldExecute("DEVICE", deviceId, "offlineCheck", config)) {
                            taskMap.put(taskKey, ScheduledTask.builder()
                                    .entityType("DEVICE")
                                    .entityId(deviceId)
                                    .jobType("offlineCheck")
                                    .config(config)
                                    .priority(config.getPriority())
                                    .fromProduct(false) // 标记为设备自己的配置
                                    .build());
                        }
                    }
                }
                
                // 处理健康检查任务
                if (jobConfig.getHealthCheck() != null) {
                    JobTaskConfig config = jobConfig.getHealthCheck();
                    String taskKey = buildTaskKey("DEVICE", deviceId, "healthCheck");
                    
                    if (!config.isEnabled()) {
                        taskMap.remove(taskKey);
                        log.debug("设备[{}]禁用了健康检查任务，移除产品配置的任务", deviceId);
                    } else {
                        if (shouldExecute("DEVICE", deviceId, "healthCheck", config)) {
                            taskMap.put(taskKey, ScheduledTask.builder()
                                    .entityType("DEVICE")
                                    .entityId(deviceId)
                                    .jobType("healthCheck")
                                    .config(config)
                                    .priority(config.getPriority())
                                    .fromProduct(false)
                                    .build());
                        }
                    }
                }
                
                // 处理数据采集任务
                if (jobConfig.getDataCollect() != null) {
                    JobTaskConfig config = jobConfig.getDataCollect();
                    String taskKey = buildTaskKey("DEVICE", deviceId, "dataCollect");
                    
                    if (!config.isEnabled()) {
                        taskMap.remove(taskKey);
                        log.debug("设备[{}]禁用了数据采集任务，移除产品配置的任务", deviceId);
                    } else {
                        if (shouldExecute("DEVICE", deviceId, "dataCollect", config)) {
                            taskMap.put(taskKey, ScheduledTask.builder()
                                    .entityType("DEVICE")
                                    .entityId(deviceId)
                                    .jobType("dataCollect")
                                    .config(config)
                                    .priority(config.getPriority())
                                    .fromProduct(false)
                                    .build());
                        }
                    }
                }
                
            } catch (Exception e) {
                log.error("[collectDeviceTasksWithOverride] 解析设备[{}] jobConfig 失败", deviceId, e);
            }
        }
    }
    
    /**
     * 收集空间设施任务（返回Map）
     */
    private Map<String, ScheduledTask> collectSpatialTasksMap() {
        Map<String, ScheduledTask> taskMap = new LinkedHashMap<>();
        
        // 园区任务
        collectCampusTasksToMap(taskMap);
        
        // 建筑任务
        collectBuildingTasksToMap(taskMap);
        
        // 楼层任务
        collectFloorTasksToMap(taskMap);
        
        // 区域任务
        collectAreaTasksToMap(taskMap);
        
        return taskMap;
    }
    
    /**
     * 构建任务唯一键
     */
    private String buildTaskKey(String entityType, Long entityId, String jobType) {
        return entityType + ":" + entityId + ":" + jobType;
    }
    
    /**
     * 收集园区任务到Map
     */
    private void collectCampusTasksToMap(Map<String, ScheduledTask> taskMap) {
        List<cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO> campusList = campusService.getCampusWithJobConfig();

        for (cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO campus : campusList) {
            String jobConfigStr = campus.getJobConfig();
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }

            try {
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);

                // 处理设备统计任务
                if (jobConfig.getDeviceStatistics() != null && jobConfig.getDeviceStatistics().isEnabled()) {
                    JobTaskConfig config = jobConfig.getDeviceStatistics();
                    if (shouldExecute("CAMPUS", campus.getId(), "deviceStatistics", config)) {
                        String taskKey = buildTaskKey("CAMPUS", campus.getId(), "deviceStatistics");
                        taskMap.put(taskKey, ScheduledTask.builder()
                                .entityType("CAMPUS")
                                .entityId(campus.getId())
                                .jobType("deviceStatistics")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }

            } catch (Exception e) {
                log.error("[collectCampusTasksToMap] 解析园区[{}] jobConfig 失败", campus.getId(), e);
            }
        }
    }
    
    /**
     * 收集建筑任务到Map
     */
    private void collectBuildingTasksToMap(Map<String, ScheduledTask> taskMap) {
        List<cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO> buildings = buildingService.getBuildingWithJobConfig();

        for (cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO building : buildings) {
            String jobConfigStr = building.getJobConfig();
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }

            try {
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);

                // 处理能耗统计任务
                if (jobConfig.getEnergyStatistics() != null && jobConfig.getEnergyStatistics().isEnabled()) {
                    JobTaskConfig config = jobConfig.getEnergyStatistics();
                    if (shouldExecute("BUILDING", building.getId(), "energyStatistics", config)) {
                        String taskKey = buildTaskKey("BUILDING", building.getId(), "energyStatistics");
                        taskMap.put(taskKey, ScheduledTask.builder()
                                .entityType("BUILDING")
                                .entityId(building.getId())
                                .jobType("energyStatistics")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }

            } catch (Exception e) {
                log.error("[collectBuildingTasksToMap] 解析建筑[{}] jobConfig 失败", building.getId(), e);
            }
        }
    }
    
    /**
     * 收集楼层任务到Map
     */
    private void collectFloorTasksToMap(Map<String, ScheduledTask> taskMap) {
        List<cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO> floors = floorService.getFloorWithJobConfig();

        for (cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO floor : floors) {
            String jobConfigStr = floor.getJobConfig();
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }

            try {
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);

                // 处理环境监测任务
                if (jobConfig.getEnvMonitor() != null && jobConfig.getEnvMonitor().isEnabled()) {
                    JobTaskConfig config = jobConfig.getEnvMonitor();
                    if (shouldExecute("FLOOR", floor.getId(), "envMonitor", config)) {
                        String taskKey = buildTaskKey("FLOOR", floor.getId(), "envMonitor");
                        taskMap.put(taskKey, ScheduledTask.builder()
                                .entityType("FLOOR")
                                .entityId(floor.getId())
                                .jobType("envMonitor")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }

            } catch (Exception e) {
                log.error("[collectFloorTasksToMap] 解析楼层[{}] jobConfig 失败", floor.getId(), e);
            }
        }
    }
    
    /**
     * 收集区域任务到Map
     */
    private void collectAreaTasksToMap(Map<String, ScheduledTask> taskMap) {
        List<cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO> areas = areaService.getAreaWithJobConfig();

        for (cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO area : areas) {
            String jobConfigStr = area.getJobConfig();
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }

            try {
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);

                // 处理设备巡检任务
                if (jobConfig.getDeviceInspection() != null && jobConfig.getDeviceInspection().isEnabled()) {
                    JobTaskConfig config = jobConfig.getDeviceInspection();
                    if (shouldExecute("AREA", area.getId(), "deviceInspection", config)) {
                        String taskKey = buildTaskKey("AREA", area.getId(), "deviceInspection");
                        taskMap.put(taskKey, ScheduledTask.builder()
                                .entityType("AREA")
                                .entityId(area.getId())
                                .jobType("deviceInspection")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }

            } catch (Exception e) {
                log.error("[collectAreaTasksToMap] 解析区域[{}] jobConfig 失败", area.getId(), e);
            }
        }
    }
    
    // ========== 以下是旧的方法，保留用于向后兼容 ==========
    
    /**
     * 收集产品级任务
     */
    private List<ScheduledTask> collectProductTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        
        // 获取所有配置了 jobConfig 的产品
        List<IotProductDO> products = productService.getProductsWithJobConfig();
        
        for (IotProductDO product : products) {
            Long productId = product.getId();
            String jobConfigStr = product.getJobConfig();
            
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }
            
            try {
                // 解析 jobConfig
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);
                
                // 处理离线检查任务
                if (jobConfig.getOfflineCheck() != null && jobConfig.getOfflineCheck().isEnabled()) {
                    JobTaskConfig config = jobConfig.getOfflineCheck();
                    
                    // 判断是否需要执行
                    if (shouldExecute("PRODUCT", productId, "offlineCheck", config)) {
                        tasks.add(ScheduledTask.builder()
                                .entityType("PRODUCT")
                                .entityId(productId)
                                .jobType("offlineCheck")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }
                
                // 处理健康检查任务
                if (jobConfig.getHealthCheck() != null && jobConfig.getHealthCheck().isEnabled()) {
                    JobTaskConfig config = jobConfig.getHealthCheck();
                    if (shouldExecute("PRODUCT", productId, "healthCheck", config)) {
                        tasks.add(ScheduledTask.builder()
                                .entityType("PRODUCT")
                                .entityId(productId)
                                .jobType("healthCheck")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }
                
                // 处理数据采集任务
                if (jobConfig.getDataCollect() != null && jobConfig.getDataCollect().isEnabled()) {
                    JobTaskConfig config = jobConfig.getDataCollect();
                    if (shouldExecute("PRODUCT", productId, "dataCollect", config)) {
                        tasks.add(ScheduledTask.builder()
                                .entityType("PRODUCT")
                                .entityId(productId)
                                .jobType("dataCollect")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }
                
            } catch (Exception e) {
                log.error("解析产品 {} 的 jobConfig 失败: {}", productId, jobConfigStr, e);
            }
        }
        
        return tasks;
    }
    
    /**
     * 收集设备级任务
     */
    private List<ScheduledTask> collectDeviceTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        
        // 获取所有配置了 jobConfig 的设备
        List<IotDeviceDO> devices = deviceService.getDevicesWithJobConfig();
        
        for (IotDeviceDO device : devices) {
            Long deviceId = device.getId();
            String jobConfigStr = device.getJobConfig();
            
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }
            
            try {
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);
                
                // 设备级配置优先级更高，会覆盖产品级配置
                if (jobConfig.getOfflineCheck() != null && jobConfig.getOfflineCheck().isEnabled()) {
                    JobTaskConfig config = jobConfig.getOfflineCheck();
                    if (shouldExecute("DEVICE", deviceId, "offlineCheck", config)) {
                        tasks.add(ScheduledTask.builder()
                                .entityType("DEVICE")
                                .entityId(deviceId)
                                .jobType("offlineCheck")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }
                
            } catch (Exception e) {
                log.error("解析设备 {} 的 jobConfig 失败", deviceId, e);
            }
        }
        
        return tasks;
    }
    
    /**
     * 判断是否应该执行任务
     */
    private boolean shouldExecute(String entityType, Long entityId, String jobType, JobTaskConfig config) {
        // 查询最后执行记录
        JobExecutionRecord lastRecord = executionRecordService.getLastRecord(entityType, entityId, jobType);
        
        if (lastRecord == null) {
            // 首次执行
            return true;
        }
        
        // 计算下次执行时间
        LocalDateTime nextExecuteTime = lastRecord.getNextExecuteTime();
        if (nextExecuteTime == null) {
            // 没有设置下次执行时间，重新计算
            nextExecuteTime = calculateNextExecuteTime(lastRecord.getLastExecuteTime(), config);
        }
        
        // 判断是否到了执行时间
        return LocalDateTime.now().isAfter(nextExecuteTime);
    }
    
    /**
     * 执行任务
     */
    private void executeTask(ScheduledTask task, Map<String, Integer> statistics) {
        log.info("开始执行任务: {}", task);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取对应的执行器
            JobExecutor executor = executors.get(task.getJobType());
            if (executor == null) {
                log.warn("未找到任务执行器: {}", task.getJobType());
                return;
            }
            
            // 执行任务
            String result = executor.execute(task);
            long duration = System.currentTimeMillis() - startTime;
            
            // 计算下次执行时间
            LocalDateTime nextExecuteTime = calculateNextExecuteTime(LocalDateTime.now(), task.getConfig());
            
            // 记录执行结果
            executionRecordService.recordExecution(
                    task.getEntityType(),
                    task.getEntityId(),
                    task.getJobType(),
                    LocalDateTime.now(),
                    nextExecuteTime,
                    1, // 成功
                    result,
                    (int) duration
            );
            
            // 统计
            statistics.merge(task.getJobType() + "_success", 1, Integer::sum);
            
            log.info("任务执行成功: {}, 耗时: {}ms, 下次执行: {}", 
                    task.getJobType(), duration, nextExecuteTime);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录执行失败
            executionRecordService.recordExecution(
                    task.getEntityType(),
                    task.getEntityId(),
                    task.getJobType(),
                    LocalDateTime.now(),
                    null,
                    2, // 失败
                    "执行失败: " + e.getMessage(),
                    (int) duration
            );
            
            statistics.merge(task.getJobType() + "_error", 1, Integer::sum);
            
            log.error("任务执行失败: {}", task, e);
        }
    }
    
    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextExecuteTime(LocalDateTime lastTime, JobTaskConfig config) {
        int interval = config.getInterval();
        String unit = config.getUnit();
        
        if ("HOUR".equals(unit)) {
            return lastTime.plusHours(interval);
        } else { // MINUTE
            return lastTime.plusMinutes(interval);
        }
    }
    
    /**
     * 构建执行结果消息
     */
    private String buildResultMessage(Map<String, Integer> statistics, long duration) {
        StringBuilder sb = new StringBuilder();
        sb.append("执行完成，耗时: ").append(duration).append("ms\n");
        
        statistics.forEach((key, count) -> {
            sb.append(key).append(": ").append(count).append("\n");
        });
        
        return sb.toString();
    }
    
    /**
     * 收集空间设施任务
     */
    private List<ScheduledTask> collectSpatialTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        
        // 园区任务
        tasks.addAll(collectCampusTasks());
        
        // 建筑任务
        tasks.addAll(collectBuildingTasks());
        
        // 楼层任务
        tasks.addAll(collectFloorTasks());
        
        // 区域任务
        tasks.addAll(collectAreaTasks());
        
        return tasks;
    }
    
    /**
     * 收集园区任务
     */
    private List<ScheduledTask> collectCampusTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        List<cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO> campusList = campusService.getCampusWithJobConfig();
        
        for (cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO campus : campusList) {
            String jobConfigStr = campus.getJobConfig();
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }
            
            try {
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);
                
                // 处理设备统计任务
                if (jobConfig.getDeviceStatistics() != null && jobConfig.getDeviceStatistics().isEnabled()) {
                    JobTaskConfig config = jobConfig.getDeviceStatistics();
                    if (shouldExecute("CAMPUS", campus.getId(), "deviceStatistics", config)) {
                        tasks.add(ScheduledTask.builder()
                                .entityType("CAMPUS")
                                .entityId(campus.getId())
                                .jobType("deviceStatistics")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }
                
            } catch (Exception e) {
                log.error("[collectCampusTasks] 解析园区[{}] jobConfig 失败", campus.getId(), e);
            }
        }
        
        return tasks;
    }
    
    /**
     * 收集建筑任务
     */
    private List<ScheduledTask> collectBuildingTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        List<cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO> buildings = buildingService.getBuildingWithJobConfig();
        
        for (cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO building : buildings) {
            String jobConfigStr = building.getJobConfig();
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }
            
            try {
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);
                
                // 处理能耗统计任务
                if (jobConfig.getEnergyStatistics() != null && jobConfig.getEnergyStatistics().isEnabled()) {
                    JobTaskConfig config = jobConfig.getEnergyStatistics();
                    if (shouldExecute("BUILDING", building.getId(), "energyStatistics", config)) {
                        tasks.add(ScheduledTask.builder()
                                .entityType("BUILDING")
                                .entityId(building.getId())
                                .jobType("energyStatistics")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }
                
            } catch (Exception e) {
                log.error("[collectBuildingTasks] 解析建筑[{}] jobConfig 失败", building.getId(), e);
            }
        }
        
        return tasks;
    }
    
    /**
     * 收集楼层任务
     */
    private List<ScheduledTask> collectFloorTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        List<cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO> floors = floorService.getFloorWithJobConfig();
        
        for (cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO floor : floors) {
            String jobConfigStr = floor.getJobConfig();
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }
            
            try {
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);
                
                // 处理环境监测任务
                if (jobConfig.getEnvMonitor() != null && jobConfig.getEnvMonitor().isEnabled()) {
                    JobTaskConfig config = jobConfig.getEnvMonitor();
                    if (shouldExecute("FLOOR", floor.getId(), "envMonitor", config)) {
                        tasks.add(ScheduledTask.builder()
                                .entityType("FLOOR")
                                .entityId(floor.getId())
                                .jobType("envMonitor")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }
                
            } catch (Exception e) {
                log.error("[collectFloorTasks] 解析楼层[{}] jobConfig 失败", floor.getId(), e);
            }
        }
        
        return tasks;
    }
    
    /**
     * 收集区域任务
     */
    private List<ScheduledTask> collectAreaTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        List<cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO> areas = areaService.getAreaWithJobConfig();
        
        for (cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO area : areas) {
            String jobConfigStr = area.getJobConfig();
            if (StrUtil.isBlank(jobConfigStr)) {
                continue;
            }
            
            try {
                JobConfig jobConfig = JsonUtils.parseObject(jobConfigStr, JobConfig.class);
                
                // 处理设备巡检任务
                if (jobConfig.getDeviceInspection() != null && jobConfig.getDeviceInspection().isEnabled()) {
                    JobTaskConfig config = jobConfig.getDeviceInspection();
                    if (shouldExecute("AREA", area.getId(), "deviceInspection", config)) {
                        tasks.add(ScheduledTask.builder()
                                .entityType("AREA")
                                .entityId(area.getId())
                                .jobType("deviceInspection")
                                .config(config)
                                .priority(config.getPriority())
                                .build());
                    }
                }
                
            } catch (Exception e) {
                log.error("[collectAreaTasks] 解析区域[{}] jobConfig 失败", area.getId(), e);
            }
        }
        
        return tasks;
    }
}

