package cn.iocoder.yudao.module.iot.service.access;

import cn.hutool.core.util.StrUtil;
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

    /** 抓拍图片存储目录 */
    private static final String CAPTURE_DIRECTORY = "access/capture";

    @Override
    public void handleEvent(AccessControlEventMessage event) {
        if (event == null) {
            log.warn("[handleEvent] 收到空事件消息，忽略处理");
            return;
        }

        log.info("[handleEvent] 开始处理门禁事件: deviceId={}, eventType={}, eventTime={}",
                event.getDeviceId(), event.getEventType(), event.getEventTime());

        try {
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
