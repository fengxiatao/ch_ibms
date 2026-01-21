package cn.iocoder.yudao.module.iot.mq.consumer.device.handler;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmPartitionDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmHostMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmPartitionMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmZoneMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 报警主机命令结果处理器
 * 
 * <p>处理报警主机命令执行结果，包括：</p>
 * <ul>
 *   <li>主机布防（ARM_ALL）：更新所有分区和非旁路防区为布防状态</li>
 *   <li>主机居家布防（ARM_EMERGENCY）：更新所有分区和非旁路防区为居家布防状态</li>
 *   <li>主机撤防（DISARM）：更新所有分区和非旁路防区为撤防状态</li>
 *   <li>防区旁路（ZONE_BYPASS）：更新防区状态为旁路</li>
 *   <li>取消旁路（ZONE_UNBYPASS）：更新防区状态为撤防</li>
 *   <li>防区布防（ZONE_ARM）：更新防区状态为布防</li>
 *   <li>防区撤防（ZONE_DISARM）：更新防区状态为撤防</li>
 * </ul>
 * 
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AlarmDeviceResultHandler implements DeviceResultHandler {

    private static final String DEVICE_TYPE = "ALARM";

    // 主机级别命令类型常量
    private static final String CMD_ARM_ALL = "ARM_ALL";
    private static final String CMD_ARM_EMERGENCY = "ARM_EMERGENCY";
    private static final String CMD_DISARM = "DISARM";
    
    // 状态上报类型常量
    private static final String CMD_STATUS_UPDATE = "STATUS_UPDATE";
    private static final String CMD_OPC_CONTROL_ACK = "OPC_CONTROL_ACK";
    
    // 防区级别命令类型常量
    private static final String CMD_ZONE_BYPASS = "ZONE_BYPASS";
    private static final String CMD_ZONE_UNBYPASS = "ZONE_UNBYPASS";
    private static final String CMD_ZONE_ARM = "ZONE_ARM";
    private static final String CMD_ZONE_DISARM = "ZONE_DISARM";

    // 布防状态常量
    private static final Integer ARM_STATUS_DISARM = 0;  // 撤防
    private static final Integer ARM_STATUS_ARM = 1;     // 布防
    private static final Integer ARM_STATUS_BYPASS = 2;  // 旁路

    // 防区状态常量
    private static final String ZONE_STATUS_DISARM = "DISARM";
    private static final String ZONE_STATUS_ARM = "ARM";
    private static final String ZONE_STATUS_BYPASS = "BYPASS";
    
    // 分区状态常量（与主机systemStatus一致：0-撤防，1-布防，2-居家布防）
    private static final Integer PARTITION_STATUS_DISARM = 0;
    private static final Integer PARTITION_STATUS_ARM_ALL = 1;
    private static final Integer PARTITION_STATUS_ARM_EMERGENCY = 2;

    private final IotAlarmZoneMapper alarmZoneMapper;
    private final IotAlarmHostMapper alarmHostMapper;
    private final IotAlarmPartitionMapper alarmPartitionMapper;

    @Override
    public String getDeviceType() {
        return DEVICE_TYPE;
    }

    @Override
    public void handleResult(IotDeviceMessage message) {
        if (message == null) {
            return;
        }

        // 检查命令是否成功
        Integer code = message.getCode();
        if (code == null || code != 0) {
            log.debug("[AlarmDeviceResultHandler] 命令执行失败，跳过状态更新: code={}, msg={}", 
                    code, message.getMsg());
            return;
        }

        // 获取命令类型和参数
        String commandType = extractCommandType(message);
        if (commandType == null) {
            log.debug("[AlarmDeviceResultHandler] 无法获取命令类型，跳过处理");
            return;
        }

        log.info("[AlarmDeviceResultHandler] 处理命令结果: deviceId={}, commandType={}", 
                message.getDeviceId(), commandType);

        // 根据命令类型处理
        switch (commandType) {
            // 设备状态上报（来自设备的实际状态）
            case CMD_STATUS_UPDATE:
                handleDeviceStatusUpdate(message);
                break;
            case CMD_OPC_CONTROL_ACK:
                // OPC控制ACK只是确认命令收到，实际状态由STATUS_UPDATE处理
                log.debug("[AlarmDeviceResultHandler] 收到OPC控制ACK，等待设备状态上报");
                break;
            // 主机级别命令
            case CMD_ARM_ALL:
                handleHostArmAll(message);
                break;
            case CMD_ARM_EMERGENCY:
                handleHostArmEmergency(message);
                break;
            case CMD_DISARM:
                handleHostDisarm(message);
                break;
            // 防区级别命令
            case CMD_ZONE_BYPASS:
                handleZoneBypass(message);
                break;
            case CMD_ZONE_UNBYPASS:
                handleZoneUnbypass(message);
                break;
            case CMD_ZONE_ARM:
                handleZoneArm(message);
                break;
            case CMD_ZONE_DISARM:
                handleZoneDisarm(message);
                break;
            default:
                log.debug("[AlarmDeviceResultHandler] 不需要处理的命令类型: {}", commandType);
        }
    }
    
    // ==================== 设备状态上报处理 ====================

    /**
     * 处理设备状态上报（来自设备的实际状态）
     * 这是最准确的状态来源，根据设备上报的armStatus更新数据库
     */
    private void handleDeviceStatusUpdate(IotDeviceMessage message) {
        Long deviceId = message.getDeviceId();
        Object params = message.getParams();
        
        if (!(params instanceof Map)) {
            log.warn("[AlarmDeviceResultHandler] STATUS_UPDATE 消息缺少参数");
            return;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramsMap = (Map<String, Object>) params;
        String armStatus = (String) paramsMap.get("armStatus");
        
        if (armStatus == null) {
            log.warn("[AlarmDeviceResultHandler] STATUS_UPDATE 消息缺少 armStatus");
            return;
        }
        
        log.info("[AlarmDeviceResultHandler] ✅ 收到设备状态上报: deviceId={}, armStatus={}", deviceId, armStatus);
        
        // 根据 deviceId 查找对应的主机
        IotAlarmHostDO host = alarmHostMapper.selectOne(
            new LambdaQueryWrapper<IotAlarmHostDO>()
                .eq(IotAlarmHostDO::getDeviceId, deviceId)
        );
        
        if (host == null) {
            log.warn("[AlarmDeviceResultHandler] 未找到设备对应的主机: deviceId={}", deviceId);
            return;
        }
        
        Long hostId = host.getId();
        
        // 解析设备上报的armStatus并更新数据库
        // armStatus可能是: ARMED, DISARMED, STAY_ARMED, AWAY_ARMED 等
        switch (armStatus.toUpperCase()) {
            case "ARMED":
            case "AWAY_ARMED":
            case "ARM_AWAY":
                // 外出布防
                updateAllPartitionsStatus(hostId, PARTITION_STATUS_ARM_ALL);
                updateAllZonesStatus(hostId, ARM_STATUS_ARM, ZONE_STATUS_ARM, "A", "防区布防+无报警");
                updateHostSystemStatus(hostId, 1);  // 1=全部布防
                break;
            case "STAY_ARMED":
            case "ARM_STAY":
            case "HOME_ARMED":
                // 居家布防
                updateAllPartitionsStatus(hostId, PARTITION_STATUS_ARM_EMERGENCY);
                updateAllZonesStatus(hostId, ARM_STATUS_ARM, ZONE_STATUS_ARM, "A", "防区布防+无报警");
                updateHostSystemStatus(hostId, 2);  // 2=居家布防
                break;
            case "DISARMED":
            case "DISARM":
                // 撤防
                updateAllPartitionsStatus(hostId, PARTITION_STATUS_DISARM);
                updateAllZonesStatus(hostId, ARM_STATUS_DISARM, ZONE_STATUS_DISARM, "a", "防区撤防");
                updateHostSystemStatus(hostId, 0);  // 0=撤防
                break;
            default:
                log.warn("[AlarmDeviceResultHandler] 未知的设备布防状态: {}", armStatus);
        }
        
        log.info("[AlarmDeviceResultHandler] ✅ 已根据设备状态上报更新数据库: hostId={}, armStatus={}", hostId, armStatus);
    }
    
    /**
     * 更新主机系统状态
     */
    private void updateHostSystemStatus(Long hostId, Integer systemStatus) {
        try {
            IotAlarmHostDO host = new IotAlarmHostDO();
            host.setId(hostId);
            host.setSystemStatus(systemStatus);
            alarmHostMapper.updateById(host);
            log.info("[AlarmDeviceResultHandler] 已更新主机状态: hostId={}, systemStatus={}", hostId, systemStatus);
        } catch (Exception e) {
            log.error("[AlarmDeviceResultHandler] 更新主机状态失败: hostId={}", hostId, e);
        }
    }
    
    // ==================== 主机级别命令处理 ====================

    /**
     * 处理主机全部布防（外出布防）命令结果
     * 更新所有分区和非旁路防区为布防状态
     */
    private void handleHostArmAll(IotDeviceMessage message) {
        Long hostId = extractHostId(message);
        if (hostId == null) {
            log.warn("[AlarmDeviceResultHandler] ARM_ALL 命令缺少 hostId");
            return;
        }

        log.info("[AlarmDeviceResultHandler] ✅ 主机全部布防成功: hostId={}", hostId);
        
        // 1. 更新所有分区状态为布防
        updateAllPartitionsStatus(hostId, PARTITION_STATUS_ARM_ALL);
        
        // 2. 更新所有非旁路防区状态为布防
        updateAllZonesStatus(hostId, ARM_STATUS_ARM, ZONE_STATUS_ARM, "A", "防区布防+无报警");
    }

    /**
     * 处理主机居家布防（紧急布防）命令结果
     * 更新所有分区和非旁路防区为居家布防状态
     */
    private void handleHostArmEmergency(IotDeviceMessage message) {
        Long hostId = extractHostId(message);
        if (hostId == null) {
            log.warn("[AlarmDeviceResultHandler] ARM_EMERGENCY 命令缺少 hostId");
            return;
        }

        log.info("[AlarmDeviceResultHandler] ✅ 主机居家布防成功: hostId={}", hostId);
        
        // 1. 更新所有分区状态为居家布防
        updateAllPartitionsStatus(hostId, PARTITION_STATUS_ARM_EMERGENCY);
        
        // 2. 更新所有非旁路防区状态为布防
        updateAllZonesStatus(hostId, ARM_STATUS_ARM, ZONE_STATUS_ARM, "A", "防区布防+无报警");
    }

    /**
     * 处理主机撤防命令结果
     * 更新所有分区和非旁路防区为撤防状态
     */
    private void handleHostDisarm(IotDeviceMessage message) {
        Long hostId = extractHostId(message);
        if (hostId == null) {
            log.warn("[AlarmDeviceResultHandler] DISARM 命令缺少 hostId");
            return;
        }

        log.info("[AlarmDeviceResultHandler] ✅ 主机撤防成功: hostId={}", hostId);
        
        // 1. 更新所有分区状态为撤防
        updateAllPartitionsStatus(hostId, PARTITION_STATUS_DISARM);
        
        // 2. 更新所有非旁路防区状态为撤防
        updateAllZonesStatus(hostId, ARM_STATUS_DISARM, ZONE_STATUS_DISARM, "a", "防区撤防");
    }

    /**
     * 更新主机下所有分区的状态
     * 
     * @param hostId 主机ID
     * @param status 分区状态（0-撤防，1-布防，2-居家布防）
     */
    private void updateAllPartitionsStatus(Long hostId, Integer status) {
        try {
            List<IotAlarmPartitionDO> partitions = alarmPartitionMapper.selectList(
                new LambdaQueryWrapper<IotAlarmPartitionDO>()
                    .eq(IotAlarmPartitionDO::getHostId, hostId)
            );

            for (IotAlarmPartitionDO partition : partitions) {
                partition.setStatus(status);
                alarmPartitionMapper.updateById(partition);
            }
            
            log.info("[AlarmDeviceResultHandler] 已更新 {} 个分区状态为: {}", partitions.size(), status);
        } catch (Exception e) {
            log.error("[AlarmDeviceResultHandler] 更新分区状态失败: hostId={}", hostId, e);
        }
    }

    /**
     * 更新主机下所有非旁路防区的状态
     */
    private void updateAllZonesStatus(Long hostId, Integer armStatus, String zoneStatus, 
                                      String statusChar, String statusName) {
        try {
            // 查询所有非旁路状态的防区
            List<IotAlarmZoneDO> zones = alarmZoneMapper.selectList(
                new LambdaQueryWrapper<IotAlarmZoneDO>()
                    .eq(IotAlarmZoneDO::getHostId, hostId)
                    .ne(IotAlarmZoneDO::getArmStatus, ARM_STATUS_BYPASS) // 排除旁路状态的防区
            );

            for (IotAlarmZoneDO zone : zones) {
                zone.setArmStatus(armStatus);
                zone.setZoneStatus(zoneStatus);
                zone.setStatus(statusChar);
                zone.setStatusName(statusName);
                zone.setOnlineStatus(1);
                alarmZoneMapper.updateById(zone);
            }
            
            log.info("[AlarmDeviceResultHandler] 已更新 {} 个防区状态为: armStatus={}, zoneStatus={}", 
                    zones.size(), armStatus, zoneStatus);
        } catch (Exception e) {
            log.error("[AlarmDeviceResultHandler] 更新防区状态失败: hostId={}", hostId, e);
        }
    }
    
    // ==================== 防区级别命令处理 ====================

    /**
     * 处理防区旁路命令结果
     */
    private void handleZoneBypass(IotDeviceMessage message) {
        Integer zoneNo = extractZoneNo(message);
        Long hostId = extractHostId(message);

        if (zoneNo == null || hostId == null) {
            log.warn("[AlarmDeviceResultHandler] 旁路命令缺少必要参数: hostId={}, zoneNo={}", hostId, zoneNo);
            return;
        }

        // 更新防区状态为旁路
        updateZoneStatus(hostId, zoneNo, ARM_STATUS_BYPASS, ZONE_STATUS_BYPASS, "b", "防区旁路");
        log.info("[AlarmDeviceResultHandler] ✅ 防区旁路成功: hostId={}, zoneNo={}", hostId, zoneNo);
    }

    /**
     * 处理取消旁路命令结果
     */
    private void handleZoneUnbypass(IotDeviceMessage message) {
        Integer zoneNo = extractZoneNo(message);
        Long hostId = extractHostId(message);

        if (zoneNo == null || hostId == null) {
            log.warn("[AlarmDeviceResultHandler] 取消旁路命令缺少必要参数: hostId={}, zoneNo={}", hostId, zoneNo);
            return;
        }

        // 更新防区状态为撤防（取消旁路后恢复为撤防状态）
        updateZoneStatus(hostId, zoneNo, ARM_STATUS_DISARM, ZONE_STATUS_DISARM, "a", "防区撤防");
        log.info("[AlarmDeviceResultHandler] ✅ 取消旁路成功: hostId={}, zoneNo={}", hostId, zoneNo);
    }

    /**
     * 处理防区布防命令结果
     */
    private void handleZoneArm(IotDeviceMessage message) {
        Integer zoneNo = extractZoneNo(message);
        Long hostId = extractHostId(message);

        if (zoneNo == null || hostId == null) {
            log.warn("[AlarmDeviceResultHandler] 布防命令缺少必要参数: hostId={}, zoneNo={}", hostId, zoneNo);
            return;
        }

        // 更新防区状态为布防
        updateZoneStatus(hostId, zoneNo, ARM_STATUS_ARM, ZONE_STATUS_ARM, "A", "防区布防+无报警");
        log.info("[AlarmDeviceResultHandler] ✅ 防区布防成功: hostId={}, zoneNo={}", hostId, zoneNo);
    }

    /**
     * 处理防区撤防命令结果
     */
    private void handleZoneDisarm(IotDeviceMessage message) {
        Integer zoneNo = extractZoneNo(message);
        Long hostId = extractHostId(message);

        if (zoneNo == null || hostId == null) {
            log.warn("[AlarmDeviceResultHandler] 撤防命令缺少必要参数: hostId={}, zoneNo={}", hostId, zoneNo);
            return;
        }

        // 更新防区状态为撤防
        updateZoneStatus(hostId, zoneNo, ARM_STATUS_DISARM, ZONE_STATUS_DISARM, "a", "防区撤防");
        log.info("[AlarmDeviceResultHandler] ✅ 防区撤防成功: hostId={}, zoneNo={}", hostId, zoneNo);
    }

    /**
     * 更新防区状态
     * 
     * @param hostId 主机ID
     * @param zoneNo 防区号
     * @param armStatus 布防状态（0-撤防, 1-布防, 2-旁路）
     * @param zoneStatus 防区状态字符串
     * @param statusChar 状态字符
     * @param statusName 状态名称
     */
    private void updateZoneStatus(Long hostId, Integer zoneNo, Integer armStatus, 
                                  String zoneStatus, String statusChar, String statusName) {
        try {
            // 查找防区
            IotAlarmZoneDO zone = alarmZoneMapper.selectOne(
                new LambdaQueryWrapper<IotAlarmZoneDO>()
                    .eq(IotAlarmZoneDO::getHostId, hostId)
                    .eq(IotAlarmZoneDO::getZoneNo, zoneNo)
            );

            if (zone == null) {
                log.warn("[AlarmDeviceResultHandler] 防区不存在: hostId={}, zoneNo={}", hostId, zoneNo);
                return;
            }

            // 更新状态
            zone.setArmStatus(armStatus);
            zone.setZoneStatus(zoneStatus);
            zone.setStatus(statusChar);
            zone.setStatusName(statusName);
            zone.setOnlineStatus(1); // 在线

            alarmZoneMapper.updateById(zone);
            log.debug("[AlarmDeviceResultHandler] 防区状态已更新: zoneId={}, armStatus={}, zoneStatus={}", 
                    zone.getId(), armStatus, zoneStatus);
        } catch (Exception e) {
            log.error("[AlarmDeviceResultHandler] 更新防区状态失败: hostId={}, zoneNo={}", hostId, zoneNo, e);
        }
    }

    /**
     * 从消息中提取命令类型
     */
    private String extractCommandType(IotDeviceMessage message) {
        // 优先从 method 字段获取
        String method = message.getMethod();
        if (method != null && !method.isEmpty()) {
            return method;
        }

        // 从 params 中获取 commandType
        Object params = message.getParams();
        if (params instanceof Map) {
            Object commandType = ((Map<?, ?>) params).get("commandType");
            if (commandType != null) {
                return commandType.toString();
            }
        }
        return null;
    }

    /**
     * 从消息中提取防区号
     */
    private Integer extractZoneNo(IotDeviceMessage message) {
        // 从 params 中获取
        Object params = message.getParams();
        if (params instanceof Map) {
            Object zoneNo = ((Map<?, ?>) params).get("zoneNo");
            if (zoneNo instanceof Integer) {
                return (Integer) zoneNo;
            }
            if (zoneNo instanceof Number) {
                return ((Number) zoneNo).intValue();
            }
            if (zoneNo != null) {
                try {
                    return Integer.parseInt(zoneNo.toString());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        // 从 data 中获取（备选）
        Object data = message.getData();
        if (data instanceof Map) {
            Object zoneNo = ((Map<?, ?>) data).get("zoneNo");
            if (zoneNo instanceof Integer) {
                return (Integer) zoneNo;
            }
            if (zoneNo instanceof Number) {
                return ((Number) zoneNo).intValue();
            }
        }
        return null;
    }

    /**
     * 从消息中提取主机ID
     */
    private Long extractHostId(IotDeviceMessage message) {
        // 从 params 中获取 hostId
        Object params = message.getParams();
        if (params instanceof Map) {
            Object hostId = ((Map<?, ?>) params).get("hostId");
            if (hostId instanceof Long) {
                return (Long) hostId;
            }
            if (hostId instanceof Number) {
                return ((Number) hostId).longValue();
            }
            if (hostId != null) {
                try {
                    return Long.parseLong(hostId.toString());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        // 如果 params 中没有 hostId，尝试通过 deviceId 查找
        Long deviceId = message.getDeviceId();
        if (deviceId != null) {
            try {
                IotAlarmHostDO host = alarmHostMapper.selectOne(
                    new LambdaQueryWrapper<IotAlarmHostDO>()
                        .eq(IotAlarmHostDO::getDeviceId, deviceId)
                );
                if (host != null) {
                    return host.getId();
                }
            } catch (Exception e) {
                log.warn("[AlarmDeviceResultHandler] 通过 deviceId 查找 hostId 失败: deviceId={}", deviceId, e);
            }
        }
        return null;
    }
}

