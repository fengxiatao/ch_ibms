package cn.iocoder.yudao.module.iot.service.access;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlEventMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessEventLogDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonCredentialMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.websocket.DeviceMessagePushService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 门禁事件处理器实现
 * 
 * <p>负责处理从 Gateway 模块接收的门禁事件消息，实现完整的事件处理流程：</p>
 * <ol>
 *   <li>关联人员信息 - 通过 personId 或 cardNo 查询人员信息 (Requirements: 3.2)</li>
 *   <li>保存抓拍图片 - 将 Base64 编码的图片解码并保存到文件系统 (Requirements: 3.3)</li>
 *   <li>保存事件到数据库 - 转换消息为实体并持久化 (Requirements: 3.4)</li>
 *   <li>推送到前端 - 通过 WebSocket 实时推送事件 (Requirements: 3.5)</li>
 * </ol>
 * 
 * <p>重构说明：</p>
 * <ul>
 *   <li>移除了旧的 AccessMessagePushService 依赖</li>
 *   <li>使用统一的 DeviceMessagePushService 推送门禁事件</li>
 * </ul>
 *
 * @author 芋道源码
 * @see AccessControlEventMessage
 * @see IotAccessEventLogDO
 * Requirements: 3.2, 3.3, 3.4, 3.5
 */
@Slf4j
@Service
public class AccessEventHandlerImpl implements AccessEventHandler {

    @Resource
    private IotAccessEventLogService eventLogService;


    @Resource
    private IotAccessPersonMapper personMapper;

    @Resource
    private IotAccessPersonCredentialMapper credentialMapper;

    @Resource
    private FileApi fileApi;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotDeviceChannelService channelService;

    @Resource
    private DeviceMessagePushService deviceMessagePushService;

    /** 抓拍图片存储目录 */
    private static final String CAPTURE_DIRECTORY = "access/capture";
    
    // ========== 门禁报警类型常量（大华SDK定义）==========
    /** 远程开门事件 */
    private static final int ALARM_REMOTE_OPEN_DOOR = 12673;
    /** 关门事件 */
    private static final int ALARM_DOOR_CLOSED = 12658;
    /** 门状态变化事件 */
    private static final int ALARM_DOOR_STATE_CHANGE = 12300;
    /** 门常开开始 */
    private static final int ALARM_ALWAYS_OPEN_START = 12291;
    /** 门常开结束 */
    private static final int ALARM_ALWAYS_OPEN_END = 12292;
    /** 门常闭开始 */
    private static final int ALARM_ALWAYS_CLOSED_START = 12293;
    /** 门常闭结束 */
    private static final int ALARM_ALWAYS_CLOSED_END = 12294;
    /** 远程常开 */
    private static final int ALARM_REMOTE_ALWAYS_OPEN = 12675;
    /** 远程常闭 */
    private static final int ALARM_REMOTE_ALWAYS_CLOSED = 12676;
    /** 取消常开常闭 */
    private static final int ALARM_CANCEL_ALWAYS = 12677;

    @Override
    public void handleEvent(AccessControlEventMessage event) {
        if (event == null) {
            log.warn("[handleEvent] 收到空事件消息，忽略处理");
            return;
        }

        log.info("[handleEvent] 开始处理门禁事件: deviceId={}, eventType={}, eventTime={}",
                event.getDeviceId(), event.getEventType(), event.getEventTime());

        // 【关键】获取租户ID：优先从消息中获取，否则从设备信息中查询
        Long tenantId = event.getTenantId();
        if (tenantId == null && event.getDeviceId() != null) {
            // 使用 getDeviceFromCache 方法（带 @TenantIgnore）获取设备的租户ID
            IotDeviceDO device = deviceService.getDeviceFromCache(event.getDeviceId());
            if (device != null) {
                tenantId = device.getTenantId();
                log.debug("[handleEvent] 从设备信息获取租户ID: deviceId={}, tenantId={}", 
                        event.getDeviceId(), tenantId);
            }
        }
        
        // 在正确的租户上下文中执行事件处理
        final Long finalTenantId = tenantId;
        Runnable processLogic = () -> doHandleEvent(event);
        
        if (finalTenantId != null) {
            TenantUtils.execute(finalTenantId, processLogic);
        } else {
            log.warn("[handleEvent] 无法获取租户ID，使用忽略租户模式处理: deviceId={}", event.getDeviceId());
            TenantUtils.executeIgnore(processLogic);
        }
    }
    
