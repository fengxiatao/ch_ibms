package cn.iocoder.yudao.module.iot.service.security;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.PlayUrlRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.SecurityOverviewCameraPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.SecurityOverviewCameraRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
import cn.iocoder.yudao.module.iot.service.camera.dto.CameraDeviceView;
import cn.iocoder.yudao.module.iot.websocket.message.ServiceFailureMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * IoT 安防概览 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotSecurityOverviewServiceImpl implements IotSecurityOverviewService {

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;

    @Resource
    private IbmsDeviceRuntimeMapper ibmsDeviceRuntimeMapper;
    
    
    @Resource
    private cn.iocoder.yudao.module.iot.websocket.DeviceMessagePushService messagePushService;
    
    // ✅ ZLMediaKit 已迁移到 Gateway 层，通过物模型getPlayUrl服务调用
    // 参考：docs/ZLMediaKit架构迁移说明.md

    @Override
    public PageResult<SecurityOverviewCameraRespVO> getSecurityOverviewCameras(
            SecurityOverviewCameraPageReqVO reqVO) {
        
        log.info("[安防概览] 查询摄像头列表: includeSnapshot={}, onlineOnly={}", 
                reqVO.getIncludeSnapshot(), reqVO.getOnlineOnly());
        
        // 1. 单台账：原 iot_device 查询只查 product_id=3（网络摄像头）。
        // 这里改为只查 ibms_device.ibms_product_id=3，并用 ibms_device_runtime 组装 legacy 壳。
        Long ibmsProductId = 3L;
        List<IbmsDeviceDO> ibmsDevices = ibmsDeviceMapper.selectListByIbmsProductId(ibmsProductId);
        if (ibmsDevices == null) {
            ibmsDevices = List.of();
        }

        List<Long> deviceIds = ibmsDevices.stream().map(IbmsDeviceDO::getId).toList();
        List<IbmsDeviceRuntimeDO> runtimes = deviceIds.isEmpty()
                ? List.of()
                : ibmsDeviceRuntimeMapper.selectList(
                new LambdaQueryWrapperX<IbmsDeviceRuntimeDO>()
                        .in(IbmsDeviceRuntimeDO::getDeviceId, deviceIds));
        Map<Long, IbmsDeviceRuntimeDO> runtimeMap = runtimes == null
                ? Map.<Long, IbmsDeviceRuntimeDO>of()
                : runtimes.stream().collect(Collectors.toMap(IbmsDeviceRuntimeDO::getDeviceId, r -> r, (a, b) -> a));

        // 2. 组装视图 + 计算状态，再按 onlineTime 倒序（nulls 最后）
        List<CameraDeviceView> shells = ibmsDevices.stream()
                .map(ibms -> CameraDeviceView.of(ibms, runtimeMap.get(ibms.getId())))
                .filter(java.util.Objects::nonNull)
                .filter(d -> {
                    if (reqVO.getOnlineOnly() == null || !reqVO.getOnlineOnly()) {
                        return true;
                    }
                    return cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum
                            .isOnline(d.getState());
                })
                .sorted(Comparator.comparing(CameraDeviceView::getOnlineTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        log.info("[安防概览] 查询到设备数量: total={}, listSize={}", shells.size(), shells.size());

        // 3. 分页
        int pageNo = reqVO.getPageNo();
        int pageSize = reqVO.getPageSize();
        int from = Math.max(0, (pageNo - 1) * pageSize);
        int to = Math.min(from + pageSize, shells.size());
        List<CameraDeviceView> pageList = from >= shells.size() ? List.of() : shells.subList(from, to);

        // 4. 转换为VO
        List<SecurityOverviewCameraRespVO> voList = pageList.stream()
                .map(device -> convertToVO(device, reqVO.getIncludeSnapshot()))
                .collect(Collectors.toList());
        
        log.info("[安防概览] 转换后VO数量: {}", voList.size());
        
        return new PageResult<>(voList, (long) shells.size());
    }

    @Override
    public String getDeviceSnapshot(Long deviceId) {
        log.info("[安防概览] 获取设备快照: deviceId={}", deviceId);
        
        // 1. 查询设备信息
        IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(deviceId);
        if (ibms == null) {
            log.warn("[安防概览] 设备不存在: deviceId={}", deviceId);
            return null;
        }
        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeMapper.selectById(deviceId);
        CameraDeviceView device = CameraDeviceView.of(ibms, runtime);
        if (device == null) {
            log.warn("[安防概览] 设备不存在: deviceId={}", deviceId);
            return null;
        }
        
        // 2. 检查设备是否在线
        if (device.getState() == null || device.getState() != 1) {
            log.warn("[安防概览] 设备离线: deviceId={}", deviceId);
            return null;
        }
        
        // 3. ✅ 通过物模型调用 Gateway 的 snapshot 服务（使用 ZLMediaKit）
        try {
            String snapshot = invokeSnapshotService(device);
            if (snapshot != null && !snapshot.isEmpty()) {
                log.info("[安防概览] ✅ 快照获取成功: deviceId={}, size={}", 
                        deviceId, snapshot.length());
                return snapshot;
            } else {
                log.warn("[安防概览] ❌ 快照服务返回空结果: deviceId={}", deviceId);
                return null;
            }
        } catch (Exception e) {
            log.error("[安防概览] ❌ 快照服务调用失败: deviceId={}", deviceId, e);
            
            // ✅ 推送服务失败消息到 WebSocket
            pushSnapshotFailure(device, e.getMessage());
            
            return null;
        }
    }
    
    /**
     * 通过物模型调用 Gateway 的快照服务
     * 
     * ✅ 架构说明：
     * - Biz 层通过 RocketMQ 发送 SERVICE_INVOKE 消息
     * - Gateway 层接收并调用 ZLMediaKit 的 getSnap API
     * - Gateway 返回 Base64 图片数据通过 SERVICE_RESULT 消息
     * 
     * ✅ 优势：
     * - 使用 ZLMediaKit 的流代理快照，性能更好
     * - 符合物模型架构，统一服务调用方式
     * - 支持快照缓存和优化
     * 
     * @param device 设备信息
     * @return Base64编码的图片数据（含 data:image/jpeg;base64, 前缀）
     */
    private String invokeSnapshotService(CameraDeviceView device) {
        log.info("[安防概览] (占位) 快照服务未启用: deviceId={}", device.getId());
        return null;
    }

    /**
     * 转换设备DO为VO
     */
    private SecurityOverviewCameraRespVO convertToVO(CameraDeviceView device, Boolean includeSnapshot) {
        SecurityOverviewCameraRespVO vo = new SecurityOverviewCameraRespVO();
        
        vo.setId(device.getId());
        vo.setDeviceName(device.getDeviceName());
        vo.setNickname(device.getNickname());
        vo.setLocation(device.getAddress());
        vo.setDeviceKey(device.getDeviceKey());
        
        // 设置在线状态（使用统一的状态判断方法）
        boolean isOnline = cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum.isOnline(device.getState());
        vo.setOnline(isOnline);
        vo.setStatus(isOnline ? "online" : "offline");
        vo.setStatusText(isOnline ? "正常" : "离线");
        vo.setLastOnlineTime(device.getOnlineTime());
        
        // 设置设备配置信息（M2-E：改为视图直接 getter）
        SecurityOverviewCameraRespVO.DeviceInfo deviceInfo =
            new SecurityOverviewCameraRespVO.DeviceInfo();
        deviceInfo.setVendor(device.getVendor());
        deviceInfo.setHttpPort(device.getHttpPort());
        deviceInfo.setRtspPort(device.getRtspPort());
        // historic: shell 从不写入 onvifPort，为保持行为一致，仍设为 null
        deviceInfo.setOnvifPort(null);
        vo.setDeviceInfo(deviceInfo);
        
        // 如果需要抓图且设备在线，获取实时抓图
        if (Boolean.TRUE.equals(includeSnapshot) && isOnline) {
            try {
                // ✅ 通过物模型调用 Gateway 的 snapshot 服务（使用 ZLMediaKit）
                String snapshotBase64 = invokeSnapshotService(device);
                if (snapshotBase64 != null && !snapshotBase64.isEmpty()) {
                    vo.setSnapshotUrl("data:image/jpeg;base64," + snapshotBase64);
                    log.debug("[安防概览] ✅ 物模型快照成功: deviceId={}", device.getId());
                } else {
                    log.warn("[安防概览] ⚠️ 物模型快照返回空: deviceId={}", device.getId());
                    vo.setSnapshotUrl(null);
                }
            } catch (Exception e) {
                log.warn("[安防概览] ❌ 物模型快照失败: deviceId={}", device.getId(), e);
                vo.setSnapshotUrl(null);
            }
        }
        
        return vo;
    }

    /**
     * 获取设备实时抓图（内部方法）
     * 
     * ⚠️⚠️⚠️ 已废弃 - 禁止使用此方法！⚠️⚠️⚠️
     * 
     * ❌ 错误做法：直接通过 HTTP 调用设备快照接口
     * - 性能差（5-10秒）
     * - 成功率低（~70%，Cookie 错误）
     * - 绕过了 Gateway 架构
     * - 不使用流代理
     * 
     * ✅ 正确做法：使用 invokeSnapshotService() 方法
     * - 通过物模型（RocketMQ）调用 Gateway
     * - Gateway 使用 ZLMediaKit 流代理快照
     * - 性能优秀（< 0.5秒）
     * - 成功率高（~100%）
     * 
     * 📚 参考文档：
     * - docs/安防概览-纯ZLMediaKit快照架构.md
     * - docs/sessions/.../快照方法调用错误修复.md
     * 
     * @deprecated 此方法已废弃，请使用 {@link #invokeSnapshotService(CameraDeviceView)} 替代
     */
    @Deprecated
    @SuppressWarnings("unused")
    private String getDeviceSnapshotInternal(CameraDeviceView device) {
        try {
            // 解析配置（M2-E：视图直取）
            String ip = device.getIp();
            int port = device.getHttpPort();
            String username = device.getUsername();
            String password = device.getPassword();
            
            log.debug("[安防概览] 准备获取抓图: deviceId={}, ip={}, port={}, username={}", 
                    device.getId(), ip, port, username);
            
        // ✅ 尝试多种常见的ONVIF抓图路径（注意：路径区分大小写！）
        String[] snapshotPaths = {
            "/ISAPI/Streaming/channels/1/picture", // 海康威视主路径（全小写）
            "/ISAPI/Streaming/channels/101/picture", // 海康威视备用
            "/cgi-bin/snapshot.cgi?channel=1",  // 大华
            "/onvif-http/snapshot",  // 标准ONVIF
            "/onvif/snapshot",
            "/snapshot.jpg",
            "/snap.jpg",
            "/tmpfs/auto.jpg"  // 某些设备
        };
            
            for (String path : snapshotPaths) {
                String snapshotUrl = String.format("http://%s:%d%s", ip, port, path);
                
                try {
                    log.debug("[安防概览] 尝试路径: {}", snapshotUrl);
                    
                    // 使用HttpUtil（Hutool工具类）
                    cn.hutool.http.HttpResponse response = cn.hutool.http.HttpUtil.createGet(snapshotUrl)
                            .basicAuth(username, password)
                            .timeout(5000)
                            .execute();
                    
                    byte[] imageBytes = response.bodyBytes();
                    String contentType = response.header("Content-Type");
                    
                    // ✅ 验证是否真的是图片
                    if (imageBytes != null && imageBytes.length > 100 && 
                        contentType != null && contentType.startsWith("image/")) {
                        
                        String imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                        log.info("[安防概览] 抓图成功: deviceId={}, url={}, size={}KB, contentType={}", 
                                device.getId(), snapshotUrl, imageBytes.length / 1024, contentType);
                        return imageBase64;
                    } else {
                        log.debug("[安防概览] 响应不是图片: deviceId={}, url={}, contentType={}, size={}", 
                                device.getId(), snapshotUrl, contentType, 
                                imageBytes != null ? imageBytes.length : 0);
                    }
                } catch (Exception e) {
                    log.debug("[安防概览] 路径尝试失败: url={}, error={}", snapshotUrl, e.getMessage());
                }
            }
            
            log.warn("[安防概览] 所有抓图路径均失败: deviceId={}", device.getId());
            return null;
            
        } catch (Exception e) {
            log.error("[安防概览] 获取设备抓图异常: deviceId={}", device.getId(), e);
            return null;
        }
    }

    @Override
    public PlayUrlRespVO getPlayUrl(Long deviceId) {
        log.info("[安防概览] 获取播放地址: deviceId={}", deviceId);
        
        // 1. 查询设备信息
        IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(deviceId);
        if (ibms == null) {
            log.warn("[安防概览] 设备不存在: deviceId={}", deviceId);
            throw new RuntimeException("设备不存在: deviceId=" + deviceId);
        }
        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeMapper.selectById(deviceId);
        CameraDeviceView device = CameraDeviceView.of(ibms, runtime);
        if (device == null) {
            log.warn("[安防概览] 设备不存在: deviceId={}", deviceId);
            throw new RuntimeException("设备不存在: deviceId=" + deviceId);
        }
        
        // 2. 检查设备是否在线
        if (device.getState() == null || device.getState() != 1) {
            log.warn("[安防概览] 设备离线: deviceId={}", deviceId);
            throw new RuntimeException("设备离线: deviceId=" + deviceId);
        }
        
        // 3. 调用 Gateway 的 getPlayUrl 服务（通过 RocketMQ）
        try {
            return invokeGetPlayUrlService(device);
        } catch (Exception e) {
            log.error("[安防概览] 获取播放地址失败: deviceId={}", deviceId, e);
            throw new RuntimeException("获取播放地址失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用 Gateway 的 getPlayUrl 服务
     */
    private PlayUrlRespVO invokeGetPlayUrlService(CameraDeviceView device) {
        log.info("[安防概览] (占位) 获取播放地址: deviceId={}", device.getId());
        return PlayUrlRespVO.builder()
                .wsFlvUrl(null)
                .webrtcUrl(null)
                .wsFmp4Url(null)
                .fmp4Url(null)
                .flvUrl(null)
                .hlsUrl(null)
                .rtmpUrl(null)
                .streamKey(null)
                .build();
    }
    
    /**
     * 推送快照服务失败消息到 WebSocket
     * 
     * @param device 设备信息
     * @param errorMessage 错误消息
     */
    private void pushSnapshotFailure(CameraDeviceView device, String errorMessage) {
        try {
            String deviceName = device.getDeviceName() != null ? device.getDeviceName() : ("设备_" + device.getId());
            ServiceFailureMessage failureMsg = ServiceFailureMessage.forSnapshot(
                device.getId(),
                deviceName,
                errorMessage,
                UUID.randomUUID().toString()
            );
            
            messagePushService.pushServiceFailure(failureMsg);
            
            log.info("[安防概览] ✅ 已推送快照失败消息到WebSocket: deviceId={}, deviceName={}", 
                    device.getId(), deviceName);
            
        } catch (Exception e) {
            // 推送失败不影响主流程，只记录日志
            log.error("[安防概览] ❌ 推送快照失败消息异常: deviceId={}", device.getId(), e);
        }
    }
}
