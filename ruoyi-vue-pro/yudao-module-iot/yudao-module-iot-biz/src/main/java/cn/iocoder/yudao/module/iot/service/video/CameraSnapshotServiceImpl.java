package cn.iocoder.yudao.module.iot.service.video;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraSnapshotPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraSnapshotRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraSnapshotDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.camera.IotCameraSnapshotMapper;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.springframework.web.multipart.MultipartFile;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * 摄像头抓图记录 Service实现类
 *
 * @author 长辉信息
 */
@Slf4j
@Service
public class CameraSnapshotServiceImpl implements CameraSnapshotService {

    @Resource
    private IotCameraSnapshotMapper cameraSnapshotMapper;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotDeviceChannelService channelService;

    @Resource
    private FileApi fileApi;

    /**
     * 快照文件存储目录（关联基础设施模块）
     * 默认：/snapshots/{deviceId}/{yyyyMMdd}/
     */
    @Value("${iot.snapshot.directory:/snapshots}")
    private String snapshotDirectory;

    /**
     * 快照文件URL前缀（HTTP访问地址）
     * 例如：http://192.168.1.246:8080/snapshots
     */
    @Value("${iot.snapshot.url-prefix:http://192.168.1.246:8080/snapshots}")
    private String snapshotUrlPrefix;