    /**
     * 实际的事件处理逻辑（在正确的租户上下文中执行）
     */
    private void doHandleEvent(AccessControlEventMessage event) {
        try {
            // 0. 【关键】处理门状态变化事件（常开/常闭/取消），同步到数据库
            handleDoorStateChangeIfNeeded(event);
            
            // 0.5 【新增】实时推送门状态变化到前端WebSocket（用户可以看到门的开关状态变化）
            pushDoorStateChangeToWebSocket(event);
            
            // 1. 关联人员信息
            enrichPersonInfo(event);

            // 2. 保存抓拍图片（如果有）
            String captureUrl = saveSnapshotImage(event);

            // 3. 转换并保存事件到数据库
            IotAccessEventLogDO eventLog = convertToEventLog(event, captureUrl);
            Long eventId = eventLogService.saveEventLog(eventLog);
            eventLog.setId(eventId);

            log.info("[handleEvent] 事件保存成功: eventId={}, personId={}, personName={}",
                    eventId, eventLog.getPersonId(), eventLog.getPersonName());

            // 4. 推送到前端
            // 由 IotAccessEventLogService.saveEventLog 统一推送（避免重复推送/统一格式）

        } catch (Exception e) {
            log.error("[handleEvent] 处理门禁事件失败: deviceId={}, error={}",
                    event.getDeviceId(), e.getMessage(), e);
        }
    }
    
    /**
     * 【关键】处理门状态变化事件
     * <p>当设备（通过官方页面或其他方式）进行常开/常闭操作时，需要同步到数据库</p>
     * 
     * @param event 事件消息
     */
    private void handleDoorStateChangeIfNeeded(AccessControlEventMessage event) {
        Map<String, Object> extData = event.getExtData();
        if (extData == null) {
            return;
        }
        
        // 检查是否需要同步门状态
        Object needSync = extData.get("needSyncDoorState");
        if (!Boolean.TRUE.equals(needSync)) {
            return;
        }
        
        String newDoorMode = (String) extData.get("newDoorMode");
        Object channelNoObj = extData.get("channelNo");
        if (newDoorMode == null || channelNoObj == null) {
            log.warn("[handleDoorStateChangeIfNeeded] 缺少必要参数: newDoorMode={}, channelNo={}", 
                    newDoorMode, channelNoObj);
            return;
        }
        
        int channelNo = channelNoObj instanceof Number ? ((Number) channelNoObj).intValue() : 0;
        Long deviceId = event.getDeviceId();
        
        log.info("[handleDoorStateChangeIfNeeded] 检测到外部门状态变化: deviceId={}, channelNo={}, newDoorMode={}", 
                deviceId, channelNo, newDoorMode);
        
        try {
            // 查找对应的通道
            IotDeviceChannelDO channel = channelService.getChannelByDeviceIdAndChannelNo(deviceId, channelNo);
            if (channel == null) {
                log.warn("[handleDoorStateChangeIfNeeded] 未找到通道: deviceId={}, channelNo={}", 
                        deviceId, channelNo);
                return;
            }
            
            // 更新通道配置中的门模式
            Map<String, Object> config = channel.getConfig();
            if (config == null) {
                config = new HashMap<>();
            }
            
            String oldDoorMode = (String) config.get("doorMode");
            if (newDoorMode.equals(oldDoorMode)) {
                log.debug("[handleDoorStateChangeIfNeeded] 门模式未变化，跳过更新: deviceId={}, channelNo={}, mode={}", 
                        deviceId, channelNo, newDoorMode);
                return;
            }
            
            // 更新门模式
            config.put("doorMode", newDoorMode);
            config.put("alwaysMode", newDoorMode);
            int alwaysModeCode = "常开".equals(newDoorMode) ? 1 : ("常闭".equals(newDoorMode) ? 2 : 0);
            config.put("alwaysModeCode", alwaysModeCode);
            
            channel.setConfig(config);
            channelService.updateChannelConfig(channel.getId(), config);
            
            log.info("[handleDoorStateChangeIfNeeded] ✅ 门状态同步成功: deviceId={}, channelNo={}, oldMode={}, newMode={}", 
                    deviceId, channelNo, oldDoorMode, newDoorMode);
            
        } catch (Exception e) {
            log.error("[handleDoorStateChangeIfNeeded] 同步门状态失败: deviceId={}, channelNo={}, error={}", 
                    deviceId, channelNo, e.getMessage(), e);
        }
    }
    
