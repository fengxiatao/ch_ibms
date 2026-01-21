package cn.iocoder.yudao.module.iot.service.camera;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.camera.vo.IotCameraForGatewayRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.camera.vo.IotCameraPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.camera.vo.IotCameraSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.mysql.camera.IotCameraMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.CAMERA_NOT_EXISTS;

/**
 * IoT 摄像头管理 Service实现类
 *
 * @author 长辉信息
 */
@Service
@Validated
@Slf4j
public class IotCameraServiceImpl implements IotCameraService {

    @Resource
    private IotCameraMapper cameraMapper;
    
    @Resource
    private IotDeviceMapper deviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCamera(IotCameraSaveReqVO createReqVO) {
        // 插入摄像头配置
        IotCameraDO camera = IotCameraDO.builder()
                .deviceId(createReqVO.getDeviceId())
                .streamUrlMain(createReqVO.getStreamUrlMain())
                .streamUrlSub(createReqVO.getStreamUrlSub())
                .rtspPort(createReqVO.getRtspPort())
                .onvifPort(createReqVO.getOnvifPort())
                .username(createReqVO.getUsername())
                .password(createReqVO.getPassword()) // TODO: 需要加密
                .manufacturer(createReqVO.getManufacturer())
                .model(createReqVO.getModel())
                .ptzSupport(createReqVO.getPtzSupport())
                .audioSupport(createReqVO.getAudioSupport())
                .resolution(createReqVO.getResolution())
                .frameRate(createReqVO.getFrameRate())
                .bitRate(createReqVO.getBitRate())
                .presetCount(createReqVO.getPresetCount())
                .brightness(createReqVO.getBrightness())
                .contrast(createReqVO.getContrast())
                .saturation(createReqVO.getSaturation())
                .build();
        cameraMapper.insert(camera);
        
        log.info("[createCamera][摄像头配置创建成功，deviceId={}, id={}]", 
                createReqVO.getDeviceId(), camera.getId());
        
        return camera.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCamera(IotCameraSaveReqVO updateReqVO) {
        // 校验存在
        validateCameraExists(updateReqVO.getId());
        
        // 更新摄像头配置
        IotCameraDO updateObj = IotCameraDO.builder()
                .id(updateReqVO.getId())
                .deviceId(updateReqVO.getDeviceId())
                .streamUrlMain(updateReqVO.getStreamUrlMain())
                .streamUrlSub(updateReqVO.getStreamUrlSub())
                .rtspPort(updateReqVO.getRtspPort())
                .onvifPort(updateReqVO.getOnvifPort())
                .username(updateReqVO.getUsername())
                .password(updateReqVO.getPassword()) // TODO: 需要加密
                .manufacturer(updateReqVO.getManufacturer())
                .model(updateReqVO.getModel())
                .ptzSupport(updateReqVO.getPtzSupport())
                .audioSupport(updateReqVO.getAudioSupport())
                .resolution(updateReqVO.getResolution())
                .frameRate(updateReqVO.getFrameRate())
                .bitRate(updateReqVO.getBitRate())
                .presetCount(updateReqVO.getPresetCount())
                .brightness(updateReqVO.getBrightness())
                .contrast(updateReqVO.getContrast())
                .saturation(updateReqVO.getSaturation())
                .build();
        cameraMapper.updateById(updateObj);
        
        log.info("[updateCamera][摄像头配置更新成功，id={}]", updateReqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCamera(Long id) {
        // 校验存在
        validateCameraExists(id);
        
        // 删除摄像头配置
        cameraMapper.deleteById(id);
        
        log.info("[deleteCamera][摄像头配置删除成功，id={}]", id);
    }

    @Override
    public IotCameraDO getCamera(Long id) {
        return cameraMapper.selectById(id);
    }

    @Override
    public IotCameraDO getCameraByDeviceId(Long deviceId) {
        return cameraMapper.selectByDeviceId(deviceId);
    }

    @Override
    public PageResult<IotCameraDO> getCameraPage(IotCameraPageReqVO pageReqVO) {
        return cameraMapper.selectPage(
                pageReqVO,
                pageReqVO.getDeviceId(),
                pageReqVO.getManufacturer(),
                pageReqVO.getPtzSupport()
        );
    }

    @Override
    public Boolean testConnection(Long id) {
        IotCameraDO camera = validateCameraExists(id);
        
        // TODO: 实现实际的连接测试逻辑
        // 1. 通过ONVIF协议测试连接
        // 2. 或者通过RTSP流测试连接
        log.info("[testConnection][测试摄像头连接，id={}, ip={}]", id, camera.getStreamUrlMain());
        
        return true; // 暂时返回true
    }

    @Override
    public String getStreamUrl(Long id, String streamType) {
        IotCameraDO camera = validateCameraExists(id);
        
        if ("sub".equalsIgnoreCase(streamType)) {
            return camera.getStreamUrlSub();
        }
        return camera.getStreamUrlMain();
    }

    /**
     * 校验摄像头是否存在
     */
    private IotCameraDO validateCameraExists(Long id) {
        IotCameraDO camera = cameraMapper.selectById(id);
        if (camera == null) {
            throw exception(CAMERA_NOT_EXISTS);
        }
        return camera;
    }

    @Override
    public List<IotCameraForGatewayRespVO> listForGateway() {
        // 查询所有摄像头配置
        List<IotCameraDO> cameras = cameraMapper.selectList();
        
        if (cameras == null || cameras.isEmpty()) {
            log.info("[listForGateway] 没有摄像头设备");
            return new ArrayList<>();
        }
        
        // 提取所有设备ID
        List<Long> deviceIds = cameras.stream()
                .map(IotCameraDO::getDeviceId)
                .filter(Objects::nonNull)
                .toList();
        
        if (deviceIds.isEmpty()) {
            log.info("[listForGateway] 没有有效的设备ID");
            return new ArrayList<>();
        }
        
        // 批量查询设备信息（获取IP地址）
        List<IotDeviceDO> devices = deviceMapper.selectList("id", deviceIds);
        Map<Long, IotDeviceDO> deviceMap = devices.stream()
                .collect(Collectors.toMap(IotDeviceDO::getId, device -> device));
        
        // 组装返回结果
        List<IotCameraForGatewayRespVO> result = new ArrayList<>();
        for (IotCameraDO camera : cameras) {
            IotDeviceDO device = deviceMap.get(camera.getDeviceId());
            String ipAddress = DeviceConfigHelper.getIpAddress(device);
            if (device == null || ipAddress == null) {
                log.warn("[listForGateway] 设备不存在或IP为空，跳过: deviceId={}", camera.getDeviceId());
                continue;
            }
            
            IotCameraForGatewayRespVO vo = new IotCameraForGatewayRespVO();
            vo.setDeviceId(camera.getDeviceId());
            vo.setIpAddress(ipAddress);
            vo.setOnvifPort(camera.getOnvifPort() != null ? camera.getOnvifPort() : 80);
            vo.setUsername(camera.getUsername());
            vo.setPassword(camera.getPassword());
            
            result.add(vo);
        }
        
        log.info("[listForGateway] 返回 {} 个摄像头设备", result.size());
        return result;
    }

}

