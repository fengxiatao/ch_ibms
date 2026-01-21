package cn.iocoder.yudao.module.iot.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostUpdateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostWithDetailsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition.IotAlarmPartitionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.alarm.IotAlarmHostConvert;
import cn.iocoder.yudao.module.iot.convert.alarm.IotAlarmPartitionConvert;
import cn.iocoder.yudao.module.iot.convert.alarm.IotAlarmZoneConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import cn.iocoder.yudao.module.iot.core.alarm.dto.AlarmHostStatusDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmPartitionDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmHostMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmPartitionMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmZoneMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.alarm.AlarmHostControlResponse;
import cn.iocoder.yudao.module.iot.enums.device.AlarmDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.websocket.IotWebSocketHandler;
import cn.iocoder.yudao.module.iot.websocket.message.AlarmHostStatusMessage;
import cn.iocoder.yudao.module.iot.websocket.message.IotMessage;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ALARM_HOST_NOT_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ALARM_ZONE_NOT_EXISTS;

/**
 * 报警主机 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class IotAlarmHostServiceImpl implements IotAlarmHostService {

    @Resource
    private IotAlarmHostMapper alarmHostMapper;
    
    @Resource
    private IotAlarmZoneMapper alarmZoneMapper;
    
    @Resource
    private IotAlarmPartitionMapper alarmPartitionMapper;
    
    @Resource
    private IotAlarmPartitionService alarmPartitionService;
    
    @Resource
    private IotDeviceService deviceService;
    
    @Resource
    private IotProductService productService;
    
    @Resource
    private IotMessageBus messageBus;
    
    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;
    
    @Resource(name = "iotWebSocketHandler")
    private IotWebSocketHandler webSocketHandler;
    
    @Resource
    private IotAlarmOperationLogService operationLogService;
    
    /**
     * 存储待处理的异步请求
     * Key: requestId
     * Value: CompletableFuture<IotAlarmHostDO>
     */
    private final Map<String, CompletableFuture<IotAlarmHostDO>> pendingRequests = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public Long createAlarmHost(IotAlarmHostCreateReqVO createReqVO) {
        // 1. 先创建基础设备记录
        IotDeviceSaveReqVO deviceReqVO = new IotDeviceSaveReqVO();
        deviceReqVO.setDeviceName(createReqVO.getHostName()); // 使用正确的字段名
        // 设置位置信息
        deviceReqVO.setInstallLocation(createReqVO.getLocation()); // 使用正确的字段名
        
        IotProductDO alarmProduct = productService.validateProductExists("ALARM_HOST_PRODUCT");
        deviceReqVO.setProductId(alarmProduct.getId());
        
        // ⚠️ 重要：设置设备账号，用于Gateway认证
        // 这个account字段是Gateway识别设备的关键
        deviceReqVO.setAccount(createReqVO.getAccount());
        
        // 创建设备并获取设备ID
        Long deviceId = deviceService.createDevice(deviceReqVO);
        
        // 2. 创建报警主机记录
        IotAlarmHostDO alarmHost = IotAlarmHostConvert.INSTANCE.convert(createReqVO);
        // 设置关联的设备ID
        alarmHost.setDeviceId(deviceId);
        // 初始化状态
        alarmHost.setOnlineStatus(0); // 默认离线
        alarmHost.setArmStatus("DISARM"); // 默认撤防
        alarmHost.setAlarmStatus(0); // 默认正常
        alarmHostMapper.insert(alarmHost);
        
        log.info("创建报警主机成功，主机ID: {}, 关联设备ID: {}, account: {}", 
                alarmHost.getId(), deviceId, createReqVO.getAccount());
        
        // 返回报警主机ID
        return alarmHost.getId();
    }

    @Override
    public void updateAlarmHost(IotAlarmHostUpdateReqVO updateReqVO) {
        // 校验存在
        validateAlarmHostExists(updateReqVO.getId());
        // 更新
        IotAlarmHostDO updateObj = IotAlarmHostConvert.INSTANCE.convert(updateReqVO);
        alarmHostMapper.updateById(updateObj);
    }

    @Override
    public void deleteAlarmHost(Long id) {
        // 校验存在
        validateAlarmHostExists(id);
        // 删除
        alarmHostMapper.deleteById(id);
    }

    private void validateAlarmHostExists(Long id) {
        if (alarmHostMapper.selectById(id) == null) {
            throw exception(ALARM_HOST_NOT_EXISTS);
        }
    }

    @Override
    public IotAlarmHostDO getAlarmHost(Long id) {
        return alarmHostMapper.selectById(id);
    }

    @Override
    public PageResult<IotAlarmHostDO> getAlarmHostPage(IotAlarmHostPageReqVO pageReqVO) {
        return alarmHostMapper.selectPage(pageReqVO);
    }

    @Override
    public void quickQuery(String account) {
        // 1. 根据账号查询主机
        IotAlarmHostDO host = getAlarmHostByAccount(account);
        if (host == null) {
            throw exception(ALARM_HOST_NOT_EXISTS);
        }
        
        log.info("[quickQuery][快速查询主机状态] account={}, hostName={}", account, host.getHostName());
        
        // 2. 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put(AlarmDeviceTypeConstants.PARAM_HOST_ID, host.getId());
        params.put(AlarmDeviceTypeConstants.PARAM_ACCOUNT, account);
        params.put(AlarmDeviceTypeConstants.PARAM_PASSWORD, host.getPassword());
        params.put(AlarmDeviceTypeConstants.PARAM_TENANT_ID, TenantContextHolder.getTenantId());
        
        // 3. 使用统一命令发布器发送查询命令
        String requestId = deviceCommandPublisher.publishCommand(
                AlarmDeviceTypeConstants.ALARM,
                host.getDeviceId(),
                AlarmDeviceTypeConstants.COMMAND_QUERY,
                params
        );
        
        log.info("[quickQuery][已发送快速查询命令到Gateway] account={}, requestId={}", 
                account, requestId);
    }

    @Override
    public IotAlarmHostDO armAll(Long id) {
        // 1. 校验主机存在
        IotAlarmHostDO host = alarmHostMapper.selectById(id);
        if (host == null) {
            throw exception(ALARM_HOST_NOT_EXISTS);
        }
        
        log.info("[armAll][开始执行全部布防] hostId={}, account={}, hostName={}", 
                id, host.getAccount(), host.getHostName());
        
        // 2. 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put(AlarmDeviceTypeConstants.PARAM_HOST_ID, id);
        params.put(AlarmDeviceTypeConstants.PARAM_ACCOUNT, host.getAccount());
        params.put(AlarmDeviceTypeConstants.PARAM_PASSWORD, host.getPassword());
        params.put(AlarmDeviceTypeConstants.PARAM_TENANT_ID, TenantContextHolder.getTenantId());
        
        // 3. 使用统一命令发布器发送布防命令
        String requestId = deviceCommandPublisher.publishCommand(
                AlarmDeviceTypeConstants.ALARM,
                host.getDeviceId(),
                AlarmDeviceTypeConstants.COMMAND_ARM_ALL,
                params
        );
        
        log.info("[armAll][已发送全部布防命令到Gateway] hostId={}, account={}, requestId={}", 
                id, host.getAccount(), requestId);
        
        // 4. 等待设备响应并查询最新状态
        return waitAndQueryStatus(id, host.getAccount(), AlarmDeviceTypeConstants.COMMAND_ARM_ALL);
    }

    @Override
    public IotAlarmHostDO armEmergency(Long id) {
        // 1. 校验主机存在
        IotAlarmHostDO host = alarmHostMapper.selectById(id);
        if (host == null) {
            throw exception(ALARM_HOST_NOT_EXISTS);
        }
        
        log.info("[armEmergency][开始执行紧急布防] hostId={}, account={}, hostName={}", 
                id, host.getAccount(), host.getHostName());
        
        // 2. 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put(AlarmDeviceTypeConstants.PARAM_HOST_ID, id);
        params.put(AlarmDeviceTypeConstants.PARAM_ACCOUNT, host.getAccount());
        params.put(AlarmDeviceTypeConstants.PARAM_PASSWORD, host.getPassword());
        params.put(AlarmDeviceTypeConstants.PARAM_TENANT_ID, TenantContextHolder.getTenantId());
        
        // 3. 使用统一命令发布器发送紧急布防命令
        String requestId = deviceCommandPublisher.publishCommand(
                AlarmDeviceTypeConstants.ALARM,
                host.getDeviceId(),
                AlarmDeviceTypeConstants.COMMAND_ARM_EMERGENCY,
                params
        );
        
        log.info("[armEmergency][已发送紧急布防命令到Gateway] hostId={}, account={}, requestId={}", 
                id, host.getAccount(), requestId);
        
        // 4. 等待设备响应并查询最新状态
        return waitAndQueryStatus(id, host.getAccount(), AlarmDeviceTypeConstants.COMMAND_ARM_EMERGENCY);
    }

    @Override
    public IotAlarmHostDO disarm(Long id) {
        // 1. 校验主机存在
        IotAlarmHostDO host = alarmHostMapper.selectById(id);
        if (host == null) {
            throw exception(ALARM_HOST_NOT_EXISTS);
        }
        
        log.info("[disarm][开始执行撤防] hostId={}, account={}, hostName={}", 
                id, host.getAccount(), host.getHostName());
        
        // 2. 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put(AlarmDeviceTypeConstants.PARAM_HOST_ID, id);
        params.put(AlarmDeviceTypeConstants.PARAM_ACCOUNT, host.getAccount());
        params.put(AlarmDeviceTypeConstants.PARAM_PASSWORD, host.getPassword());
        params.put(AlarmDeviceTypeConstants.PARAM_TENANT_ID, TenantContextHolder.getTenantId());
        
        // 3. 使用统一命令发布器发送撤防命令
        String requestId = deviceCommandPublisher.publishCommand(
                AlarmDeviceTypeConstants.ALARM,
                host.getDeviceId(),
                AlarmDeviceTypeConstants.COMMAND_DISARM,
                params
        );
        
        log.info("[disarm][已发送撤防命令到Gateway] hostId={}, account={}, requestId={}", 
                id, host.getAccount(), requestId);
        
        // 4. 等待设备响应并查询最新状态
        return waitAndQueryStatus(id, host.getAccount(), AlarmDeviceTypeConstants.COMMAND_DISARM);
    }

    @Override
    public IotAlarmHostDO clearAlarm(Long id) {
        // 1. 校验主机存在
        IotAlarmHostDO host = alarmHostMapper.selectById(id);
        if (host == null) {
            throw exception(ALARM_HOST_NOT_EXISTS);
        }
        
        log.info("[clearAlarm][开始执行消警] hostId={}, account={}, hostName={}", 
                id, host.getAccount(), host.getHostName());
        
        // 2. 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put(AlarmDeviceTypeConstants.PARAM_HOST_ID, id);
        params.put(AlarmDeviceTypeConstants.PARAM_ACCOUNT, host.getAccount());
        params.put(AlarmDeviceTypeConstants.PARAM_TENANT_ID, TenantContextHolder.getTenantId());
        
        // 3. 使用统一命令发布器发送消警命令
        String requestId = deviceCommandPublisher.publishCommand(
                AlarmDeviceTypeConstants.ALARM,
                host.getDeviceId(),
                AlarmDeviceTypeConstants.COMMAND_CLEAR_ALARM,
                params
        );
        
        log.info("[clearAlarm][发送消警命令] hostId={}, account={}, requestId={}", 
                id, host.getAccount(), requestId);
        
        // 4. 等待设备响应并查询最新状态
        return waitAndQueryStatus(id, host.getAccount(), null);
    }

    /**
     * 等待设备响应并查询最新状态
     *
     * @param id 主机ID
     * @param account 主机账号
     * @param expectedStatus 预期状态（可为null）
     * @return 最新的主机状态
     */
    private IotAlarmHostDO waitAndQueryStatus(Long id, String account, String expectedStatus) {
        try {
            // 1. 等待2秒让设备处理命令
            Thread.sleep(2000);
            
            // 2. 查询主机信息获取 deviceId
            IotAlarmHostDO host = alarmHostMapper.selectById(id);
            if (host == null) {
                throw new RuntimeException("主机不存在: " + id);
            }
            
            // 3. 构建查询状态命令参数
            Map<String, Object> params = new HashMap<>();
            params.put(AlarmDeviceTypeConstants.PARAM_HOST_ID, id);
            params.put(AlarmDeviceTypeConstants.PARAM_ACCOUNT, account);
            params.put(AlarmDeviceTypeConstants.PARAM_TENANT_ID, TenantContextHolder.getTenantId());
            
            // 4. 使用统一命令发布器发送查询状态命令
            String requestId = deviceCommandPublisher.publishCommand(
                    AlarmDeviceTypeConstants.ALARM,
                    host.getDeviceId(),
                    AlarmDeviceTypeConstants.COMMAND_QUERY_STATUS,
                    params
            );
            
            log.info("[waitAndQueryStatus][发送查询状态命令] hostId={}, account={}, requestId={}", id, account, requestId);
            
            // 5. 等待1秒让查询完成
            Thread.sleep(1000);
            
            // 6. 从数据库获取最新状态
            IotAlarmHostDO updatedHost = alarmHostMapper.selectById(id);
            
            log.info("[waitAndQueryStatus][获取最新状态] hostId={}, armStatus={}, alarmStatus={}", 
                    id, updatedHost.getArmStatus(), updatedHost.getAlarmStatus());
            
            return updatedHost;
            
        } catch (InterruptedException e) {
            log.error("[waitAndQueryStatus][等待状态更新失败]", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("操作超时，请稍后重试");
        }
    }

    @Override
    public IotAlarmHostDO getAlarmHostByAccount(String account) {
        return alarmHostMapper.selectOne(new LambdaQueryWrapper<IotAlarmHostDO>()
                .eq(IotAlarmHostDO::getAccount, account));
    }

    @Override
    @TenantIgnore // 忽略租户隔离：RocketMQ 消费者调用时，未传递租户上下文
    public IotAlarmHostDO getAlarmHostByDeviceId(Long deviceId) {
        return alarmHostMapper.selectOne(new LambdaQueryWrapper<IotAlarmHostDO>()
                .eq(IotAlarmHostDO::getDeviceId, deviceId));
    }

    @Override
    @Transactional
    public void updateHostStatus(String account, Integer systemStatus, 
                                  List<AlarmHostStatusDTO.ZoneStatus> zones) {
        // 1. 根据account查找主机
        IotAlarmHostDO host = getAlarmHostByAccount(account);
        if (host == null) {
            log.warn("[updateHostStatus][主机不存在] account={}", account);
            return;
        }

        // 2. 更新主机状态（同时更新 systemStatus 和 armStatus）
        IotAlarmHostDO updateHost = new IotAlarmHostDO();
        updateHost.setId(host.getId());
        updateHost.setSystemStatus(systemStatus);
        updateHost.setLastQueryTime(LocalDateTime.now());
        
        // 根据 systemStatus 设置 armStatus（前端显示用）
        String armStatus = convertSystemStatusToArmStatus(systemStatus);
        updateHost.setArmStatus(armStatus);
        
        alarmHostMapper.updateById(updateHost);

        log.info("[updateHostStatus][更新主机状态] hostId={}, account={}, systemStatus={}, armStatus={}", 
                host.getId(), account, systemStatus, armStatus);

        // 3. 更新防区状态
        if (zones != null && !zones.isEmpty()) {
            for (AlarmHostStatusDTO.ZoneStatus zoneStatus : zones) {
                updateZoneStatus(host.getId(), zoneStatus);
            }
            log.info("[updateHostStatus][更新防区状态完成] hostId={}, zoneCount={}", 
                    host.getId(), zones.size());
        }
    }
    
    @Override
    @Transactional
    public void updateHostStatusWithPartitions(String account, Integer systemStatus, 
                                               List<AlarmHostStatusDTO.PartitionStatus> partitions,
                                               List<AlarmHostStatusDTO.ZoneStatus> zones) {
        // 1. 根据account查找主机
        IotAlarmHostDO host = getAlarmHostByAccount(account);
        if (host == null) {
            log.warn("[updateHostStatusWithPartitions][主机不存在] account={}", account);
            return;
        }

        // 2. 更新主机状态
        IotAlarmHostDO updateHost = new IotAlarmHostDO();
        updateHost.setId(host.getId());
        updateHost.setSystemStatus(systemStatus);
        updateHost.setLastQueryTime(LocalDateTime.now());
        alarmHostMapper.updateById(updateHost);

        log.info("[updateHostStatusWithPartitions][更新主机状态] hostId={}, account={}, systemStatus={}", 
                host.getId(), account, systemStatus);

        // 3. 更新分区状态
        if (partitions != null && !partitions.isEmpty()) {
            for (AlarmHostStatusDTO.PartitionStatus partitionStatus : partitions) {
                alarmPartitionService.updatePartitionStatus(host.getId(), partitionStatus.getPartitionNo(), partitionStatus.getStatus());
            }
            log.info("[updateHostStatusWithPartitions][更新分区状态完成] hostId={}, partitionCount={}", 
                    host.getId(), partitions.size());
        }

        // 4. 更新防区状态
        if (zones != null && !zones.isEmpty()) {
            for (AlarmHostStatusDTO.ZoneStatus zoneStatus : zones) {
                updateZoneStatus(host.getId(), zoneStatus);
            }
            log.info("[updateHostStatusWithPartitions][更新防区状态完成] hostId={}, zoneCount={}", 
                    host.getId(), zones.size());
        }
        
        // 5. 推送 WebSocket 状态更新
        pushHostStatusUpdate(host.getId());
    }

    /**
     * 更新单个防区状态
     */
    private void updateZoneStatus(Long hostId, AlarmHostStatusDTO.ZoneStatus zoneStatus) {
        // 查找或创建防区
        IotAlarmZoneDO zoneDO = alarmZoneMapper.selectOne(
                new LambdaQueryWrapper<IotAlarmZoneDO>()
                        .eq(IotAlarmZoneDO::getHostId, hostId)
                        .eq(IotAlarmZoneDO::getZoneNo, zoneStatus.getZoneNo())
        );

        if (zoneDO == null) {
            // 创建新防区
            zoneDO = new IotAlarmZoneDO();
            zoneDO.setHostId(hostId);
            zoneDO.setZoneNo(zoneStatus.getZoneNo());
            zoneDO.setZoneName("防区" + zoneStatus.getZoneNo());
            zoneDO.setZoneType("UNKNOWN");
            
            // 设置所属分区ID（默认分配到该主机的第一个分区）
            IotAlarmPartitionDO partition = alarmPartitionMapper.selectOne(
                    new LambdaQueryWrapper<IotAlarmPartitionDO>()
                            .eq(IotAlarmPartitionDO::getHostId, hostId)
                            .orderByAsc(IotAlarmPartitionDO::getPartitionNo)
                            .last("LIMIT 1")
            );
            if (partition != null) {
                zoneDO.setPartitionId(partition.getId());
                log.info("[updateZoneStatus][分配防区到分区] hostId={}, zoneNo={}, partitionId={}, partitionNo={}", 
                        hostId, zoneStatus.getZoneNo(), partition.getId(), partition.getPartitionNo());
            } else {
                log.warn("[updateZoneStatus][未找到分区，无法分配防区] hostId={}, zoneNo={}", 
                        hostId, zoneStatus.getZoneNo());
            }
        }

        // 更新状态
        zoneDO.setStatus(String.valueOf(getStatusChar(zoneStatus)));
        zoneDO.setStatusName(getStatusName(zoneStatus));
        zoneDO.setArmStatus(zoneStatus.getStatus());  // 直接使用枚举值：0-撤防，1-布防，2-旁路
        zoneDO.setAlarmStatus(zoneStatus.getAlarmStatus());  // 直接使用枚举值：0-正常，1-报警，11-17各类报警

        // 如果是报警状态，更新报警时间和计数
        if (zoneDO.getAlarmStatus() != null && zoneDO.getAlarmStatus() > 0) {
            zoneDO.setLastAlarmTime(LocalDateTime.now());
            Integer count = zoneDO.getAlarmCount();
            zoneDO.setAlarmCount(count == null ? 1 : count + 1);
        }

        if (zoneDO.getId() == null) {
            alarmZoneMapper.insert(zoneDO);
        } else {
            alarmZoneMapper.updateById(zoneDO);
        }
    }

    /**
     * 获取状态字符（用于显示）
     */
    private char getStatusChar(AlarmHostStatusDTO.ZoneStatus zoneStatus) {
        if (zoneStatus.getStatus() == 0) {
            return 'a'; // 撤防
        } else if (zoneStatus.getStatus() == 2) {
            return 'b'; // 旁路
        } else {
            // 布防状态，根据报警状态返回不同字符
            int alarmStatus = zoneStatus.getAlarmStatus();
            if (alarmStatus == 0) return 'A';
            if (alarmStatus == 1) return 'B';
            if (alarmStatus >= 11 && alarmStatus <= 17) {
                return (char) ('C' + (alarmStatus - 11));
            }
            return 'A';
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(AlarmHostStatusDTO.ZoneStatus zoneStatus) {
        if (zoneStatus.getStatus() == 0) {
            return "防区撤防";
        } else if (zoneStatus.getStatus() == 2) {
            return "防区旁路";
        } else {
            // 布防状态
            int alarmStatus = zoneStatus.getAlarmStatus();
            switch (alarmStatus) {
                case 0: return "防区布防+无报警";  // 修正：添加"+无报警"
                case 1: return "防区布防+正在报警";
                case 11: return "剪断报警";  // 修正：特殊报警不需要"防区布防+"前缀
                case 12: return "短路报警";
                case 13: return "触网报警";
                case 14: return "松弛报警";
                case 15: return "拉紧报警";
                case 16: return "攀爬报警";
                case 17: return "开路报警";
                default: return "防区布防";
            }
        }
    }
    
    /**
     * 延迟查询状态（确保设备状态同步）
     */
    @SuppressWarnings("unused")
    private void scheduleStatusQuery(Long hostId, String account) {
        // 查询主机信息获取 deviceId
        IotAlarmHostDO host = alarmHostMapper.selectById(hostId);
        if (host == null) {
            log.warn("[scheduleStatusQuery][主机不存在] hostId={}", hostId);
            return;
        }
        
        Long deviceId = host.getDeviceId();
        Long tenantId = TenantContextHolder.getTenantId();
        
        // 延迟1秒后查询状态
        CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS)
                .execute(() -> {
                    Map<String, Object> params = new HashMap<>();
                    params.put(AlarmDeviceTypeConstants.PARAM_HOST_ID, hostId);
                    params.put(AlarmDeviceTypeConstants.PARAM_ACCOUNT, account);
                    params.put(AlarmDeviceTypeConstants.PARAM_TENANT_ID, tenantId);
                    
                    String requestId = deviceCommandPublisher.publishCommand(
                            AlarmDeviceTypeConstants.ALARM,
                            deviceId,
                            AlarmDeviceTypeConstants.COMMAND_QUERY_STATUS,
                            params
                    );
                    
                    log.info("[scheduleStatusQuery][发送状态查询命令] hostId={}, account={}, requestId={}", 
                            hostId, account, requestId);
                });
    }
    
    @Override
    @TenantIgnore // 忽略租户隔离：RocketMQ 消费者调用时，未传递租户上下文
    public void updateHostArmStatusByDeviceId(Long deviceId, String armStatus) {
        // 根据 deviceId 查找主机
        IotAlarmHostDO host = alarmHostMapper.selectOne(new LambdaQueryWrapper<IotAlarmHostDO>()
                .eq(IotAlarmHostDO::getDeviceId, deviceId));
        
        if (host == null) {
            log.warn("[updateHostArmStatusByDeviceId][主机不存在] deviceId={}", deviceId);
            return;
        }
        
        // 更新布防状态（并同步 systemStatus，避免前端显示与设备实际不一致）
        IotAlarmHostDO updateObj = new IotAlarmHostDO();
        updateObj.setId(host.getId());
        updateObj.setArmStatus(armStatus);
        // armStatus 可能来自 CID 事件（ARM/DISARM）或业务字段（ARM_ALL/ARM_EMERGENCY）
        // 仅能做到“撤防一定为 0”，ARM 不区分外出/居家时按外出布防处理
        if (armStatus != null) {
            switch (armStatus) {
                case "DISARM":
                    updateObj.setSystemStatus(0);
                    break;
                case "ARM_EMERGENCY":
                    updateObj.setSystemStatus(2);
                    break;
                case "ARM_ALL":
                case "ARM":
                    updateObj.setSystemStatus(1);
                    break;
                default:
                    // 未知状态不覆盖 systemStatus，避免错误回写
                    break;
            }
        }
        alarmHostMapper.updateById(updateObj);
        
        log.info("[updateHostArmStatusByDeviceId][主机状态已更新] hostId={}, deviceId={}, armStatus={}, systemStatus={}",
                host.getId(), deviceId, armStatus, updateObj.getSystemStatus());

        // 推送到前端，确保列表实时刷新（/ws/iot -> alarm_host_status）
        pushHostStatusUpdate(host.getId());
    }
    
    /**
     * 将系统状态（数字）转换为布防状态（字符串）
     * 
     * @param systemStatus 系统状态：0-撤防，1-布防，2-居家布防
     * @return 布防状态："DISARM", "ARM_ALL", "ARM_EMERGENCY"
     */
    private String convertSystemStatusToArmStatus(Integer systemStatus) {
        if (systemStatus == null) {
            return "DISARM";
        }
        
        switch (systemStatus) {
            case 0:
                return "DISARM";          // 撤防
            case 1:
                return "ARM_ALL";         // 全部布防
            case 2:
                return "ARM_EMERGENCY";   // 居家布防（紧急布防）
            default:
                log.warn("[convertSystemStatusToArmStatus][未知的系统状态] systemStatus={}", systemStatus);
                return "DISARM";
        }
    }

    @Override
    public void updateHostName(Long id, String hostName) {
        // 1. 校验主机是否存在
        IotAlarmHostDO host = alarmHostMapper.selectById(id);
        if (host == null) {
            throw exception(ALARM_HOST_NOT_EXISTS);
        }

        // 2. 更新主机名称
        IotAlarmHostDO updateObj = new IotAlarmHostDO();
        updateObj.setId(id);
        updateObj.setHostName(hostName);
        alarmHostMapper.updateById(updateObj);

        log.info("[updateHostName][主机名称已更新] hostId={}, hostName={}", id, hostName);
    }

    @Override
    public void updateZoneName(Long id, String zoneName) {
        // 1. 校验防区是否存在
        IotAlarmZoneDO zone = alarmZoneMapper.selectById(id);
        if (zone == null) {
            throw exception(ALARM_ZONE_NOT_EXISTS);
        }

        // 2. 更新防区名称
        IotAlarmZoneDO updateObj = new IotAlarmZoneDO();
        updateObj.setId(id);
        updateObj.setZoneName(zoneName);
        alarmZoneMapper.updateById(updateObj);

        log.info("[updateZoneName][防区名称已更新] zoneId={}, zoneName={}", id, zoneName);
    }
    
    // ==================== 新增：返回完整状态的操作方法（CompletableFuture + 消息监听） ====================
    
    @Override
    public IotAlarmHostWithDetailsRespVO armAllWithDetails(Long id) {
        return executeControlCommandWithDetails(id, "ARM_ALL", "全部布防");
    }
    
    @Override
    public IotAlarmHostWithDetailsRespVO armEmergencyWithDetails(Long id) {
        return executeControlCommandWithDetails(id, "ARM_EMERGENCY", "紧急布防");
    }
    
    @Override
    public IotAlarmHostWithDetailsRespVO disarmWithDetails(Long id) {
        return executeControlCommandWithDetails(id, "DISARM", "撤防");
    }
    
    @Override
    public IotAlarmHostWithDetailsRespVO clearAlarmWithDetails(Long id) {
        return executeControlCommandWithDetails(id, "CLEAR_ALARM", "消警");
    }
    
    /**
     * 执行控制命令并返回完整状态（乐观更新：先更新数据库状态，再异步发送命令）
     * 
     * @param id 主机ID
     * @param commandType 命令类型
     * @param commandName 命令名称（用于日志）
     * @return 包含主机、分区、防区的完整状态
     */
    private IotAlarmHostWithDetailsRespVO executeControlCommandWithDetails(Long id, String commandType, String commandName) {
        // 1. 校验主机存在
        IotAlarmHostDO host = alarmHostMapper.selectById(id);
        if (host == null) {
            throw exception(ALARM_HOST_NOT_EXISTS);
        }
        
        // 2. 记录操作日志
        String requestId = IdUtil.fastSimpleUUID();
        try {
            operationLogService.logOperation(id, null, null, commandType, null, "SUCCESS", null, requestId);
        } catch (Exception e) {
            log.error("[executeControlCommand][记录操作日志失败] hostId={}, commandType={}", id, commandType, e);
        }
        
        // 3. 【乐观更新】立即更新数据库中的状态（假设命令会成功）
        optimisticUpdateHostAndZones(id, commandType);
        
        // 4. 异步发送控制命令（不等待响应）
        String account = host.getAccount();
        String password = host.getPassword();
        Long deviceId = host.getDeviceId();
        Long tenantId = TenantContextHolder.getTenantId();
        
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put(AlarmDeviceTypeConstants.PARAM_HOST_ID, id);
                params.put(AlarmDeviceTypeConstants.PARAM_ACCOUNT, account);
                params.put(AlarmDeviceTypeConstants.PARAM_PASSWORD, password);
                params.put(AlarmDeviceTypeConstants.PARAM_TENANT_ID, tenantId);
                
                String publishedRequestId = deviceCommandPublisher.publishCommand(
                        AlarmDeviceTypeConstants.ALARM,
                        deviceId,
                        commandType,
                        params
                );
                
                log.info("[executeControlCommand][已发送{}命令] hostId={}, requestId={}", commandName, id, publishedRequestId);
            } catch (Exception e) {
                log.error("[executeControlCommand][发送{}命令失败] hostId={}", commandName, id, e);
            }
        });
        
        // 5. 重新查询更新后的主机状态并返回
        IotAlarmHostDO updatedHost = alarmHostMapper.selectById(id);
        return buildHostWithDetails(updatedHost);
    }
    
    /**
     * 乐观更新主机、分区和防区的状态
     * 
     * @param hostId 主机ID
     * @param commandType 命令类型（ARM_ALL, ARM_EMERGENCY, DISARM, CLEAR_ALARM）
     */
    private void optimisticUpdateHostAndZones(Long hostId, String commandType) {
        // 根据命令类型确定新的状态
        Integer newSystemStatus;
        Integer newPartitionStatus;  // 分区状态（0-撤防，1-布防，2-居家布防）
        Integer newZoneArmStatus;
        String newZoneStatus;
        String newZoneStatusChar;
        String newZoneStatusName;
        
        switch (commandType) {
            case "ARM_ALL":
                newSystemStatus = 1;  // 全部布防
                newPartitionStatus = 1;  // 布防
                newZoneArmStatus = 1; // 布防
                newZoneStatus = "ARM";
                newZoneStatusChar = "A";
                newZoneStatusName = "防区布防+无报警";
                break;
            case "ARM_EMERGENCY":
                newSystemStatus = 2;  // 居家布防
                newPartitionStatus = 2;  // 居家布防
                newZoneArmStatus = 1; // 布防
                newZoneStatus = "ARM";
                newZoneStatusChar = "A";
                newZoneStatusName = "防区布防+无报警";
                break;
            case "DISARM":
                newSystemStatus = 0;  // 撤防
                newPartitionStatus = 0;  // 撤防
                newZoneArmStatus = 0; // 撤防
                newZoneStatus = "DISARM";
                newZoneStatusChar = "a";
                newZoneStatusName = "防区撤防";
                break;
            case "CLEAR_ALARM":
                // 消警不改变布防状态，只清除报警
                clearAlarmStatus(hostId);
                return;
            default:
                log.warn("[optimisticUpdateHostAndZones] 未知的命令类型: {}", commandType);
                return;
        }
        
        // 更新主机状态
        IotAlarmHostDO hostUpdate = new IotAlarmHostDO();
        hostUpdate.setId(hostId);
        hostUpdate.setSystemStatus(newSystemStatus);
        alarmHostMapper.updateById(hostUpdate);
        log.info("[optimisticUpdateHostAndZones] 已更新主机状态: hostId={}, systemStatus={}", hostId, newSystemStatus);
        
        // 更新所有分区状态
        List<IotAlarmPartitionDO> partitions = alarmPartitionMapper.selectList(
            new LambdaQueryWrapper<IotAlarmPartitionDO>()
                .eq(IotAlarmPartitionDO::getHostId, hostId)
        );
        for (IotAlarmPartitionDO partition : partitions) {
            partition.setStatus(newPartitionStatus);
            alarmPartitionMapper.updateById(partition);
        }
        log.info("[optimisticUpdateHostAndZones] 已更新 {} 个分区状态: status={}", partitions.size(), newPartitionStatus);
        
        // 更新所有非旁路防区状态
        List<IotAlarmZoneDO> zones = alarmZoneMapper.selectList(
            new LambdaQueryWrapper<IotAlarmZoneDO>()
                .eq(IotAlarmZoneDO::getHostId, hostId)
                .ne(IotAlarmZoneDO::getArmStatus, 2) // 排除旁路状态的防区
        );
        for (IotAlarmZoneDO zone : zones) {
            zone.setArmStatus(newZoneArmStatus);
            zone.setZoneStatus(newZoneStatus);
            zone.setStatus(newZoneStatusChar);
            zone.setStatusName(newZoneStatusName);
            zone.setOnlineStatus(1);
            alarmZoneMapper.updateById(zone);
        }
        log.info("[optimisticUpdateHostAndZones] 已更新 {} 个防区状态: armStatus={}, zoneStatus={}", 
                zones.size(), newZoneArmStatus, newZoneStatus);
    }
    
    /**
     * 清除报警状态（不改变布防状态）
     */
    private void clearAlarmStatus(Long hostId) {
        // 更新主机报警状态
        IotAlarmHostDO hostUpdate = new IotAlarmHostDO();
        hostUpdate.setId(hostId);
        hostUpdate.setAlarmStatus(0);
        alarmHostMapper.updateById(hostUpdate);
        
        // 更新所有防区报警状态
        List<IotAlarmZoneDO> zones = alarmZoneMapper.selectList(
            new LambdaQueryWrapper<IotAlarmZoneDO>()
                .eq(IotAlarmZoneDO::getHostId, hostId)
        );
        for (IotAlarmZoneDO zone : zones) {
            zone.setAlarmStatus(0);
            alarmZoneMapper.updateById(zone);
        }
        log.info("[clearAlarmStatus] 已清除主机和 {} 个防区的报警状态: hostId={}", zones.size(), hostId);
    }
    
    /**
     * 构建包含完整状态的响应VO（主机+分区+防区）
     * 
     * @param host 主机DO对象
     * @return 包含主机、分区、防区的完整状态VO
     */
    private IotAlarmHostWithDetailsRespVO buildHostWithDetails(IotAlarmHostDO host) {
        // 1. 转换主机基本信息
        IotAlarmHostWithDetailsRespVO result = new IotAlarmHostWithDetailsRespVO();
        BeanUtils.copyProperties(IotAlarmHostConvert.INSTANCE.convert(host), result);
        
        // 2. 查询分区列表
        List<IotAlarmPartitionDO> partitions = alarmPartitionMapper.selectList(
            new LambdaQueryWrapper<IotAlarmPartitionDO>()
                .eq(IotAlarmPartitionDO::getHostId, host.getId())
                .orderByAsc(IotAlarmPartitionDO::getPartitionNo)
        );
        
        // 3. 查询所有防区
        List<IotAlarmZoneDO> allZones = alarmZoneMapper.selectList(
            new LambdaQueryWrapper<IotAlarmZoneDO>()
                .eq(IotAlarmZoneDO::getHostId, host.getId())
                .orderByAsc(IotAlarmZoneDO::getZoneNo)
        );
        
        // 4. 将防区分配到对应的分区中
        List<IotAlarmPartitionRespVO> partitionVOs = IotAlarmPartitionConvert.INSTANCE.convertList(partitions);
        for (IotAlarmPartitionRespVO partitionVO : partitionVOs) {
            // 找到属于该分区的所有防区
            List<IotAlarmZoneDO> partitionZones = allZones.stream()
                .filter(zone -> partitionVO.getId().equals(zone.getPartitionId()))
                .collect(java.util.stream.Collectors.toList());
            
            // 转换并设置到分区的zones字段
            partitionVO.setZones(IotAlarmZoneConvert.INSTANCE.convertList(partitionZones));
        }
        
        result.setPartitions(partitionVOs);
        
        // 5. 设置扁平化的防区列表（用于 WebSocket 推送）
        result.setZones(IotAlarmZoneConvert.INSTANCE.convertList(allZones));
        
        log.info("[buildHostWithDetails][构建完整状态] hostId={}, 分区数={}, 总防区数={}", 
                host.getId(), partitions.size(), allZones.size());
        
        return result;
    }
    
    // ==================== 消息监听方法（处理Gateway的异步响应） ====================
    
    /**
     * 监听Gateway返回的控制命令响应
     * 当Gateway执行完控制命令后，会发送此响应消息
     * 
     * 注意：Gateway在控制成功后会自动查询状态并发送ALARM_HOST_STATUS_UPDATE消息
     * 因此这里只需要处理失败情况
     * 
     * @param response 控制命令响应
     */
    public void handleControlResponse(AlarmHostControlResponse response) {
        String requestId = response.getRequestId();
        CompletableFuture<IotAlarmHostDO> future = pendingRequests.get(requestId);
        
        if (future != null) {
            log.info("[handleControlResponse][收到控制响应] requestId={}, success={}, commandType={}", 
                    requestId, response.getSuccess(), response.getCommandType());
            
            if (!response.getSuccess()) {
                // 操作失败，完成Future并抛出异常
                log.error("[handleControlResponse][设备操作失败] requestId={}, error={}", 
                        requestId, response.getErrorMessage());
                future.completeExceptionally(
                    new RuntimeException("设备操作失败: " + response.getErrorMessage())
                );
            }
            // 成功的情况：Gateway会自动查询状态，等待ALARM_HOST_STATUS_UPDATE消息
        } else {
            log.warn("[handleControlResponse][未找到对应的请求] requestId={}", requestId);
        }
    }
    
    /**
     * 监听Gateway返回的状态更新通知
     * 当Gateway查询到设备状态后，会发送此更新消息
     * 
     * @param statusDTO 状态更新DTO
     */
    public void handleStatusUpdate(AlarmHostStatusDTO statusDTO) {
        String requestId = statusDTO.getRequestId();
        CompletableFuture<IotAlarmHostDO> future = pendingRequests.get(requestId);
        
        if (future != null) {
            log.info("[handleStatusUpdate][收到状态更新] requestId={}, account={}", 
                    requestId, statusDTO.getAccount());
            
            // 更新数据库
            updateHostStatusWithPartitions(
                statusDTO.getAccount(),
                statusDTO.getSystemStatus(),
                statusDTO.getPartitions(),
                statusDTO.getZones()
            );
            
            // 完成Future
            IotAlarmHostDO updatedHost = getAlarmHostByAccount(statusDTO.getAccount());
            if (updatedHost != null) {
                future.complete(updatedHost);
                log.info("[handleStatusUpdate][Future已完成] requestId={}, hostId={}", 
                        requestId, updatedHost.getId());
            } else {
                future.completeExceptionally(
                    new RuntimeException("更新状态后未找到主机: " + statusDTO.getAccount())
                );
            }
        } else {
            // 即使没有对应的请求，也要更新数据库（可能是设备主动上报）
            log.info("[handleStatusUpdate][更新状态（无对应请求）] account={}", statusDTO.getAccount());
            updateHostStatusWithPartitions(
                statusDTO.getAccount(),
                statusDTO.getSystemStatus(),
                statusDTO.getPartitions(),
                statusDTO.getZones()
            );
        }
    }
    
    /**
     * 推送报警主机状态更新到前端
     */
    private void pushHostStatusUpdate(Long hostId) {
        try {
            // 查询主机对象
            IotAlarmHostDO host = alarmHostMapper.selectById(hostId);
            if (host == null) {
                log.warn("[pushHostStatusUpdate][主机不存在] hostId={}", hostId);
                return;
            }
            
            // 查询完整的主机信息（包含分区和防区）
            IotAlarmHostWithDetailsRespVO hostDetails = buildHostWithDetails(host);
            if (hostDetails == null) {
                log.warn("[pushHostStatusUpdate][构建主机详情失败] hostId={}", hostId);
                return;
            }
            
            // 构建 WebSocket 消息
            AlarmHostStatusMessage message = AlarmHostStatusMessage.builder()
                    .hostId(hostDetails.getId())
                    .account(hostDetails.getAccount())
                    .hostName(hostDetails.getHostName())
                    .systemStatus(hostDetails.getSystemStatus())
                    .onlineStatus(hostDetails.getOnlineStatus())
                    .alarmStatus(hostDetails.getAlarmStatus())
                    .build();
            
            // 转换分区状态
            if (hostDetails.getPartitions() != null) {
                List<AlarmHostStatusMessage.PartitionStatus> partitionList = hostDetails.getPartitions().stream()
                        .map(p -> AlarmHostStatusMessage.PartitionStatus.builder()
                                .id(p.getId())
                                .partitionNo(p.getPartitionNo())
                                .partitionName(p.getPartitionName())
                                .status(p.getStatus())
                                .alarmStatus(p.getAlarmStatus())
                                .build())
                        .toList();
                message.setPartitions(partitionList);
            }
            
            // 转换防区状态
            if (hostDetails.getZones() != null) {
                List<AlarmHostStatusMessage.ZoneStatus> zoneList = hostDetails.getZones().stream()
                        .map(z -> AlarmHostStatusMessage.ZoneStatus.builder()
                                .id(z.getId())
                                .zoneNo(z.getZoneNo())
                                .zoneName(z.getZoneName())
                                .armStatus(z.getArmStatus())
                                .alarmStatus(z.getAlarmStatus())
                                .zoneStatusCode(z.getZoneStatusCode())
                                .build())
                        .toList();
                message.setZones(zoneList);
            }
            
            // 推送到前端
            webSocketHandler.broadcast(IotMessage.alarmHostStatus(message));
            log.info("[pushHostStatusUpdate][推送成功] hostId={}, hostName={}, systemStatus={}", 
                    hostId, hostDetails.getHostName(), hostDetails.getSystemStatus());
                    
        } catch (Exception e) {
            log.error("[pushHostStatusUpdate][推送失败] hostId={}, error={}", hostId, e.getMessage(), e);
        }
    }
    
    // ==================== 防区操作方法实现 ====================
    
    @Override
    public IotAlarmHostWithDetailsRespVO armZoneWithDetails(Long hostId, Integer zoneNo) {
        return executeZoneControlCommandWithDetails(hostId, zoneNo, "ZONE_ARM", "单防区布防");
    }
    
    @Override
    public IotAlarmHostWithDetailsRespVO disarmZoneWithDetails(Long hostId, Integer zoneNo) {
        return executeZoneControlCommandWithDetails(hostId, zoneNo, "ZONE_DISARM", "单防区撤防");
    }
    
    @Override
    public IotAlarmHostWithDetailsRespVO bypassZoneWithDetails(Long hostId, Integer zoneNo) {
        return executeZoneControlCommandWithDetails(hostId, zoneNo, "ZONE_BYPASS", "防区旁路");
    }
    
    @Override
    public IotAlarmHostWithDetailsRespVO unbypassZoneWithDetails(Long hostId, Integer zoneNo) {
        return executeZoneControlCommandWithDetails(hostId, zoneNo, "ZONE_UNBYPASS", "撤销防区旁路");
    }
    
    /**
     * 执行防区控制命令并返回完整状态
     * 
     * @param hostId 主机ID
     * @param zoneNo 防区号
     * @param commandType 命令类型（ZONE_ARM, ZONE_DISARM, ZONE_BYPASS, ZONE_UNBYPASS）
     * @param commandName 命令名称（用于日志）
     * @return 包含主机、分区、防区的完整状态
     */
    private IotAlarmHostWithDetailsRespVO executeZoneControlCommandWithDetails(
            Long hostId, Integer zoneNo, String commandType, String commandName) {
        // 1. 校验主机存在
        IotAlarmHostDO host = alarmHostMapper.selectById(hostId);
        if (host == null) {
            throw exception(ALARM_HOST_NOT_EXISTS);
        }
        
        // 2. 生成请求ID
        String requestId = IdUtil.fastSimpleUUID();
        
        // 3. 查找防区ID用于记录日志
        Long zoneId = null;
        try {
            IotAlarmZoneDO zone = alarmZoneMapper.selectOne(
                new LambdaQueryWrapper<IotAlarmZoneDO>()
                    .eq(IotAlarmZoneDO::getHostId, hostId)
                    .eq(IotAlarmZoneDO::getZoneNo, zoneNo)
            );
            if (zone != null) {
                zoneId = zone.getId();
            }
        } catch (Exception e) {
            log.warn("[executeZoneControlCommand][查找防区失败] hostId={}, zoneNo={}", hostId, zoneNo, e);
        }
        
        // 4. 记录操作日志
        try {
            // 将ZONE_ARM转换为BYPASS等标准操作类型
            String logOperationType = commandType.replace("ZONE_", "");
            operationLogService.logOperation(hostId, null, zoneId, logOperationType, null, "SUCCESS", null, requestId);
        } catch (Exception e) {
            log.error("[executeZoneControlCommand][记录操作日志失败] hostId={}, zoneNo={}, commandType={}",
                    hostId, zoneNo, commandType, e);
        }
        
        // 5. 异步发送控制命令（不等待响应）
        String account = host.getAccount();
        String password = host.getPassword();
        Long deviceId = host.getDeviceId();
        Long tenantId = TenantContextHolder.getTenantId();
        
        CompletableFuture.runAsync(() -> {
            try {
                // 构建防区控制命令参数
                Map<String, Object> params = new HashMap<>();
                params.put(AlarmDeviceTypeConstants.PARAM_HOST_ID, hostId);
                params.put(AlarmDeviceTypeConstants.PARAM_ACCOUNT, account);
                params.put(AlarmDeviceTypeConstants.PARAM_PASSWORD, password);
                params.put(AlarmDeviceTypeConstants.PARAM_TENANT_ID, tenantId);
                params.put(AlarmDeviceTypeConstants.PARAM_ZONE_NO, zoneNo);
                
                String publishedRequestId = deviceCommandPublisher.publishCommand(
                        AlarmDeviceTypeConstants.ALARM,
                        deviceId,
                        commandType,
                        params
                );
                
                log.info("[executeZoneControlCommand][已发送{}命令] hostId={}, zoneNo={}, commandType={}, requestId={}",
                        commandName, hostId, zoneNo, commandType, publishedRequestId);
            } catch (Exception e) {
                log.error("[executeZoneControlCommand][发送{}命令失败] hostId={}, zoneNo={}",
                        commandName, hostId, zoneNo, e);
            }
        });
        
        // 6. 立即返回当前状态（不等待设备响应）
        return buildHostWithDetails(host);
    }
}
