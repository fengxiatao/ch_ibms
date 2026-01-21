package cn.iocoder.yudao.module.iot.service.video;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.client.ZlmApiClient;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.PlayUrlRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ZLMediaKit 流媒体服务实现
 * 
 * <p>核心功能：</p>
 * <ul>
 *     <li>按需拉流：当前端请求播放时，自动从摄像头拉取 RTSP 流</li>
 *     <li>协议转换：将 RTSP 转换为 WS-FLV/HLS/WebRTC 等 Web 友好格式</li>
 *     <li>资源管理：无人观看时自动释放流资源</li>
 * </ul>
 *
 * @author IBMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZlmStreamServiceImpl implements ZlmStreamService {

    private final ZlmApiClient zlmApiClient;
    private final IotDeviceChannelService channelService;
    private final IotDeviceService deviceService;

    /** 应用名常量 */
    private static final String APP_LIVE = "live";

    /** 流代理缓存：streamKey -> proxyKey */
    private final ConcurrentHashMap<String, String> streamProxyCache = new ConcurrentHashMap<>();

    @Override
    public PlayUrlRespVO getLivePlayUrl(Long channelId, Integer subtype) {
        // 默认使用主码流
        if (subtype == null) {
            subtype = 0;
        }
        
        // 1. 查询通道信息
        IotDeviceChannelDO channel = channelService.getChannel(channelId);
        if (channel == null) {
            throw new RuntimeException("通道不存在: channelId=" + channelId);
        }

        // 2. 查询设备信息
        IotDeviceDO device = deviceService.getDevice(channel.getDeviceId());
        if (device == null) {
            throw new RuntimeException("设备不存在: deviceId=" + channel.getDeviceId());
        }

        // 3. 构建流标识（包含码流类型，避免主/子码流冲突）
        String streamKey = buildStreamKey(channelId, subtype);

        // 4. 检查流是否已存在，不存在则拉流
        if (!zlmApiClient.isStreamOnline(APP_LIVE, streamKey)) {
            log.info("[ZLM流服务] 流不存在，开始拉流: channelId={}, streamKey={}, subtype={}", 
                    channelId, streamKey, subtype);
            
            String rtspUrl = buildRtspUrl(device, channel, subtype);
            if (StrUtil.isBlank(rtspUrl)) {
                throw new RuntimeException("无法构建 RTSP 地址: channelId=" + channelId);
            }

            String proxyKey = zlmApiClient.addStreamProxy(APP_LIVE, streamKey, rtspUrl);
            if (proxyKey == null) {
                throw new RuntimeException("拉流失败: channelId=" + channelId);
            }
            
            streamProxyCache.put(streamKey, proxyKey);
            log.info("[ZLM流服务] ✅ 拉流成功: streamKey={}, proxyKey={}", streamKey, proxyKey);
        }

        // 5. 生成播放地址
        ZlmApiClient.PlayUrlsVO urls = zlmApiClient.buildPlayUrls(APP_LIVE, streamKey);

        return PlayUrlRespVO.builder()
                .wsFlvUrl(urls.getWsFlvUrl())      // 推荐：低延迟
                .flvUrl(urls.getFlvUrl())
                .hlsUrl(urls.getHlsUrl())
                .wsFmp4Url(urls.getWsFmp4Url())
                .fmp4Url(null)
                .rtmpUrl(urls.getRtmpUrl())
                .webrtcUrl(urls.getWebRtcUrl())    // 推荐：极低延迟
                .streamKey(streamKey)
                .build();
    }

    @Override
    public boolean stopStream(Long channelId) {
        String streamKey = buildStreamKey(channelId);
        String proxyKey = streamProxyCache.remove(streamKey);

        if (proxyKey != null) {
            boolean success = zlmApiClient.delStreamProxy(proxyKey);
            log.info("[ZLM流服务] 停止流: channelId={}, streamKey={}, success={}", channelId, streamKey, success);
            return success;
        }

        // 尝试直接关闭流
        return zlmApiClient.closeStream(APP_LIVE, streamKey);
    }

    @Override
    public boolean isStreamOnline(Long channelId) {
        String streamKey = buildStreamKey(channelId);
        return zlmApiClient.isStreamOnline(APP_LIVE, streamKey);
    }

    @Override
    public void handleStreamNotFound(String app, String stream) {
        log.info("[ZLM Hook] 流未找到，尝试按需拉流: app={}, stream={}", app, stream);

        // 从 stream 名称解析通道 ID
        Long channelId = parseChannelIdFromStreamKey(stream);
        if (channelId == null) {
            log.warn("[ZLM Hook] 无法解析通道ID: stream={}", stream);
            return;
        }

        try {
            // 查询通道和设备信息
            IotDeviceChannelDO channel = channelService.getChannel(channelId);
            if (channel == null) {
                log.warn("[ZLM Hook] 通道不存在: channelId={}", channelId);
                return;
            }

            IotDeviceDO device = deviceService.getDevice(channel.getDeviceId());
            if (device == null) {
                log.warn("[ZLM Hook] 设备不存在: deviceId={}", channel.getDeviceId());
                return;
            }

            // 构建 RTSP 并拉流（Hook 回调默认使用主码流）
            String rtspUrl = buildRtspUrl(device, channel, 0);
            String proxyKey = zlmApiClient.addStreamProxy(app, stream, rtspUrl);

            if (proxyKey != null) {
                streamProxyCache.put(stream, proxyKey);
                log.info("[ZLM Hook] ✅ 按需拉流成功: stream={}", stream);
            }
        } catch (Exception e) {
            log.error("[ZLM Hook] 按需拉流失败: stream={}", stream, e);
        }
    }

    @Override
    public void handleStreamNoneReader(String app, String stream) {
        log.info("[ZLM Hook] 无人观看，释放流资源: app={}, stream={}", app, stream);

        String proxyKey = streamProxyCache.remove(stream);
        if (proxyKey != null) {
            zlmApiClient.delStreamProxy(proxyKey);
            log.info("[ZLM Hook] ✅ 流资源已释放: stream={}, proxyKey={}", stream, proxyKey);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建流标识
     * 格式：channel_{channelId}
     */
    private String buildStreamKey(Long channelId) {
        return buildStreamKey(channelId, 0);
    }

    private String buildStreamKey(Long channelId, Integer subtype) {
        // 主码流不带后缀，子码流带 _sub 后缀
        if (subtype != null && subtype == 1) {
            return "channel_" + channelId + "_sub";
        }
        return "channel_" + channelId;
    }

    /**
     * 从流标识解析通道ID
     */
    private Long parseChannelIdFromStreamKey(String streamKey) {
        if (streamKey != null && streamKey.startsWith("channel_")) {
            try {
                return Long.parseLong(streamKey.substring("channel_".length()));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 构建摄像头 RTSP 地址
     * 
     * <p>优先级：</p>
     * <ol>
     *     <li>通道配置的 streamUrlMain（已配置的完整 RTSP 地址）</li>
     *     <li>使用 targetIp + targetChannelNo 构建（NVR 场景）</li>
     *     <li>使用设备 IP 构建（IPC 场景）</li>
     * </ol>
     * 
     * <p>支持多品牌摄像头/NVR：</p>
     * <ul>
     *     <li>大华 NVR：rtsp://user:pass@ip:554/cam/realmonitor?channel=5&subtype=0</li>
     *     <li>海康 NVR：rtsp://user:pass@ip:554/Streaming/Channels/501</li>
     *     <li>通用：rtsp://user:pass@ip:554/stream1</li>
     * </ul>
     */
    private String buildRtspUrl(IotDeviceDO device, IotDeviceChannelDO channel, Integer subtype) {
        // 默认主码流
        if (subtype == null) {
            subtype = 0;
        }
        
        // 📍 确定目标 IP
        // 关键逻辑：NVR 通道需要使用 NVR 的 IP（设备 IP），而不是 IPC 的 IP（targetIp）
        // 因为 RTSP 流是从 NVR 获取的，不是直连 IPC
        String ip;
        String deviceType = channel.getDeviceType();
        if ("NVR".equalsIgnoreCase(deviceType)) {
            // NVR 通道：使用设备 IP（NVR 的 IP）
            ip = DeviceConfigHelper.getIpAddress(device);
            log.debug("[ZLM] NVR 通道，使用 NVR IP: {}", ip);
        } else {
            // IPC 直连场景：优先使用 targetIp，否则使用设备 IP
            ip = channel.getTargetIp();
            if (StrUtil.isBlank(ip)) {
                ip = DeviceConfigHelper.getIpAddress(device);
            }
        }
        if (StrUtil.isBlank(ip)) {
            log.error("[ZLM] 无法获取设备 IP: channelId={}, deviceId={}", channel.getId(), device.getId());
            return null;
        }

        // 🔐 获取认证信息
        String username = channel.getUsername();
        String password = channel.getPassword();
        
        // 如果通道没有配置，尝试从设备配置中解析
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            if (device.getConfig() != null) {
                try {
                    var configMap = device.getConfig().toMap();
                    if (StrUtil.isBlank(username)) {
                        Object u = configMap.get("username");
                        if (u != null) username = u.toString();
                    }
                    if (StrUtil.isBlank(password)) {
                        Object p = configMap.get("password");
                        if (p != null) password = p.toString();
                    }
                } catch (Exception ignored) {}
            }
        }
        
        // 默认值
        if (StrUtil.isBlank(username)) username = "admin";
        if (StrUtil.isBlank(password)) password = "admin123";

        // 📺 确定通道号
        // ⚠️ 关键逻辑：NVR 通道需要使用 channel_no（NVR通道号），而不是 target_channel_no（IPC通道号）
        // - channel_no：NVR 设备上的通道编号（1-16），用于 RTSP 流地址中的 channel 参数
        // - target_channel_no：实际 IPC 设备的通道号（通常为1），仅用于 PTZ 直连控制
        int channelNo = 1;
        if ("NVR".equalsIgnoreCase(deviceType)) {
            // NVR 场景：使用 NVR 的 channel_no
            if (channel.getChannelNo() != null && channel.getChannelNo() > 0) {
                channelNo = channel.getChannelNo();
            }
        } else {
            // IPC 直连场景：优先使用 targetChannelNo，否则使用 channelNo
            if (channel.getTargetChannelNo() != null && channel.getTargetChannelNo() > 0) {
                channelNo = channel.getTargetChannelNo();
            } else if (channel.getChannelNo() != null && channel.getChannelNo() > 0) {
                channelNo = channel.getChannelNo();
            }
        }

        // 🏭 根据设备品牌构建 RTSP URL
        String productKey = device.getProductKey();
        String deviceName = device.getDeviceName();
        
        // 📡 从设备配置中获取 RTSP 端口，默认使用 80（大华 NVR 通常使用 80 端口）
        int rtspPort = 80; // 大华 NVR 默认使用 80 端口而非标准 554
        if (device.getConfig() != null) {
            try {
                var configMap = device.getConfig().toMap();
                Object portObj = configMap.get("rtspPort");
                if (portObj != null) {
                    rtspPort = Integer.parseInt(portObj.toString());
                }
            } catch (Exception ignored) {}
        }
        
        String rtspUrl;
        // 大华设备/NVR
        if (isDahuaDevice(deviceName, productKey)) {
            // subtype: 0=主码流(高清), 1=子码流(标清)
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=%d&subtype=%d",
                    username, password, ip, rtspPort, channelNo, subtype);
        }
        // 海康设备/NVR
        else if (isHikvisionDevice(deviceName, productKey)) {
            // 海康通道号格式：X01=主码流, X02=子码流
            int streamType = (subtype == 1) ? 2 : 1; // 1=主码流, 2=子码流
            int hikChannel = channelNo * 100 + streamType;
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/Streaming/Channels/%d",
                    username, password, ip, rtspPort, hikChannel);
        }
        // 通用格式（适用于大多数 NVR）- 使用大华格式作为默认
        else {
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=%d&subtype=%d",
                    username, password, ip, rtspPort, channelNo, subtype);
        }
        
        String quality = (subtype == 0) ? "主码流/高清" : "子码流/标清";
        log.info("[ZLM] 构建 RTSP 地址: ip={}, channel={}, quality={}, url={}", ip, channelNo, quality, rtspUrl);
        return rtspUrl;
    }

    private boolean isDahuaDevice(String deviceType, String productKey) {
        if (deviceType != null) {
            String lower = deviceType.toLowerCase();
            if (lower.contains("dahua") || lower.contains("大华") || lower.contains("dh")) {
                return true;
            }
        }
        if (productKey != null) {
            String lower = productKey.toLowerCase();
            return lower.contains("dahua") || lower.contains("dh");
        }
        return false;
    }

    private boolean isHikvisionDevice(String deviceType, String productKey) {
        if (deviceType != null) {
            String lower = deviceType.toLowerCase();
            if (lower.contains("hikvision") || lower.contains("海康") || lower.contains("hik")) {
                return true;
            }
        }
        if (productKey != null) {
            String lower = productKey.toLowerCase();
            return lower.contains("hikvision") || lower.contains("hik");
        }
        return false;
    }

    // ==================== 录像回放 ====================

    @Override
    public PlayUrlRespVO getPlaybackUrl(Long channelId, String startTime, String endTime) {
        // 1. 查询通道信息
        IotDeviceChannelDO channel = channelService.getChannel(channelId);
        if (channel == null) {
            throw new RuntimeException("通道不存在: channelId=" + channelId);
        }

        // 2. 查询设备信息
        IotDeviceDO device = deviceService.getDevice(channel.getDeviceId());
        if (device == null) {
            throw new RuntimeException("设备不存在: deviceId=" + channel.getDeviceId());
        }

        // 3. 构建回放流标识（包含时间信息的唯一标识）
        String timeHash = String.valueOf((startTime + endTime).hashCode() & 0x7fffffff);
        String streamKey = String.format("playback_%d_%s", channelId, timeHash);

        // 4. 检查流是否已存在，不存在则拉流
        if (!zlmApiClient.isStreamOnline(APP_PLAYBACK, streamKey)) {
            log.info("[ZLM流服务] 回放流不存在，开始拉流: channelId={}, streamKey={}", channelId, streamKey);

            String rtspUrl = buildPlaybackRtspUrl(device, channel, startTime, endTime);
            if (StrUtil.isBlank(rtspUrl)) {
                throw new RuntimeException("无法构建回放 RTSP 地址: channelId=" + channelId);
            }

            String proxyKey = zlmApiClient.addStreamProxy(APP_PLAYBACK, streamKey, rtspUrl);
            if (proxyKey == null) {
                throw new RuntimeException("拉取回放流失败: channelId=" + channelId);
            }

            streamProxyCache.put(streamKey, proxyKey);
            log.info("[ZLM流服务] ✅ 回放流拉取成功: streamKey={}, proxyKey={}", streamKey, proxyKey);
        }

        // 5. 生成播放地址
        ZlmApiClient.PlayUrlsVO urls = zlmApiClient.buildPlayUrls(APP_PLAYBACK, streamKey);

        return PlayUrlRespVO.builder()
                .wsFlvUrl(urls.getWsFlvUrl())
                .flvUrl(urls.getFlvUrl())
                .hlsUrl(urls.getHlsUrl())
                .wsFmp4Url(urls.getWsFmp4Url())
                .fmp4Url(null)
                .rtmpUrl(urls.getRtmpUrl())
                .webrtcUrl(urls.getWebRtcUrl())
                .streamKey(streamKey)
                .build();
    }

    /**
     * 构建录像回放 RTSP URL
     * <ul>
     *     <li>大华 NVR：rtsp://user:pass@ip:554/cam/playback?channel=5&subtype=0&starttime=2026_01_16_10_00_00&endtime=2026_01_16_11_00_00</li>
     *     <li>海康 NVR：rtsp://user:pass@ip:554/Streaming/tracks/501?starttime=20260116t100000z&endtime=20260116t110000z</li>
     * </ul>
     */
    private String buildPlaybackRtspUrl(IotDeviceDO device, IotDeviceChannelDO channel, String startTime, String endTime) {
        // 📍 确定目标 IP（与实时流逻辑保持一致）
        // NVR 通道需要使用 NVR 的 IP，而不是 IPC 的 IP
        String ip;
        String deviceType = channel.getDeviceType();
        if ("NVR".equalsIgnoreCase(deviceType)) {
            // NVR 通道：使用设备 IP（NVR 的 IP）
            ip = DeviceConfigHelper.getIpAddress(device);
            log.debug("[ZLM] NVR 通道回放，使用 NVR IP: {}", ip);
        } else {
            // IPC 直连场景：优先使用 targetIp，否则使用设备 IP
            ip = channel.getTargetIp();
            if (StrUtil.isBlank(ip)) {
                ip = DeviceConfigHelper.getIpAddress(device);
            }
        }
        if (StrUtil.isBlank(ip)) {
            log.error("[ZLM] 无法获取设备 IP: channelId={}, deviceId={}", channel.getId(), device.getId());
            return null;
        }

        // 获取认证信息
        String username = channel.getUsername();
        String password = channel.getPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            if (device.getConfig() != null) {
                try {
                    var configMap = device.getConfig().toMap();
                    if (StrUtil.isBlank(username)) {
                        Object u = configMap.get("username");
                        if (u != null) username = u.toString();
                    }
                    if (StrUtil.isBlank(password)) {
                        Object p = configMap.get("password");
                        if (p != null) password = p.toString();
                    }
                } catch (Exception ignored) {}
            }
        }
        if (StrUtil.isBlank(username)) username = "admin";
        if (StrUtil.isBlank(password)) password = "admin123";

        // 📺 确定通道号（与实时流逻辑保持一致）
        // NVR 通道需要使用 channel_no（NVR通道号），而不是 target_channel_no（IPC通道号）
        int channelNo = 1;
        if ("NVR".equalsIgnoreCase(deviceType)) {
            // NVR 场景：使用 NVR 的 channel_no
            if (channel.getChannelNo() != null && channel.getChannelNo() > 0) {
                channelNo = channel.getChannelNo();
            }
        } else {
            // IPC 直连场景：优先使用 targetChannelNo，否则使用 channelNo
            if (channel.getTargetChannelNo() != null && channel.getTargetChannelNo() > 0) {
                channelNo = channel.getTargetChannelNo();
            } else if (channel.getChannelNo() != null && channel.getChannelNo() > 0) {
                channelNo = channel.getChannelNo();
            }
        }

        // 📡 从设备配置中获取 RTSP 端口，默认使用 80（大华 NVR 通常使用 80 端口）
        int rtspPort = 80;
        if (device.getConfig() != null) {
            try {
                var configMap = device.getConfig().toMap();
                Object portObj = configMap.get("rtspPort");
                if (portObj != null) {
                    rtspPort = Integer.parseInt(portObj.toString());
                }
            } catch (Exception ignored) {}
        }
        String productKey = device.getProductKey();
        String deviceName = device.getDeviceName();

        String rtspUrl;
        // 大华设备/NVR - 使用大华回放格式
        if (isDahuaDevice(deviceName, productKey)) {
            // 大华格式时间：YYYY_MM_DD_HH_mm_ss
            String dahuaStartTime = formatToDahuaTime(startTime);
            String dahuaEndTime = formatToDahuaTime(endTime);
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/cam/playback?channel=%d&subtype=0&starttime=%s&endtime=%s",
                    username, password, ip, rtspPort, channelNo, dahuaStartTime, dahuaEndTime);
        }
        // 海康设备/NVR
        else if (isHikvisionDevice(deviceName, productKey)) {
            // 海康格式时间：YYYYMMDDtHHmmssz
            String hikStartTime = formatToHikvisionTime(startTime);
            String hikEndTime = formatToHikvisionTime(endTime);
            int hikChannel = channelNo * 100 + 1; // 主码流
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/Streaming/tracks/%d?starttime=%s&endtime=%s",
                    username, password, ip, rtspPort, hikChannel, hikStartTime, hikEndTime);
        }
        // 通用格式 - 使用大华格式
        else {
            String dahuaStartTime = formatToDahuaTime(startTime);
            String dahuaEndTime = formatToDahuaTime(endTime);
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/cam/playback?channel=%d&subtype=0&starttime=%s&endtime=%s",
                    username, password, ip, rtspPort, channelNo, dahuaStartTime, dahuaEndTime);
        }

        log.info("[ZLM] 构建回放 RTSP 地址: ip={}, channel={}, startTime={}, endTime={}, url={}", 
                ip, channelNo, startTime, endTime, rtspUrl);
        return rtspUrl;
    }

    /**
     * 转换时间为大华格式：YYYY_MM_DD_HH_mm_ss
     */
    private String formatToDahuaTime(String time) {
        try {
            // 支持 ISO 格式和时间戳
            java.time.LocalDateTime dt;
            if (time.contains("T") || time.contains("-")) {
                dt = java.time.LocalDateTime.parse(time.replace("Z", "").replace(" ", "T"));
            } else {
                dt = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(Long.parseLong(time)),
                    java.time.ZoneId.systemDefault()
                );
            }
            return dt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        } catch (Exception e) {
            log.warn("[ZLM] 时间格式转换失败，使用原始值: {}", time);
            return time;
        }
    }

    /**
     * 转换时间为海康格式：YYYYMMDDtHHmmssz
     */
    private String formatToHikvisionTime(String time) {
        try {
            java.time.LocalDateTime dt;
            if (time.contains("T") || time.contains("-")) {
                dt = java.time.LocalDateTime.parse(time.replace("Z", "").replace(" ", "T"));
            } else {
                dt = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(Long.parseLong(time)),
                    java.time.ZoneId.systemDefault()
                );
            }
            return dt.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd't'HHmmss'z'"));
        } catch (Exception e) {
            log.warn("[ZLM] 时间格式转换失败，使用原始值: {}", time);
            return time;
        }
    }

    private static final String APP_PLAYBACK = "playback";

    @Override
    public int clearAllStreams() {
        log.info("[ZLM流服务] 开始清除所有流代理...");
        
        // 1. 清空本地缓存
        int cacheSize = streamProxyCache.size();
        streamProxyCache.clear();
        log.info("[ZLM流服务] 已清空本地缓存: {} 条", cacheSize);
        
        // 2. 关闭 ZLMediaKit 上的所有 live 流
        int liveCount = zlmApiClient.closeAllStreams(APP_LIVE);
        log.info("[ZLM流服务] 已关闭 live 流: {} 条", liveCount);
        
        // 3. 关闭 ZLMediaKit 上的所有 playback 流
        int playbackCount = zlmApiClient.closeAllStreams(APP_PLAYBACK);
        log.info("[ZLM流服务] 已关闭 playback 流: {} 条", playbackCount);
        
        int totalClosed = liveCount + playbackCount;
        log.info("[ZLM流服务] ✅ 清除完成: 本地缓存={}, ZLM关闭={}", cacheSize, totalClosed);
        
        return totalClosed;
    }
}