    /**
     * 【关键】实时推送门状态变化到前端 WebSocket
     * <p>
     * 当设备上报门状态变化事件时（如远程开门、关门等），通过 WebSocket 实时通知前端更新门状态。
     * 这样用户可以在界面上看到门的实时开关状态变化。
     * </p>
     * 
     * <p>支持的事件类型（alarmType）：</p>
     * <ul>
     *   <li>12673 - 远程开门事件：门状态变为"打开"</li>
     *   <li>12658 - 关门事件：门状态变为"关闭"</li>
     *   <li>12300 - 门状态变化事件</li>
     *   <li>12675 - 远程常开：锁状态变为"常开"</li>
     *   <li>12676 - 远程常闭：锁状态变为"常闭"</li>
     *   <li>12677 - 取消常开/常闭：锁状态变为"正常"</li>
     * </ul>
     * 
     * @param event 门禁事件消息
     */
    private void pushDoorStateChangeToWebSocket(AccessControlEventMessage event) {
        Map<String, Object> extData = event.getExtData();
        if (extData == null) {
            return;
        }
        
        // 获取报警类型
        Object alarmTypeObj = extData.get("alarmType");
        if (alarmTypeObj == null) {
            return;
        }
        
        int alarmType = alarmTypeObj instanceof Number ? ((Number) alarmTypeObj).intValue() : 0;
        
        // 只处理门状态相关的报警类型
        if (!isDoorStateRelatedAlarm(alarmType)) {
            return;
        }
        
        Long deviceId = event.getDeviceId();
        Object channelNoObj = extData.get("channelNo");
        int channelNo = channelNoObj instanceof Number ? ((Number) channelNoObj).intValue() : 0;
        
        try {
            // 查找设备信息
            IotDeviceDO device = deviceService.getDevice(deviceId);
            if (device == null) {
                log.warn("[pushDoorStateChangeToWebSocket] 未找到设备: deviceId={}", deviceId);
                return;
            }
            // 优先使用事件中的 deviceType（String 格式，如 "ACCESS_GEN1"），
            // 否则从设备表的 deviceType（Integer）转换
            String deviceType = event.getDeviceType();
            if (deviceType == null && device.getDeviceType() != null) {
                deviceType = String.valueOf(device.getDeviceType());
            }
            
            // 查找通道信息
            IotDeviceChannelDO channel = null;
            if (channelNo > 0) {
                channel = channelService.getChannelByDeviceIdAndChannelNo(deviceId, channelNo);
            } else {
                // channelNo 为 0 时，尝试获取第一个通道
                List<IotDeviceChannelDO> channels = channelService.getChannelsByDeviceId(deviceId);
                if (channels != null && !channels.isEmpty()) {
                    channel = channels.get(0);
                    channelNo = channel.getChannelNo();
                }
            }
            
            if (channel == null) {
                log.debug("[pushDoorStateChangeToWebSocket] 未找到通道，跳过推送: deviceId={}, channelNo={}", 
                        deviceId, channelNo);
                return;
            }
            
            // 根据报警类型计算门状态
            DoorStateInfo stateInfo = parseDoorStateFromAlarm(alarmType, channel);
            
            // 推送门状态变化到前端
            deviceMessagePushService.pushDoorStateChange(
                    deviceId,
                    deviceType,
                    channel.getId(),
                    channelNo,
                    stateInfo.doorStatus,
                    stateInfo.lockStatus,
                    stateInfo.alwaysMode,
                    stateInfo.action
            );
            
            log.info("[pushDoorStateChangeToWebSocket] 📡 实时推送门状态: deviceId={}, channelNo={}, alarmType={}, " +
                    "doorStatus={}, lockStatus={}, alwaysMode={}, action={}", 
                    deviceId, channelNo, alarmType, 
                    stateInfo.doorStatus, stateInfo.lockStatus, stateInfo.alwaysMode, stateInfo.action);
            
        } catch (Exception e) {
            log.warn("[pushDoorStateChangeToWebSocket] 推送门状态变化失败: deviceId={}, alarmType={}, error={}",
                    deviceId, alarmType, e.getMessage(), e);
            // 推送失败不影响主流程
        }
    }
    
