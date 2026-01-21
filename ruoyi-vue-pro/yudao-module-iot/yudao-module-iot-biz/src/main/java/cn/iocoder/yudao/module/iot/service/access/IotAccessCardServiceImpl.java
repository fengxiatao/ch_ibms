package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.card.IotAccessCardAddReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.card.IotAccessCardRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.mq.manager.DeviceCommandResponseManager;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁卡信息管理 Service 实现类
 * 
 * <p>通过 DeviceCommandPublisher 发送命令到统一 Topic（DEVICE_SERVICE_INVOKE），
 * 并监听 DEVICE_SERVICE_RESULT 接收响应。</p>
 * 
 * <p>适配说明：</p>
 * <ul>
 *   <li>使用 DeviceCommandPublisher 替代直接发送到 ACCESS_CONTROL_DEVICE_COMMAND</li>
 *   <li>监听 DEVICE_SERVICE_RESULT 替代 ACCESS_CONTROL_DEVICE_RESPONSE</li>
 *   <li>命令类型: ADD_CARD, UPDATE_CARD, DELETE_CARD, LIST_CARDS, CLEAR_ALL_CARDS</li>
 * </ul>
 * 
 * <p>Requirements: 4.1, 4.2, 4.3</p>
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotAccessCardServiceImpl implements IotAccessCardService {

    /** 命令类型常量 */
    private static final String CMD_ADD_CARD = "ADD_CARD";
    private static final String CMD_UPDATE_CARD = "UPDATE_CARD";
    private static final String CMD_DELETE_CARD = "DELETE_CARD";
    private static final String CMD_LIST_CARDS = "LIST_CARDS";
    private static final String CMD_CLEAR_ALL_CARDS = "CLEAR_ALL_CARDS";

    /** 命令超时时间（秒） */
    private static final int COMMAND_TIMEOUT_SECONDS = 30;

    @Resource
    private DeviceCommandResponseManager responseManager;

    @Resource
    private IotDeviceMapper deviceMapper;

    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;

    /**
     * 根据设备配置获取设备类型
     */
    private String getDeviceType(IotDeviceDO device) {
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            return AccessDeviceTypeConstants.getDeviceType(config.getSupportVideo());
        }
        if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            Object deviceTypeObj = cfg.get("deviceType");
            Object supportVideoObj = cfg.get("supportVideo");
            String configDeviceType = deviceTypeObj != null ? deviceTypeObj.toString() : null;
            Boolean supportVideo = (supportVideoObj instanceof Boolean) ? (Boolean) supportVideoObj : null;
            return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);
        }
        return AccessDeviceTypeConstants.ACCESS_GEN1;
    }

    @Override
    public Boolean addCard(IotAccessCardAddReqVO reqVO) {
        // 1. 验证设备
        IotDeviceDO device = validateAccessDevice(reqVO.getDeviceId());
        String deviceType = getDeviceType(device);
        
        // 2. 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put("ipAddress", DeviceConfigHelper.getIpAddress(device));
        params.put("cardNo", reqVO.getCardNo());
        params.put("personId", reqVO.getPersonId());
        params.put("personName", reqVO.getPersonName());
        params.put("validStart", reqVO.getValidStart());
        params.put("validEnd", reqVO.getValidEnd());
        params.put("doorPermissions", reqVO.getDoorPermissions());
        params.put("tenantId", device.getTenantId());
        
        // 3. 发送命令并等待响应
        IotDeviceMessage response = sendCommandAndWait(deviceType, reqVO.getDeviceId(), CMD_ADD_CARD, params);
        
        if (response == null || !isSuccess(response)) {
            String errorMsg = response != null ? response.getMsg() : "命令超时";
            log.error("[addCard] 添加卡失败: deviceId={}, cardNo={}, error={}", 
                    reqVO.getDeviceId(), reqVO.getCardNo(), errorMsg);
            throw exception(ACCESS_CARD_ADD_FAILED);
        }
        
        log.info("[addCard] 添加卡成功: deviceId={}, cardNo={}", reqVO.getDeviceId(), reqVO.getCardNo());
        return true;
    }

    @Override
    public Boolean updateCard(Long deviceId, String cardNo, IotAccessCardAddReqVO reqVO) {
        // 1. 验证设备
        IotDeviceDO device = validateAccessDevice(deviceId);
        String deviceType = getDeviceType(device);
        
        // 2. 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put("ipAddress", DeviceConfigHelper.getIpAddress(device));
        params.put("cardNo", cardNo);
        params.put("personId", reqVO.getPersonId());
        params.put("personName", reqVO.getPersonName());
        params.put("validStart", reqVO.getValidStart());
        params.put("validEnd", reqVO.getValidEnd());
        params.put("doorPermissions", reqVO.getDoorPermissions());
        params.put("tenantId", device.getTenantId());
        
        // 3. 发送命令并等待响应
        IotDeviceMessage response = sendCommandAndWait(deviceType, deviceId, CMD_UPDATE_CARD, params);
        
        if (response == null || !isSuccess(response)) {
            String errorMsg = response != null ? response.getMsg() : "命令超时";
            log.error("[updateCard] 更新卡失败: deviceId={}, cardNo={}, error={}", 
                    deviceId, cardNo, errorMsg);
            throw exception(ACCESS_CARD_UPDATE_FAILED);
        }
        
        log.info("[updateCard] 更新卡成功: deviceId={}, cardNo={}", deviceId, cardNo);
        return true;
    }

    @Override
    public Boolean deleteCard(Long deviceId, String cardNo) {
        // 1. 验证设备
        IotDeviceDO device = validateAccessDevice(deviceId);
        String deviceType = getDeviceType(device);
        
        // 2. 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put("ipAddress", DeviceConfigHelper.getIpAddress(device));
        params.put("cardNo", cardNo);
        params.put("tenantId", device.getTenantId());
        
        // 3. 发送命令并等待响应
        IotDeviceMessage response = sendCommandAndWait(deviceType, deviceId, CMD_DELETE_CARD, params);
        
        if (response == null || !isSuccess(response)) {
            String errorMsg = response != null ? response.getMsg() : "命令超时";
            log.error("[deleteCard] 删除卡失败: deviceId={}, cardNo={}, error={}", 
                    deviceId, cardNo, errorMsg);
            throw exception(ACCESS_CARD_DELETE_FAILED);
        }
        
        log.info("[deleteCard] 删除卡成功: deviceId={}, cardNo={}", deviceId, cardNo);
        return true;
    }

    @Override
    public List<IotAccessCardRespVO> listCards(Long deviceId) {
        // 1. 验证设备
        IotDeviceDO device = validateAccessDevice(deviceId);
        String deviceType = getDeviceType(device);
        
        // 2. 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put("ipAddress", DeviceConfigHelper.getIpAddress(device));
        params.put("tenantId", device.getTenantId());
        
        // 3. 发送命令并等待响应
        IotDeviceMessage response = sendCommandAndWait(deviceType, deviceId, CMD_LIST_CARDS, params);
        
        if (response == null || !isSuccess(response)) {
            String errorMsg = response != null ? response.getMsg() : "命令超时";
            log.error("[listCards] 查询卡列表失败: deviceId={}, error={}", deviceId, errorMsg);
            throw exception(ACCESS_CARD_QUERY_FAILED);
        }
        
        // 4. 转换响应数据
        List<IotAccessCardRespVO> result = new ArrayList<>();
        // TODO: 从response中解析卡列表数据
        
        log.info("[listCards] 查询卡列表成功: deviceId={}, count={}", deviceId, result.size());
        return result;
    }

    @Override
    public Boolean clearAllCards(Long deviceId) {
        // 1. 验证设备
        IotDeviceDO device = validateAccessDevice(deviceId);
        String deviceType = getDeviceType(device);
        
        // 2. 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put("ipAddress", DeviceConfigHelper.getIpAddress(device));
        params.put("tenantId", device.getTenantId());
        
        // 3. 发送命令并等待响应
        IotDeviceMessage response = sendCommandAndWait(deviceType, deviceId, CMD_CLEAR_ALL_CARDS, params);
        
        if (response == null || !isSuccess(response)) {
            String errorMsg = response != null ? response.getMsg() : "命令超时";
            log.error("[clearAllCards] 清空卡失败: deviceId={}, error={}", deviceId, errorMsg);
            throw exception(ACCESS_CARD_CLEAR_FAILED);
        }
        
        log.info("[clearAllCards] 清空卡成功: deviceId={}", deviceId);
        return true;
    }

    /**
     * 发送命令并等待响应
     */
    /**
     * 发送命令并等待响应
     * 
     * <p>使用统一的 DeviceCommandResponseManager 来等待响应，
     * 响应由 DeviceServiceResultConsumer 统一处理并通知管理器。</p>
     */
    private IotDeviceMessage sendCommandAndWait(String deviceType, Long deviceId, 
                                                  String commandType, Map<String, Object> params) {
        try {
            // 1. 发送命令并获取 requestId
            String requestId = deviceCommandPublisher.publishCommand(deviceType, deviceId, commandType, params);
            
            // 2. 注册请求到响应管理器
            responseManager.registerRequest(requestId);
            
            // 3. 等待响应
            return responseManager.waitForResponse(requestId, COMMAND_TIMEOUT_SECONDS);
        } catch (TimeoutException e) {
            log.error("[sendCommandAndWait] 命令超时: deviceType={}, deviceId={}, commandType={}", 
                    deviceType, deviceId, commandType);
            return null;
        } catch (Exception e) {
            log.error("[sendCommandAndWait] 命令执行失败: deviceType={}, deviceId={}, commandType={}, error={}", 
                    deviceType, deviceId, commandType, e.getMessage());
            return null;
        }
    }

    /**
     * 判断响应是否成功
     */
    private boolean isSuccess(IotDeviceMessage response) {
        Integer code = response.getCode();
        return code != null && code == 0;
    }

    private IotDeviceDO validateAccessDevice(Long deviceId) {
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }
        if (!"access".equals(device.getSubsystemCode())) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }
        return device;
    }

}
