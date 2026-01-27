package cn.iocoder.yudao.module.iot.service.alarm;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostStatusRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition.IotAlarmPartitionRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmPartitionDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmHostMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmPartitionMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmZoneMapper;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.iot.enums.device.AlarmDeviceTypeConstants.*;

/**
 * 报警主机分区 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotAlarmPartitionServiceImpl implements IotAlarmPartitionService {

    @Resource
    private IotAlarmPartitionMapper partitionMapper;

    @Resource
    private IotAlarmZoneMapper zoneMapper;

    @Resource
    private IotAlarmHostMapper hostMapper;
    
    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;

    @Override
    public List<IotAlarmPartitionRespVO> getPartitionListByHostId(Long hostId) {
        // 1. 先查询数据库中的分区列表
        List<IotAlarmPartitionDO> partitions = partitionMapper.selectListByHostId(hostId);

        // 2. 如果数据库中没有分区数据，则触发一次状态查询（走消息总线到 newgateway），并短暂等待落库
        if (partitions.isEmpty()) {
            log.info("[getPartitionListByHostId][数据库无分区数据，触发状态查询] hostId={}", hostId);

            // 查询主机信息
            IotAlarmHostDO host = hostMapper.selectById(hostId);
            if (host == null || StrUtil.isBlank(host.getAccount())) {
                log.warn("[getPartitionListByHostId][主机不存在或账号为空] hostId={}", hostId);
                return Collections.emptyList();
            }

            try {
                // 触发查询：Biz -> DEVICE_SERVICE_INVOKE -> newgateway
                triggerHostStatusQuery(hostId);
            } catch (Exception e) {
                log.warn("[getPartitionListByHostId][触发状态查询失败] hostId={}, account={}, error={}",
                        hostId, host.getAccount(), e.getMessage(), e);
            }

            // 短暂等待数据库落库（由查询结果消费者/状态同步逻辑写入）
            partitions = waitPartitionsReady(hostId, 2500);
            
            // 如果仍然没有数据，返回空列表
            if (partitions.isEmpty()) {
                return Collections.emptyList();
            }
        }

        // 3. 转换为VO，并计算每个分区的防区数量
        return partitions.stream().map(partition -> {
            IotAlarmPartitionRespVO vo = new IotAlarmPartitionRespVO();
            vo.setId(partition.getId());
            vo.setHostId(partition.getHostId());
            vo.setPartitionNo(partition.getPartitionNo());
            vo.setPartitionName(partition.getPartitionName());
            vo.setStatus(partition.getStatus());
            vo.setDescription(partition.getDescription());
            
            // 查询该分区下的防区数量
            long zoneCount = zoneMapper.selectCount(IotAlarmZoneDO::getPartitionId, partition.getId());
            vo.setZoneCount((int) zoneCount);
            
            vo.setCreateTime(partition.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 等待分区数据写入数据库
     *
     * <p>用于前端懒加载场景：首次打开时 DB 为空，触发一次查询后等待落库。</p>
     */
    private List<IotAlarmPartitionDO> waitPartitionsReady(Long hostId, long maxWaitMs) {
        long deadline = System.currentTimeMillis() + Math.max(0, maxWaitMs);
        List<IotAlarmPartitionDO> partitions = partitionMapper.selectListByHostId(hostId);
        while (partitions.isEmpty() && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            partitions = partitionMapper.selectListByHostId(hostId);
        }
        return partitions;
    }

    @Override
    public void triggerHostStatusQuery(Long hostId) {
        // 1. 查询主机信息
        IotAlarmHostDO host = hostMapper.selectById(hostId);
        if (host == null) {
            throw new IllegalArgumentException("主机不存在：" + hostId);
        }

        log.info("[triggerHostStatusQuery][触发主机状态查询] hostId={}, account={}, deviceId={}", 
                hostId, host.getAccount(), host.getDeviceId());
        
        // 2. 通过消息总线发送状态查询命令到 Gateway
        try {
            Long deviceId = host.getDeviceId();
            if (deviceId == null) {
                log.warn("[triggerHostStatusQuery][主机未关联设备] hostId={}, account={}", hostId, host.getAccount());
                throw new RuntimeException("主机未关联设备，无法查询状态");
            }
            
            // 构建命令参数
            Map<String, Object> params = new HashMap<>();
            params.put(PARAM_ACCOUNT, host.getAccount());
            
            // 使用 DeviceCommandPublisher 发送 QUERY_STATUS 命令
            String requestId = deviceCommandPublisher.publishCommand(ALARM, deviceId, COMMAND_QUERY_STATUS, params);
            
            log.info("[triggerHostStatusQuery][状态查询命令已发送] hostId={}, deviceId={}, account={}, requestId={}", 
                    hostId, deviceId, host.getAccount(), requestId);
            
        } catch (Exception e) {
            log.error("[triggerHostStatusQuery][触发查询失败] hostId={}, account={}",
                    hostId, host.getAccount(), e);
            throw new RuntimeException("触发查询失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncHostStatusToDatabase(Long hostId, IotAlarmHostStatusRespVO statusDTO) {
        log.info("[syncHostStatusToDatabase][同步主机状态到数据库] hostId={}, partitionCount={}, zoneCount={}",
                hostId, 
                statusDTO.getPartitions() != null ? statusDTO.getPartitions().size() : 0,
                statusDTO.getZones() != null ? statusDTO.getZones().size() : 0);

        // 1. 同步分区状态
        if (statusDTO.getPartitions() != null && !statusDTO.getPartitions().isEmpty()) {
            for (IotAlarmHostStatusRespVO.PartitionStatus partitionStatus : statusDTO.getPartitions()) {
                // 查询分区是否存在
                IotAlarmPartitionDO partition = partitionMapper.selectOne(
                        IotAlarmPartitionDO::getHostId, hostId,
                        IotAlarmPartitionDO::getPartitionNo, partitionStatus.getPartitionNo()
                );

                if (partition == null) {
                    // 创建新分区
                    try {
                        partition = new IotAlarmPartitionDO();
                        partition.setHostId(hostId);
                        partition.setPartitionNo(partitionStatus.getPartitionNo());
                        partition.setPartitionName("分区" + partitionStatus.getPartitionNo());
                        partition.setStatus(partitionStatus.getStatus());
                        partition.setDescription("自动创建");
                        partitionMapper.insert(partition);
                        log.info("[syncHostStatusToDatabase][创建分区] hostId={}, partitionNo={}", 
                                hostId, partitionStatus.getPartitionNo());
                    } catch (org.springframework.dao.DuplicateKeyException e) {
                        // 并发情况下可能已被其他线程创建，重新查询并更新
                        log.warn("[syncHostStatusToDatabase][分区已存在，重新查询] hostId={}, partitionNo={}", 
                                hostId, partitionStatus.getPartitionNo());
                        partition = partitionMapper.selectOne(
                                IotAlarmPartitionDO::getHostId, hostId,
                                IotAlarmPartitionDO::getPartitionNo, partitionStatus.getPartitionNo()
                        );
                        if (partition != null) {
                            partition.setStatus(partitionStatus.getStatus());
                            partitionMapper.updateById(partition);
                            log.info("[syncHostStatusToDatabase][更新分区] hostId={}, partitionNo={}", 
                                    hostId, partitionStatus.getPartitionNo());
                        }
                    }
                } else {
                    // 更新分区状态
                    partition.setStatus(partitionStatus.getStatus());
                    partitionMapper.updateById(partition);
                    log.info("[syncHostStatusToDatabase][更新分区] hostId={}, partitionNo={}", 
                            hostId, partitionStatus.getPartitionNo());
                }
            }
        }

        // 2. 同步防区状态（包含新增字段）
        if (statusDTO.getZones() != null && !statusDTO.getZones().isEmpty()) {
            // 获取默认分区ID（只支持单分区，将所有防区分配到第一个分区）
            Long defaultPartitionId = null;
            List<IotAlarmPartitionDO> hostPartitions = partitionMapper.selectListByHostId(hostId);
            if (!hostPartitions.isEmpty()) {
                defaultPartitionId = hostPartitions.get(0).getId();
                log.info("[syncHostStatusToDatabase][使用默认分区] hostId={}, partitionId={}", hostId, defaultPartitionId);
            }
            
            for (IotAlarmHostStatusRespVO.ZoneStatusVO zoneStatus : statusDTO.getZones()) {
                // 查询防区是否存在
                IotAlarmZoneDO zone = zoneMapper.selectOne(
                        IotAlarmZoneDO::getHostId, hostId,
                        IotAlarmZoneDO::getZoneNo, zoneStatus.getZoneNo()
                );

                if (zone == null) {
                    // 创建新防区
                    zone = new IotAlarmZoneDO();
                    zone.setHostId(hostId);
                    zone.setZoneNo(zoneStatus.getZoneNo());
                    zone.setZoneName(zoneStatus.getZoneName() != null ? zoneStatus.getZoneName() : "防区" + zoneStatus.getZoneNo());
                    
                    // 设置所属分区ID（只支持单分区，所有防区分配到第一个分区）
                    if (defaultPartitionId != null) {
                        zone.setPartitionId(defaultPartitionId);
                    }
                    
                    // 设置协议原始状态字段
                    zone.setStatus(zoneStatus.getStatus());
                    zone.setStatusName(zoneStatus.getStatusName());
                    
                    // 设置防区状态（根据协议原始状态）
                    zone.setZoneStatus(convertZoneStatus(zoneStatus.getStatus()));
                    zone.setOnlineStatus(1);
                    zone.setArmStatus(zoneStatus.getArmStatus());
                    zone.setAlarmStatus(zoneStatus.getAlarmStatus());
                    zone.setAlarmCount(zoneStatus.getAlarmStatus() != null && zoneStatus.getAlarmStatus() > 0 ? 1 : 0);
                    if (zoneStatus.getAlarmStatus() != null && zoneStatus.getAlarmStatus() > 0) {
                        zone.setLastAlarmTime(LocalDateTime.now());
                    }
                    zoneMapper.insert(zone);
                    log.info("[syncHostStatusToDatabase][创建防区] hostId={}, zoneNo={}, partitionId={}, armStatus={}, alarmStatus={}", 
                            hostId, zoneStatus.getZoneNo(), defaultPartitionId, zoneStatus.getStatus(), zoneStatus.getAlarmStatus());
                } else {
                    // 更新防区状态
                    zone.setStatus(zoneStatus.getStatus());
                    zone.setStatusName(zoneStatus.getStatusName());
                    zone.setArmStatus(zoneStatus.getArmStatus());
                    zone.setAlarmStatus(zoneStatus.getAlarmStatus());
                    zone.setZoneStatus(convertZoneStatus(zoneStatus.getStatus()));
                    zone.setOnlineStatus(1);
                    if (zoneStatus.getAlarmStatus() != null && zoneStatus.getAlarmStatus() > 0) {
                        zone.setAlarmCount((zone.getAlarmCount() != null ? zone.getAlarmCount() : 0) + 1);
                        zone.setLastAlarmTime(LocalDateTime.now());
                    }
                    zoneMapper.updateById(zone);
                    log.info("[syncHostStatusToDatabase][更新防区] hostId={}, zoneNo={}, armStatus={}, alarmStatus={}", 
                            hostId, zoneStatus.getZoneNo(), zoneStatus.getArmStatus(), zoneStatus.getAlarmStatus());
                }
            }
        }

        log.info("[syncHostStatusToDatabase][同步完成] hostId={}, 已同步 {} 个分区和 {} 个防区", 
                hostId, 
                statusDTO.getPartitions() != null ? statusDTO.getPartitions().size() : 0,
                statusDTO.getZones() != null ? statusDTO.getZones().size() : 0);
    }
    
    /**
     * 根据协议原始状态字符转换为防区状态
     */
    private String convertZoneStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "DISARM";
        }
        char ch = status.charAt(0);
        if (ch == 'a') return "DISARM";  // 撤防
        if (ch == 'b') return "BYPASS";  // 旁路
        if (Character.isUpperCase(ch)) return "ARM";  // 布防
        return "DISARM";
    }

    @Override
    public void updatePartitionArmStatusByDeviceIdAndAreaNo(Long deviceId, String areaNo, String armStatus) {
        // 1. 根据 deviceId 查找主机
        IotAlarmHostDO host = hostMapper.selectOne(IotAlarmHostDO::getDeviceId, deviceId);
        if (host == null) {
            log.warn("[updatePartitionArmStatusByDeviceIdAndAreaNo][主机不存在] deviceId={}", deviceId);
            return;
        }
        
        // 2. 查找分区
        IotAlarmPartitionDO partition = partitionMapper.selectOne(
                IotAlarmPartitionDO::getHostId, host.getId(),
                IotAlarmPartitionDO::getPartitionNo, areaNo
        );
        
        if (partition == null) {
            log.warn("[updatePartitionArmStatusByDeviceIdAndAreaNo][分区不存在] hostId={}, areaNo={}", 
                    host.getId(), areaNo);
            return;
        }
        
        // 3. 更新分区状态（将字符串状态转换为整数）
        IotAlarmPartitionDO updateObj = new IotAlarmPartitionDO();
        updateObj.setId(partition.getId());
        // 将布防状态字符串转换为整数：DISARM=0, ARM=1
        Integer statusValue = "DISARM".equals(armStatus) ? 0 : 1;
        updateObj.setStatus(statusValue);
        partitionMapper.updateById(updateObj);
        
        log.info("[updatePartitionArmStatusByDeviceIdAndAreaNo][更新分区状态] hostId={}, areaNo={}, armStatus={}", 
                host.getId(), areaNo, armStatus);
    }
    
    @Override
    public Integer getPartitionCountByHostId(Long hostId) {
        return Math.toIntExact(partitionMapper.selectCount(
                IotAlarmPartitionDO::getHostId, hostId
        ));
    }
    
    @Override
    @Transactional
    public void updatePartitionStatus(Long hostId, Integer partitionNo, Integer status) {
        // 查找分区
        IotAlarmPartitionDO partition = partitionMapper.selectOne(
                IotAlarmPartitionDO::getHostId, hostId,
                IotAlarmPartitionDO::getPartitionNo, partitionNo
        );
        
        if (partition == null) {
            // 分区不存在，自动创建
            log.info("[updatePartitionStatus][分区不存在，自动创建] hostId={}, partitionNo={}", hostId, partitionNo);
            partition = new IotAlarmPartitionDO();
            partition.setHostId(hostId);
            partition.setPartitionNo(partitionNo);
            partition.setPartitionName("分区" + partitionNo);
            partition.setStatus(status);
            partition.setDescription("自动创建");
            partitionMapper.insert(partition);
            log.info("[updatePartitionStatus][创建分区成功] hostId={}, partitionNo={}, partitionId={}, status={}", 
                    hostId, partitionNo, partition.getId(), status);
        } else {
            // 更新分区状态
            IotAlarmPartitionDO updatePartition = new IotAlarmPartitionDO();
            updatePartition.setId(partition.getId());
            updatePartition.setStatus(status);
            partitionMapper.updateById(updatePartition);
            
            log.info("[updatePartitionStatus][更新分区状态] hostId={}, partitionNo={}, status={}", 
                    hostId, partitionNo, status);
        }
    }

    @Override
    public void updatePartitionName(Long id, String partitionName) {
        // 1. 校验分区是否存在
        IotAlarmPartitionDO partition = partitionMapper.selectById(id);
        if (partition == null) {
            log.warn("[updatePartitionName][分区不存在] id={}", id);
            throw new IllegalArgumentException("分区不存在");
        }

        // 2. 更新分区名称
        IotAlarmPartitionDO updateObj = new IotAlarmPartitionDO();
        updateObj.setId(id);
        updateObj.setPartitionName(partitionName);
        partitionMapper.updateById(updateObj);

        log.info("[updatePartitionName][分区名称已更新] partitionId={}, partitionName={}", id, partitionName);
    }

    @Override
    public void armPartitionAll(Long partitionId) {
        // 1. 查询分区信息
        IotAlarmPartitionDO partition = partitionMapper.selectById(partitionId);
        if (partition == null) {
            throw new IllegalArgumentException("分区不存在");
        }

        // 2. 查询主机信息
        IotAlarmHostDO host = hostMapper.selectById(partition.getHostId());
        if (host == null) {
            throw new IllegalArgumentException("主机不存在");
        }

        // 3. 异步发送外出布防指令（指令码2，参数为分区序号）
        Long deviceId = host.getDeviceId();
        String account = host.getAccount();
        Integer partitionNo = partition.getPartitionNo();
        String password = host.getPassword();
        
        CompletableFuture.runAsync(() -> {
            try {
                sendControlCommand(deviceId, account, "ARM", "ALL", String.valueOf(partitionNo), password);
                log.info("[armPartitionAll][分区外出布防指令已发送] partitionId={}, partitionNo={}, hostAccount={}", 
                        partitionId, partitionNo, account);
            } catch (Exception e) {
                log.error("[armPartitionAll][发送指令失败] partitionId={}, partitionNo={}", partitionId, partitionNo, e);
            }
        });
    }

    @Override
    public void armPartitionEmergency(Long partitionId) {
        // 1. 查询分区信息
        IotAlarmPartitionDO partition = partitionMapper.selectById(partitionId);
        if (partition == null) {
            throw new IllegalArgumentException("分区不存在");
        }

        // 2. 查询主机信息
        IotAlarmHostDO host = hostMapper.selectById(partition.getHostId());
        if (host == null) {
            throw new IllegalArgumentException("主机不存在");
        }

        // 3. 异步发送居家布防指令（指令码3，参数为分区序号）
        Long deviceId = host.getDeviceId();
        String account = host.getAccount();
        Integer partitionNo = partition.getPartitionNo();
        String password = host.getPassword();
        
        CompletableFuture.runAsync(() -> {
            try {
                sendControlCommand(deviceId, account, "ARM", "EMERGENCY", String.valueOf(partitionNo), password);
                log.info("[armPartitionEmergency][分区居家布防指令已发送] partitionId={}, partitionNo={}, hostAccount={}", 
                        partitionId, partitionNo, account);
            } catch (Exception e) {
                log.error("[armPartitionEmergency][发送指令失败] partitionId={}, partitionNo={}", partitionId, partitionNo, e);
            }
        });
    }

    @Override
    public void disarmPartition(Long partitionId) {
        // 1. 查询分区信息
        IotAlarmPartitionDO partition = partitionMapper.selectById(partitionId);
        if (partition == null) {
            throw new IllegalArgumentException("分区不存在");
        }

        // 2. 查询主机信息
        IotAlarmHostDO host = hostMapper.selectById(partition.getHostId());
        if (host == null) {
            throw new IllegalArgumentException("主机不存在");
        }

        // 3. 异步发送撤防指令（指令码1，参数为分区序号）
        Long deviceId = host.getDeviceId();
        String account = host.getAccount();
        Integer partitionNo = partition.getPartitionNo();
        String password = host.getPassword();
        
        CompletableFuture.runAsync(() -> {
            try {
                sendControlCommand(deviceId, account, "DISARM", "", String.valueOf(partitionNo), password);
                log.info("[disarmPartition][分区撤防指令已发送] partitionId={}, partitionNo={}, hostAccount={}", 
                        partitionId, partitionNo, account);
            } catch (Exception e) {
                log.error("[disarmPartition][发送指令失败] partitionId={}, partitionNo={}", partitionId, partitionNo, e);
            }
        });
    }

    @Override
    public void clearPartitionAlarm(Long partitionId) {
        // 1. 查询分区信息
        IotAlarmPartitionDO partition = partitionMapper.selectById(partitionId);
        if (partition == null) {
            throw new IllegalArgumentException("分区不存在");
        }

        // 2. 查询主机信息
        IotAlarmHostDO host = hostMapper.selectById(partition.getHostId());
        if (host == null) {
            throw new IllegalArgumentException("主机不存在");
        }

        // 3. 异步发送消警指令（指令码11，参数为分区序号）
        Long deviceId = host.getDeviceId();
        String account = host.getAccount();
        Integer partitionNo = partition.getPartitionNo();
        String password = host.getPassword();
        
        CompletableFuture.runAsync(() -> {
            try {
                sendControlCommand(deviceId, account, "CLEAR", "", String.valueOf(partitionNo), password);
                log.info("[clearPartitionAlarm][分区消警指令已发送] partitionId={}, partitionNo={}, hostAccount={}", 
                        partitionId, partitionNo, account);
            } catch (Exception e) {
                log.error("[clearPartitionAlarm][发送指令失败] partitionId={}, partitionNo={}", partitionId, partitionNo, e);
            }
        });
    }

    /**
     * 发送控制指令到Gateway（通过 DeviceCommandPublisher 发送到统一 Topic）
     * 
     * <p>适配说明：使用 DeviceCommandPublisher 替代直接发送到 ALARM_HOST_CONTROL 主题，
     * 实现 biz 和 newgateway 的解耦。</p>
     * 
     * <p>Requirements: 4.1, 4.2, 4.3</p>
     * 
     * @param deviceId  设备ID（用于 DeviceCommandPublisher）
     * @param account   主机账号
     * @param control   控制类型（ARM, DISARM, CLEAR）
     * @param subParam  子参数（ALL, EMERGENCY 等）
     * @param param     分区序号或防区序号
     * @param password  主机密码
     */
    private void sendControlCommand(Long deviceId, String account, String control, String subParam,
                                     String param, String password) {
        try {
            // 构建命令类型（组合control和subParam）
            String commandType = control;
            if (StrUtil.isNotBlank(subParam)) {
                commandType = control + "_" + subParam;  // 例如：ARM_ALL, ARM_EMERGENCY
            }
            
            // 构建命令参数
            Map<String, Object> params = new HashMap<>();
            params.put(PARAM_ACCOUNT, account);
            params.put(PARAM_PASSWORD, password != null ? password : "1234");
            if (StrUtil.isNotBlank(param)) {
                params.put("partitionNo", Integer.parseInt(param));
            }
            
            // 使用 DeviceCommandPublisher 发送命令到统一 Topic（DEVICE_SERVICE_INVOKE）
            String requestId = deviceCommandPublisher.publishCommand(ALARM, deviceId, commandType, params);
            
            log.info("[sendControlCommand][控制指令已发送] deviceType={}, deviceId={}, commandType={}, account={}, requestId={}",
                    ALARM, deviceId, commandType, account, requestId);
            
        } catch (Exception e) {
            log.error("[sendControlCommand][发送控制指令失败] account={}, control={}, param={}", 
                    account, control, param, e);
            throw new RuntimeException("发送控制指令失败: " + e.getMessage());
        }
    }
}