    /**
     * 判断是否是门状态相关的报警类型
     */
    private boolean isDoorStateRelatedAlarm(int alarmType) {
        return alarmType == ALARM_REMOTE_OPEN_DOOR      // 远程开门
            || alarmType == ALARM_DOOR_CLOSED           // 关门
            || alarmType == ALARM_DOOR_STATE_CHANGE     // 门状态变化
            || alarmType == ALARM_ALWAYS_OPEN_START     // 常开开始
            || alarmType == ALARM_ALWAYS_OPEN_END       // 常开结束
            || alarmType == ALARM_ALWAYS_CLOSED_START   // 常闭开始
            || alarmType == ALARM_ALWAYS_CLOSED_END     // 常闭结束
            || alarmType == ALARM_REMOTE_ALWAYS_OPEN    // 远程常开
            || alarmType == ALARM_REMOTE_ALWAYS_CLOSED  // 远程常闭
            || alarmType == ALARM_CANCEL_ALWAYS;        // 取消常开常闭
    }
    
    /**
     * 根据报警类型解析门状态信息
     * 
     * @param alarmType 报警类型
     * @param channel   通道信息（用于获取当前配置状态）
     * @return 门状态信息
     */
    private DoorStateInfo parseDoorStateFromAlarm(int alarmType, IotDeviceChannelDO channel) {
        DoorStateInfo info = new DoorStateInfo();
        
        // 从通道配置中获取当前的常开/常闭模式
        Map<String, Object> config = channel.getConfig();
        int currentAlwaysMode = 0; // 默认正常
        if (config != null) {
            Object alwaysModeCode = config.get("alwaysModeCode");
            if (alwaysModeCode instanceof Number) {
                currentAlwaysMode = ((Number) alwaysModeCode).intValue();
            }
        }
        
        switch (alarmType) {
            case ALARM_REMOTE_OPEN_DOOR:
                // 远程开门：门状态=打开，锁状态=已解锁
                info.doorStatus = 1;    // 打开
                info.lockStatus = 1;    // 已解锁
                info.alwaysMode = currentAlwaysMode;
                info.action = "OPEN_DOOR";
                break;
                
            case ALARM_DOOR_CLOSED:
                // 关门：门状态=关闭，锁状态=已锁
                info.doorStatus = 0;    // 关闭
                info.lockStatus = 0;    // 已锁
                info.alwaysMode = currentAlwaysMode;
                info.action = "CLOSE_DOOR";
                break;
                
            case ALARM_DOOR_STATE_CHANGE:
                // 门状态变化：通常是门磁检测到的物理状态变化，需要根据实际情况判断
                // 默认设为关闭状态
                info.doorStatus = 0;
                info.lockStatus = 0;
                info.alwaysMode = currentAlwaysMode;
                info.action = "STATE_CHANGE";
                break;
                
            case ALARM_ALWAYS_OPEN_START:
            case ALARM_REMOTE_ALWAYS_OPEN:
                // 常开模式：门状态=打开，锁状态=已解锁，模式=常开
                info.doorStatus = 1;
                info.lockStatus = 1;
                info.alwaysMode = 1;    // 常开
                info.action = "ALWAYS_OPEN";
                break;
                
            case ALARM_ALWAYS_CLOSED_START:
            case ALARM_REMOTE_ALWAYS_CLOSED:
                // 常闭模式：门状态=关闭，锁状态=已锁，模式=常闭
                info.doorStatus = 0;
                info.lockStatus = 0;
                info.alwaysMode = 2;    // 常闭
                info.action = "ALWAYS_CLOSED";
                break;
                
            case ALARM_ALWAYS_OPEN_END:
            case ALARM_ALWAYS_CLOSED_END:
            case ALARM_CANCEL_ALWAYS:
                // 取消常开/常闭：只恢复控制模式，门状态和锁状态保持不变（不推送）
                // 这个事件是设备自动上报的，表示门从某个特殊状态恢复到正常模式
                // 不应该改变门的物理状态
                info.doorStatus = null;  // null 表示不更新
                info.lockStatus = null;  // null 表示不更新
                info.alwaysMode = 0;    // 正常
                info.action = "CANCEL_ALWAYS";
                break;
                
            default:
                // 未知类型，使用默认值
                info.doorStatus = 2;    // 未知
                info.lockStatus = 2;    // 未知
                info.alwaysMode = currentAlwaysMode;
                info.action = "UNKNOWN";
        }
        
        return info;
    }
    
