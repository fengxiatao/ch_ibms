package cn.iocoder.yudao.module.iot.service.device.config;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.device.config.vo.*;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.device.OnvifConfigMessage;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;

/**
 * IoT 设备配置服务实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class IotDeviceConfigServiceImpl implements IotDeviceConfigService {

    @Resource
    private IotDeviceMapper deviceMapper;
    @Resource
    private IotMessageBus messageBus;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // Redis Key前缀
    private static final String REDIS_KEY_DEVICE_CONFIG = "iot:device:config:";
    // 配置缓存过期时间（小时）
    private static final int CONFIG_CACHE_EXPIRE_HOURS = 24;

    @Override
    public DeviceConfigRespVO getDeviceConfig(Long deviceId) {
        // 1. 校验设备存在
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 2. 尝试从Redis缓存获取
        String cacheKey = REDIS_KEY_DEVICE_CONFIG + deviceId;
        String cachedConfig = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedConfig != null) {
            return JsonUtils.parseObject(cachedConfig, DeviceConfigRespVO.class);
        }

        // 3. 从数据库/设备配置中构建默认配置
        DeviceConfigRespVO respVO = buildDefaultConfig(device);

        // 4. 保存到Redis缓存
        stringRedisTemplate.opsForValue().set(
                cacheKey,
                JsonUtils.toJsonString(respVO),
                CONFIG_CACHE_EXPIRE_HOURS,
                TimeUnit.HOURS
        );

        return respVO;
    }

    @Override
    public void updateNetworkConfig(Long deviceId, DeviceNetworkConfigReqVO reqVO) {
        log.info("[updateNetworkConfig] 更新设备网络配置: deviceId={}, config={}", deviceId, reqVO);

        // 1. 获取当前配置
        DeviceConfigRespVO currentConfig = getDeviceConfig(deviceId);

        // 2. 更新网络配置
        DeviceConfigRespVO.NetworkConfig networkConfig = new DeviceConfigRespVO.NetworkConfig();
        networkConfig.setIpAddress(reqVO.getIpAddress());
        networkConfig.setSubnetMask(reqVO.getSubnetMask());
        networkConfig.setGateway(reqVO.getGateway());
        networkConfig.setDns(reqVO.getDns());
        networkConfig.setDhcpEnabled(reqVO.getDhcpEnabled());
        networkConfig.setHttpPort(reqVO.getHttpPort());
        networkConfig.setRtspPort(reqVO.getRtspPort());
        networkConfig.setOnvifPort(reqVO.getOnvifPort());

        currentConfig.setNetworkConfig(networkConfig);

        // 3. 更新缓存
        updateConfigCache(deviceId, currentConfig);

        log.info("[updateNetworkConfig] 网络配置更新成功: deviceId={}", deviceId);
    }

    @Override
    public void updateEventConfig(Long deviceId, DeviceEventConfigReqVO reqVO) {
        log.info("[updateEventConfig] 更新设备事件配置: deviceId={}, config={}", deviceId, reqVO);

        // 1. 获取当前配置
        DeviceConfigRespVO currentConfig = getDeviceConfig(deviceId);

        // 2. 更新事件配置
        DeviceConfigRespVO.EventConfig eventConfig = new DeviceConfigRespVO.EventConfig();
        eventConfig.setMotionDetectionEnabled(reqVO.getMotionDetectionEnabled());
        eventConfig.setMotionSensitivity(reqVO.getMotionSensitivity());
        eventConfig.setVideoLossEnabled(reqVO.getVideoLossEnabled());
        eventConfig.setTamperDetectionEnabled(reqVO.getTamperDetectionEnabled());
        eventConfig.setAudioDetectionEnabled(reqVO.getAudioDetectionEnabled());
        eventConfig.setAudioThreshold(reqVO.getAudioThreshold());

        currentConfig.setEventConfig(eventConfig);

        // 3. 更新缓存
        updateConfigCache(deviceId, currentConfig);

        log.info("[updateEventConfig] 事件配置更新成功: deviceId={}", deviceId);
    }

    @Override
    public void updateVideoConfig(Long deviceId, DeviceVideoConfigReqVO reqVO) {
        log.info("[updateVideoConfig] 更新设备视频配置: deviceId={}, config={}", deviceId, reqVO);

        // 1. 获取当前配置
        DeviceConfigRespVO currentConfig = getDeviceConfig(deviceId);

        // 2. 更新视频配置
        DeviceConfigRespVO.VideoConfig videoConfig = new DeviceConfigRespVO.VideoConfig();
        videoConfig.setResolution(reqVO.getResolution());
        videoConfig.setFrameRate(reqVO.getFrameRate());
        videoConfig.setBitrate(reqVO.getBitrate());
        videoConfig.setCodecType(reqVO.getCodecType());
        videoConfig.setQuality(reqVO.getQuality());
        videoConfig.setGopLength(reqVO.getGopLength());

        currentConfig.setVideoConfig(videoConfig);

        // 3. 更新缓存
        updateConfigCache(deviceId, currentConfig);

        log.info("[updateVideoConfig] 视频配置更新成功: deviceId={}", deviceId);
    }

    @Override
    public DeviceConfigRespVO syncConfigFromDevice(Long deviceId) {
        log.info("[syncConfigFromDevice] 从设备同步配置: deviceId={}", deviceId);

        // 1. 校验设备存在
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 2. 发布配置同步请求到Gateway
        OnvifConfigMessage syncRequest = new OnvifConfigMessage();
        syncRequest.setRequestId(UUID.randomUUID().toString());
        syncRequest.setTenantId(device.getTenantId());
        syncRequest.setDeviceId(deviceId);
        syncRequest.setAction("sync_from_device");

        // 使用统一通道
        messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, syncRequest);

        // 3. 等待同步结果（实际应使用异步回调）
        // TODO: 实现异步配置同步机制
        log.info("[syncConfigFromDevice] 配置同步请求已发送，等待Gateway处理: deviceId={}", deviceId);

        // 4. 返回当前缓存的配置（实际应返回同步后的配置）
        return getDeviceConfig(deviceId);
    }

    @Override
    public void applyConfigToDevice(Long deviceId) {
        log.info("[applyConfigToDevice] 应用配置到设备: deviceId={}", deviceId);

        // 1. 获取当前配置
        DeviceConfigRespVO currentConfig = getDeviceConfig(deviceId);

        // 2. 发布配置应用请求到Gateway
        OnvifConfigMessage applyRequest = new OnvifConfigMessage();
        applyRequest.setRequestId(UUID.randomUUID().toString());
        applyRequest.setTenantId(TenantContextHolder.getTenantId());
        applyRequest.setDeviceId(deviceId);
        applyRequest.setAction("apply_to_device");
        applyRequest.setConfigData(JsonUtils.toJsonString(currentConfig));

        // 使用统一通道
        messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, applyRequest);

        log.info("[applyConfigToDevice] 配置应用请求已发送: deviceId={}", deviceId);
    }

    @Override
    public void resetDeviceConfig(Long deviceId) {
        log.info("[resetDeviceConfig] 重置设备配置: deviceId={}", deviceId);

        // 1. 删除Redis缓存
        stringRedisTemplate.delete(REDIS_KEY_DEVICE_CONFIG + deviceId);

        // 2. 重新构建默认配置
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device != null) {
            DeviceConfigRespVO defaultConfig = buildDefaultConfig(device);
            updateConfigCache(deviceId, defaultConfig);
        }

        log.info("[resetDeviceConfig] 设备配置已重置: deviceId={}", deviceId);
    }

    @Override
    public DeviceCapabilitiesRespVO getDeviceCapabilities(Long deviceId) {
        log.info("[getDeviceCapabilities] 获取设备能力集: deviceId={}", deviceId);

        // 1. 校验设备存在
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 2. 构建设备能力集（实际应从ONVIF设备查询）
        DeviceCapabilitiesRespVO capabilities = new DeviceCapabilitiesRespVO();
        capabilities.setDeviceId(deviceId);
        capabilities.setManufacturer("Dahua"); // TODO: 从设备读取
        capabilities.setModel("IPC-HFW1230S"); // TODO: 从设备读取
        capabilities.setFirmwareVersion("2.840.0000000.18.R"); // TODO: 从设备读取

        // 支持的分辨率列表
        capabilities.setSupportedResolutions(Arrays.asList(
                "1920x1080", "1280x720", "704x576", "640x480"
        ));

        // 支持的编码格式
        capabilities.setSupportedCodecs(Arrays.asList(
                "H.264", "H.265", "MJPEG"
        ));

        capabilities.setMaxFrameRate(30);
        capabilities.setPtzSupported(false);
        capabilities.setAudioSupported(true);
        capabilities.setMotionDetectionSupported(true);
        capabilities.setTamperDetectionSupported(true);
        capabilities.setDhcpSupported(true);
        capabilities.setRecordingSupported(true);

        // TODO: 查询实际的媒体配置文件列表

        return capabilities;
    }

    /**
     * 构建默认配置
     */
    private DeviceConfigRespVO buildDefaultConfig(IotDeviceDO device) {
        DeviceConfigRespVO config = new DeviceConfigRespVO();
        config.setDeviceId(device.getId());
        config.setDeviceName(device.getDeviceName());

        // 网络配置（从 config 中获取 IP 地址）
        DeviceConfigRespVO.NetworkConfig networkConfig = new DeviceConfigRespVO.NetworkConfig();
        networkConfig.setIpAddress(DeviceConfigHelper.getIpAddress(device));
        networkConfig.setSubnetMask("255.255.255.0");
        networkConfig.setGateway("192.168.1.1");
        networkConfig.setDns("8.8.8.8");
        networkConfig.setDhcpEnabled(false);
        networkConfig.setHttpPort(80);
        networkConfig.setRtspPort(554);
        networkConfig.setOnvifPort(80);  // 大部分新设备使用 80 端口
        config.setNetworkConfig(networkConfig);

        // 事件配置
        DeviceConfigRespVO.EventConfig eventConfig = new DeviceConfigRespVO.EventConfig();
        eventConfig.setMotionDetectionEnabled(true);
        eventConfig.setMotionSensitivity(50);
        eventConfig.setVideoLossEnabled(true);
        eventConfig.setTamperDetectionEnabled(true);
        eventConfig.setAudioDetectionEnabled(false);
        eventConfig.setAudioThreshold(30);
        config.setEventConfig(eventConfig);

        // 视频配置
        DeviceConfigRespVO.VideoConfig videoConfig = new DeviceConfigRespVO.VideoConfig();
        videoConfig.setResolution("1920x1080");
        videoConfig.setFrameRate(25);
        videoConfig.setBitrate(4096);
        videoConfig.setCodecType("H.264");
        videoConfig.setQuality(80);
        videoConfig.setGopLength(50);
        config.setVideoConfig(videoConfig);

        return config;
    }

    /**
     * 更新配置缓存
     */
    private void updateConfigCache(Long deviceId, DeviceConfigRespVO config) {
        String cacheKey = REDIS_KEY_DEVICE_CONFIG + deviceId;
        stringRedisTemplate.opsForValue().set(
                cacheKey,
                JsonUtils.toJsonString(config),
                CONFIG_CACHE_EXPIRE_HOURS,
                TimeUnit.HOURS
        );
    }
}