    @Override
    public PageResult<CameraSnapshotRespVO> getSnapshotPage(CameraSnapshotPageReqVO pageReqVO) {
        // 1. 分页查询抓图记录
        PageResult<IotCameraSnapshotDO> pageResult = cameraSnapshotMapper.selectPage(
                pageReqVO,
                pageReqVO.getDeviceId(),
                pageReqVO.getChannelId(),
                pageReqVO.getChannelIds(),
                pageReqVO.getSnapshotType(),
                pageReqVO.getEventType(),
                pageReqVO.getStartTime(),
                pageReqVO.getEndTime(),
                pageReqVO.getIsProcessed()
        );

        // 2. 转换为VO
        List<CameraSnapshotRespVO> voList = convertToVO(pageResult.getList());

        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadSnapshot(Long channelId, Integer snapshotType, MultipartFile file) throws Exception {
        // 1. 查询通道信息
        IotDeviceChannelDO channel = channelService.getChannel(channelId);
        if (channel == null) {
            log.warn("[抓图管理] 通道不存在: channelId={}", channelId);
            throw new ServiceException(BAD_REQUEST.getCode(), "通道不存在: " + channelId);
        }

        // 2. 通过基础设施文件存储保存文件
        String directory = String.format("snapshots/%d/%04d%02d%02d", channelId,
                LocalDateTime.now().getYear(), LocalDateTime.now().getMonthValue(), LocalDateTime.now().getDayOfMonth());
        String url = fileApi.createFile(file.getBytes(), file.getOriginalFilename(), directory, file.getContentType());

        // 3. 解析路径
        String pathOnly = null;
        try {
            URL u = new URL(url);
            pathOnly = u.getPath();
        } catch (Exception ignore) {
            pathOnly = url;
        }

        // 4. 可选：读取图片尺寸
        Integer width = null, height = null;
        try {
            BufferedImage bi = ImageIO.read(file.getInputStream());
            if (bi != null) {
                width = bi.getWidth();
                height = bi.getHeight();
            }
        } catch (Exception ignore) {}

        // 5. 创建抓图记录（包含完整通道信息）
        IotCameraSnapshotDO snapshot = new IotCameraSnapshotDO();
        snapshot.setChannelId(channelId);
        snapshot.setDeviceId(channel.getDeviceId());
        snapshot.setChannelName(channel.getChannelName());
        snapshot.setSnapshotUrl(url);
        snapshot.setSnapshotPath(pathOnly);
        snapshot.setFileSize(file.getSize());
        snapshot.setWidth(width);
        snapshot.setHeight(height);
        snapshot.setCaptureTime(LocalDateTime.now());
        snapshot.setSnapshotType(snapshotType != null ? snapshotType : 1);
        snapshot.setIsProcessed(false);

        cameraSnapshotMapper.insert(snapshot);

        log.info("[抓图管理] 上传抓图并创建记录: id={}, channelId={}, channelName={}, deviceId={}, url={}", 
                snapshot.getId(), channelId, channel.getChannelName(), channel.getDeviceId(), url);
        return snapshot.getId();
    }

    @Override
    public IotCameraSnapshotDO getSnapshot(Long id) {
        return cameraSnapshotMapper.selectById(id);
    }

    @Override
    public void deleteSnapshot(Long id) {
        cameraSnapshotMapper.deleteById(id);
    }

    @Override
    public void markAsProcessed(Long id, String processor, String remark) {
        IotCameraSnapshotDO snapshot = cameraSnapshotMapper.selectById(id);
        if (snapshot != null) {
            snapshot.setIsProcessed(true);
            snapshot.setProcessor(processor);
            snapshot.setProcessTime(LocalDateTime.now());
            snapshot.setProcessRemark(remark);
            cameraSnapshotMapper.updateById(snapshot);
        }
    }

    @Override
    public CameraSnapshotRespVO getLatestSnapshot(Long channelId) {
        IotCameraSnapshotDO latest = cameraSnapshotMapper.selectLatestByChannelId(channelId);
        if (latest == null) {
            return null;
        }
        CameraSnapshotRespVO vo = new CameraSnapshotRespVO();
        BeanUtils.copyProperties(latest, vo);
        // 通道名称需要从通道表查询，这里暂时不设置
        // TODO: 从 iot_device_channel 表查询 channel_name
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long captureSnapshot(Long channelId) {
        // 1. TODO: 验证通道是否存在且在线（需要从 iot_device_channel 表查询）
        // 暂时跳过验证
        
        // 2. 创建抓图记录（状态：待处理）
        IotCameraSnapshotDO snapshot = new IotCameraSnapshotDO();
        snapshot.setChannelId(channelId);
        snapshot.setSnapshotType(1); // 1=手动抓拍
        snapshot.setCaptureTime(LocalDateTime.now());
        snapshot.setIsProcessed(false);
        
        cameraSnapshotMapper.insert(snapshot);
        
        log.info("[抓图管理] 创建手动抓拍记录: id={}, channelId={}", snapshot.getId(), channelId);
        
        // 3. 通过物模型发送抓拍指令到Gateway
        sendCaptureCommand(channelId, snapshot.getId());
        
        return snapshot.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSnapshot(Long channelId, Integer snapshotType, String imageData) {
        // 由Gateway回调触发，保存Base64图片
        try {
            // 1. 生成文件路径
            String filePath = generateSnapshotPath(channelId);
            String fullPath = snapshotDirectory + filePath;
            
            // 2. 解码Base64并保存文件
            byte[] imageBytes = Base64.decode(imageData);
            File file = new File(fullPath);
            file.getParentFile().mkdirs();
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(imageBytes);
            }
            
            // 3. 创建抓图记录
            IotCameraSnapshotDO snapshot = new IotCameraSnapshotDO();
            snapshot.setChannelId(channelId);
            snapshot.setSnapshotType(snapshotType);
            snapshot.setCaptureTime(LocalDateTime.now());
            snapshot.setSnapshotUrl(snapshotUrlPrefix + filePath);
            snapshot.setFileSize((long) imageBytes.length);
            snapshot.setIsProcessed(false);
            
            cameraSnapshotMapper.insert(snapshot);
            
            log.info("[抓图管理] [Gateway回调] 创建抓图记录: id={}, channelId={}, type={}, size={}bytes", 
                    snapshot.getId(), channelId, snapshotType, imageBytes.length);
            
            return snapshot.getId();
            
        } catch (Exception e) {
            log.error("[抓图管理] 保存抓图失败: channelId={}", channelId, e);
            throw new ServiceException(BAD_REQUEST.getCode(), "保存抓图失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSnapshotByFile(Long channelId, Integer snapshotType, String filePath, Long fileSize) {
        // 由Gateway回调触发，记录文件路径
        IotCameraSnapshotDO snapshot = new IotCameraSnapshotDO();
        snapshot.setChannelId(channelId);
        snapshot.setSnapshotType(snapshotType);
        snapshot.setCaptureTime(LocalDateTime.now());
        snapshot.setSnapshotUrl(snapshotUrlPrefix + filePath);
        snapshot.setFileSize(fileSize);
        snapshot.setIsProcessed(false);
        
        cameraSnapshotMapper.insert(snapshot);
        
        log.info("[抓图管理] [Gateway回调] 创建抓图记录（文件）: id={}, channelId={}, type={}, size={}bytes", 
                snapshot.getId(), channelId, snapshotType, fileSize);
        
        return snapshot.getId();
    }

    /**
     * 转换为VO并填充设备名称
     */
    private List<CameraSnapshotRespVO> convertToVO(List<IotCameraSnapshotDO> list) {
        if (list.isEmpty()) {
            return List.of();
        }

        // 查询所有涉及的设备ID
        List<Long> deviceIds = list.stream()
                .map(IotCameraSnapshotDO::getChannelId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询设备信息
        Map<Long, IotDeviceDO> deviceMap = new HashMap<>();
        for (Long deviceId : deviceIds) {
            IotDeviceDO device = deviceService.getDevice(deviceId);
            if (device != null) {
                deviceMap.put(deviceId, device);
            }
        }

        return list.stream().map(snapshot -> {
            CameraSnapshotRespVO vo = new CameraSnapshotRespVO();
            BeanUtils.copyProperties(snapshot, vo);

            // 填充设备名称
            IotDeviceDO device = deviceMap.get(snapshot.getChannelId());
            if (device != null && StrUtil.isBlank(vo.getChannelName())) {
                // 通道名称需要从通道表查询
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 生成快照文件路径
     * 格式：/snapshots/{channelId}/{yyyyMMdd}/snapshot_{timestamp}.jpg
     */
    private String generateSnapshotPath(Long channelId) {
        LocalDateTime now = LocalDateTime.now();
        String dateDir = String.format("%04d%02d%02d", 
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String fileName = String.format("snapshot_%d_%d.jpg", 
                System.currentTimeMillis(), channelId);
        
        return String.format("/%d/%s/%s", channelId, dateDir, fileName);
    }

    /**
     * 发送抓拍指令（通过物模型）
     * 
     * TODO: 待Gateway层实现抓拍处理逻辑后集成
     */
    private void sendCaptureCommand(Long channelId, Long snapshotId) {
        // TODO: 物模型通信集成（待Gateway层实现）
        log.warn("[抓图管理] TODO: 抓拍指令待实现 - channelId={}, snapshotId={}", channelId, snapshotId);
        
        // 临时方案：直接标记为成功（实际抓拍需要Gateway层实现）
        // 正式实现时，需要：
        // 1. 通过RocketMQ发送消息到Gateway
        // 2. Gateway调用ZLMediaKit或ONVIF抓拍
        // 3. Gateway回调通知Biz层保存抓图
    }

}