    /**
     * 门状态信息封装类
     */
    private static class DoorStateInfo {
        Integer doorStatus;     // 0-关闭, 1-打开, 2-未知, null-不更新
        Integer lockStatus;     // 0-已锁, 1-已解锁, 2-未知, null-不更新
        Integer alwaysMode;     // 0-正常, 1-常开, 2-常闭
        String action;          // 操作类型
    }

    /**
     * 关联人员信息
     * 
     * <p>根据 personId 或 cardNo 查询人员信息并填充到事件消息中</p>
     * 
     * @param event 事件消息
     * Requirements: 3.2
     */
    private void enrichPersonInfo(AccessControlEventMessage event) {
        // 如果已有人员信息，直接返回
        if (StrUtil.isNotBlank(event.getPersonName())) {
            return;
        }

        IotAccessPersonDO person = null;

        // 1. 优先通过 personId 查询
        if (StrUtil.isNotBlank(event.getPersonId())) {
            try {
                Long personIdLong = Long.parseLong(event.getPersonId());
                person = personMapper.selectById(personIdLong);
            } catch (NumberFormatException e) {
                // personId 可能是人员编号，尝试通过编号查询
                person = personMapper.selectByPersonCode(event.getPersonId());
            }
        }

        // 2. 如果没找到，通过卡号查询
        if (person == null && StrUtil.isNotBlank(event.getCardNo())) {
            IotAccessPersonCredentialDO credential = credentialMapper.selectByCardNo(event.getCardNo());
            if (credential != null) {
                person = personMapper.selectById(credential.getPersonId());
            }
        }

        // 3. 填充人员信息
        if (person != null) {
            event.setPersonId(String.valueOf(person.getId()));
            event.setPersonName(person.getPersonName());
            log.debug("[enrichPersonInfo] 关联人员成功: personId={}, personName={}",
                    person.getId(), person.getPersonName());
        } else {
            log.debug("[enrichPersonInfo] 未找到关联人员: eventPersonId={}, cardNo={}",
                    event.getPersonId(), event.getCardNo());
        }
    }

    /**
     * 保存抓拍图片
     * 
     * <p>将 Base64 编码的图片数据保存到文件系统</p>
     * 
     * @param event 事件消息
     * @return 图片URL，如果没有图片则返回 null
     * Requirements: 3.3
     */
    private String saveSnapshotImage(AccessControlEventMessage event) {
        // 如果已有 captureUrl，直接返回
        if (StrUtil.isNotBlank(event.getCaptureUrl())) {
            return event.getCaptureUrl();
        }

        // 如果没有 Base64 图片数据，返回 null
        if (StrUtil.isBlank(event.getSnapshotBase64())) {
            return null;
        }

        try {
            // 解码 Base64 图片数据
            byte[] imageData = Base64.getDecoder().decode(event.getSnapshotBase64());

            // 生成文件名：设备ID_时间戳_UUID.jpg
            String fileName = String.format("%d_%s_%s.jpg",
                    event.getDeviceId(),
                    event.getEventTime() != null ? 
                            event.getEventTime().toString().replace(":", "-").replace("T", "_") : 
                            System.currentTimeMillis(),
                    UUID.randomUUID().toString().substring(0, 8));

            // 保存文件
            String captureUrl = fileApi.createFile(imageData, fileName, CAPTURE_DIRECTORY, "image/jpeg");

            log.info("[saveSnapshotImage] 抓拍图片保存成功: fileName={}, url={}", fileName, captureUrl);
            return captureUrl;

        } catch (Exception e) {
            log.error("[saveSnapshotImage] 保存抓拍图片失败: deviceId={}, error={}",
                    event.getDeviceId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 转换事件消息为数据库实体
     * 
     * @param event 事件消息
     * @param captureUrl 抓拍图片URL
     * @return 事件日志实体
     * Requirements: 3.4
     */
    private IotAccessEventLogDO convertToEventLog(AccessControlEventMessage event, String captureUrl) {
        // 获取设备名称
        String deviceName = null;
        if (event.getDeviceId() != null) {
            try {
                IotDeviceDO device = deviceService.getDevice(event.getDeviceId());
                if (device != null) {
                    deviceName = device.getDeviceName();
                }
            } catch (Exception e) {
                log.warn("[convertToEventLog] 获取设备名称失败: deviceId={}, error={}", 
                    event.getDeviceId(), e.getMessage());
            }
        }

        // 获取通道名称
        String channelName = null;
        Long channelId = event.getChannelNo() != null ? event.getChannelNo().longValue() : null;
        if (event.getDeviceId() != null && event.getChannelNo() != null) {
            try {
                List<IotDeviceChannelDO> channels = channelService.getChannelsByDeviceId(event.getDeviceId());
                if (channels != null) {
                    for (IotDeviceChannelDO channel : channels) {
                        if (channel.getChannelNo() != null && channel.getChannelNo().equals(event.getChannelNo())) {
                            channelId = channel.getId();
                            channelName = channel.getChannelName();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("[convertToEventLog] 获取通道名称失败: deviceId={}, channelNo={}, error={}", 
                    event.getDeviceId(), event.getChannelNo(), e.getMessage());
            }
        }

        // 转换事件类型
        String eventTypeCode = convertEventType(event.getEventType());
        String eventTypeName = getEventTypeName(eventTypeCode);
        
        // 确定验证结果描述
        String verifyResultDesc = null;
        if (event.getVerifyResult() != null) {
            verifyResultDesc = event.getVerifyResult() == 1 ? "验证成功" : "验证失败";
        }

        return IotAccessEventLogDO.builder()
                .deviceId(event.getDeviceId())
                .deviceName(deviceName)
                .channelId(channelId)
                .channelName(channelName)
                .eventType(eventTypeCode)
                .eventDesc(eventTypeName)
                .eventTime(event.getEventTime() != null ? event.getEventTime() : LocalDateTime.now())
                .personId(parsePersonId(event.getPersonId()))
                .personName(event.getPersonName())
                .cardNo(event.getCardNo())
                .verifyMode(convertVerifyMode(event.getVerifyMode()))
                .verifyResult(event.getVerifyResult())
                .verifyResultDesc(verifyResultDesc)
                .captureUrl(captureUrl)
                .snapshotUrl(captureUrl)
                .success(event.getVerifyResult() != null && event.getVerifyResult() == 1)
                .build();
    }

    /**
     * 解析人员ID
     */
    private Long parsePersonId(String personId) {
        if (StrUtil.isBlank(personId)) {
            return null;
        }
        try {
            return Long.parseLong(personId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 转换事件类型
     * 
     * <p>将 AccessControlEventMessage.EventType 整数值转换为 AccessEventTypeEnum 字符串代码</p>
     * 
     * @param eventType 事件类型整数值
     * @return 事件类型字符串代码
     * Requirements: 8.3
     */
    private String convertEventType(Integer eventType) {
        if (eventType == null) {
            return "UNKNOWN";
        }
        return switch (eventType) {
            // 正常开门事件
            case AccessControlEventMessage.EventType.CARD -> "CARD_SWIPE";
            case AccessControlEventMessage.EventType.PASSWORD -> "PASSWORD";
            case AccessControlEventMessage.EventType.FINGERPRINT -> "FINGERPRINT";
            case AccessControlEventMessage.EventType.FACE -> "FACE_RECOGNIZE";
            case AccessControlEventMessage.EventType.QR_CODE -> "QRCODE";
            case AccessControlEventMessage.EventType.REMOTE_OPEN -> "REMOTE_OPEN";
            case AccessControlEventMessage.EventType.BUTTON_OPEN -> "BUTTON_OPEN";
            case AccessControlEventMessage.EventType.MULTI_PERSON_OPEN -> "MULTI_PERSON_OPEN";
            // 报警事件
            case AccessControlEventMessage.EventType.DOOR_NOT_CLOSED -> "DOOR_NOT_CLOSED";
            case AccessControlEventMessage.EventType.BREAK_IN -> "BREAK_IN";
            case AccessControlEventMessage.EventType.REPEAT_ENTER -> "REPEAT_ENTER";
            case AccessControlEventMessage.EventType.FORCED_OPEN -> "DURESS"; // 强行开门/胁迫报警
            case AccessControlEventMessage.EventType.TAMPER_ALARM -> "TAMPER_ALARM";
            case AccessControlEventMessage.EventType.LOCAL_ALARM -> "LOCAL_ALARM";
            case AccessControlEventMessage.EventType.DOOR_SENSOR_ALARM -> "DOOR_NOT_CLOSED"; // 门磁报警归类为门未关
            // 状态事件
            case AccessControlEventMessage.EventType.ACCESS_STATUS -> "ACCESS_STATUS";
            case AccessControlEventMessage.EventType.FINGERPRINT_CAPTURE -> "FINGERPRINT_CAPTURE";
            default -> "UNKNOWN";
        };
    }

    /**
     * 获取事件类型名称
     * 
     * @param eventTypeCode 事件类型代码
     * @return 事件类型名称
     */
    private String getEventTypeName(String eventTypeCode) {
        if (eventTypeCode == null) {
            return "未知事件";
        }
        return switch (eventTypeCode) {
            case "CARD_SWIPE" -> "刷卡开门";
            case "PASSWORD" -> "密码开门";
            case "FINGERPRINT" -> "指纹开门";
            case "FACE_RECOGNIZE" -> "人脸开门";
            case "QRCODE" -> "二维码开门";
            case "REMOTE_OPEN" -> "远程开门";
            case "BUTTON_OPEN" -> "按钮开门";
            case "MULTI_PERSON_OPEN" -> "多人组合开门";
            case "DOOR_NOT_CLOSED" -> "门未关报警";
            case "BREAK_IN" -> "闯入报警";
            case "REPEAT_ENTER" -> "反复进入报警";
            case "MALICIOUS_OPEN" -> "恶意开门报警";
            case "DURESS" -> "胁迫报警";
            case "TAMPER_ALARM" -> "防拆报警";
            case "LOCAL_ALARM" -> "本地报警";
            case "ACCESS_STATUS" -> "门禁状态事件";
            case "FINGERPRINT_CAPTURE" -> "指纹采集事件";
            default -> "未知事件";
        };
    }

    /**
     * 转换验证方式
     * 
     * @param verifyMode 验证方式整数值
     * @return 验证方式字符串
     * Requirements: 8.1
     */
    private String convertVerifyMode(Integer verifyMode) {
        if (verifyMode == null) {
            return null;
        }
        return switch (verifyMode) {
            case AccessControlEventMessage.VerifyMode.CARD -> "card";
            case AccessControlEventMessage.VerifyMode.PASSWORD -> "password";
            case AccessControlEventMessage.VerifyMode.FINGERPRINT -> "fingerprint";
            case AccessControlEventMessage.VerifyMode.FACE -> "face";
            case AccessControlEventMessage.VerifyMode.CARD_PASSWORD -> "card_password";
            case AccessControlEventMessage.VerifyMode.CARD_FINGERPRINT -> "card_fingerprint";
            case AccessControlEventMessage.VerifyMode.CARD_FACE -> "card_face";
            default -> "unknown";
        };
    }

}
