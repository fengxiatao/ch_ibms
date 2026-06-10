package cn.iocoder.yudao.module.iot.service.video;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.client.ZlmApiClient;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.PlayUrlRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

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
    private final IbmsChannelMapper ibmsChannelMapper;
    private final IbmsDeviceMapper ibmsDeviceMapper;
    private final IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

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
        IotDeviceChannelDO channel = loadChannel(channelId);
        if (channel == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "通道不存在: channelId=" + channelId);
        }

        // 2. 查询 IBMS 台账与运行态（config 在运行态表）
        IbmsDeviceDO device = ibmsDeviceMapper.selectById(channel.getDeviceId());
        if (device == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "设备不存在: deviceId=" + channel.getDeviceId());
        }
        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeService.getByDeviceId(channel.getDeviceId());

        // 3. 构建流标识（包含码流类型，避免主/子码流冲突）
        String streamKey = buildStreamKey(channelId, subtype);

        // 4. 检查流是否已存在，不存在则拉流
        if (!zlmApiClient.isStreamOnline(APP_LIVE, streamKey)) {
            log.info("[ZLM流服务] 流不存在，开始拉流: channelId={}, streamKey={}, subtype={}", 
                    channelId, streamKey, subtype);
            
            String rtspUrl = buildRtspUrl(device, runtime, channel, subtype);
            if (StrUtil.isBlank(rtspUrl)) {
                throw new ServiceException(BAD_REQUEST.getCode(),
                        "无法构建 RTSP 地址: channelId=" + channelId
                                + "（请检查：设备/台账 IP、通道 extra 的 targetIp/ip/host、或配置 streamUrlMain 完整拉流地址）");
            }

            String proxyKey = zlmApiClient.addStreamProxy(APP_LIVE, streamKey, rtspUrl);
            if (proxyKey == null) {
                throw new ServiceException(
                        BAD_REQUEST.getCode(),
                        "拉流失败: channelId=" + channelId + "（请检查 ZLMediaKit 可达、secret 配置、以及摄像头 RTSP 可连通）"
                );
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
            IotDeviceChannelDO channel = loadChannel(channelId);
            if (channel == null) {
                log.warn("[ZLM Hook] 通道不存在: channelId={}", channelId);
                return;
            }

            IbmsDeviceDO device = ibmsDeviceMapper.selectById(channel.getDeviceId());
            if (device == null) {
                log.warn("[ZLM Hook] 设备不存在: deviceId={}", channel.getDeviceId());
                return;
            }
            IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeService.getByDeviceId(channel.getDeviceId());

            // 构建 RTSP 并拉流（Hook 回调默认使用主码流）
            String rtspUrl = buildRtspUrl(device, runtime, channel, 0);
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
            String rest = streamKey.substring("channel_".length());
            int idx = rest.indexOf('_');
            String numPart = idx > 0 ? rest.substring(0, idx) : rest;
            try {
                return Long.parseLong(numPart);
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
    private String buildRtspUrl(IbmsDeviceDO device, IbmsDeviceRuntimeDO runtime, IotDeviceChannelDO channel, Integer subtype) {
        // 默认主码流
        if (subtype == null) {
            subtype = 0;
        }

        // 优先使用通道 extra 中已配置的完整 RTSP；但历史数据里曾把 HTTP 80 端口误写成 RTSP 端口。
        // 这种地址会导致 ZLM 从错误源拉流，必须回退到设备 rtspPort 动态拼接。
        String configuredSubUrl = StrUtil.trim(channel.getStreamUrlSub());
        String configuredMainUrl = StrUtil.trim(channel.getStreamUrlMain());
        if (subtype == 1 && StrUtil.isNotBlank(configuredSubUrl) && !isInvalidRtspSourceUrl(configuredSubUrl)) {
            log.info("[ZLM] 使用通道配置的子码流 RTSP: channelId={}", channel.getId());
            return configuredSubUrl;
        }
        if (subtype == 0 && StrUtil.isNotBlank(configuredMainUrl) && !isInvalidRtspSourceUrl(configuredMainUrl)) {
            log.info("[ZLM] 使用通道配置的主码流 RTSP: channelId={}", channel.getId());
            return configuredMainUrl;
        }
        if ((subtype == 1 && StrUtil.isNotBlank(configuredSubUrl)) || (subtype == 0 && StrUtil.isNotBlank(configuredMainUrl))) {
            log.warn("[ZLM] 忽略异常通道 RTSP 配置，改用设备参数动态拼接: channelId={}, subtype={}, main={}, sub={}",
                    channel.getId(), subtype, maskCredential(configuredMainUrl), maskCredential(configuredSubUrl));
        }

        IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(device, runtime);
        
        // 📍 确定目标 IP
        // 关键逻辑：NVR 通道需要使用 NVR 的 IP（设备 IP），而不是 IPC 的 IP（targetIp）
        // 因为 RTSP 流是从 NVR 获取的，不是直连 IPC
        // ⚠️ 注意：通道发现来源（如 ONVIF 直挂）可能让 channel.deviceType 不为 NVR，
        //   但只要父设备是 NVR，就必须走 NVR 分支，避免误直连 IPC。
        boolean isNvr = isNvrAttached(device, channel);
        String deviceType = isNvr ? "NVR" : channel.getDeviceType();
        String ip;
        if (isNvr) {
            // NVR 通道：使用设备 IP（NVR 的 IP）
            ip = net.ip;
            log.debug("[ZLM] NVR 通道，使用 NVR IP: {}", ip);
        } else {
            // IPC 直连场景：优先使用 targetIp，否则使用设备 IP
            ip = channel.getTargetIp();
            if (StrUtil.isBlank(ip)) {
                ip = net.ip;
            }
        }
        if (StrUtil.isBlank(ip)) {
            log.error("[ZLM] 无法获取设备 IP: channelId={}, deviceId={}", channel.getId(), device.getId());
            return null;
        }

        // 🔐 获取认证信息
        // ⚠️ NVR 模式下优先用设备级（NVR）账号；IPC 直连模式下才优先用通道级账号。
        //   这样即使通道 extra 里残留了 IPC 自身的密码，也不会污染对 NVR 的认证。
        String username;
        String password;
        if (isNvr) {
            username = StrUtil.isNotBlank(net.username) ? net.username : channel.getUsername();
            password = StrUtil.isNotBlank(net.password) ? net.password : channel.getPassword();
        } else {
            username = channel.getUsername();
            password = channel.getPassword();
            if (StrUtil.isBlank(username)) {
                username = net.username;
            }
            if (StrUtil.isBlank(password)) {
                password = net.password;
            }
        }
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            if (runtime != null && runtime.getConfig() != null) {
                try {
                    var configMap = runtime.getConfig().toMap();
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
        if (isNvr) {
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
        String deviceName = device.getName();
        
        // RTSP 端口：台账/运行态解析优先，其次运行态 config 覆盖；默认 554（历史上误用 80 会导致地址异常）
        int rtspPort = net.rtspPort > 0 ? net.rtspPort : 554;
        if (runtime != null && runtime.getConfig() != null) {
            try {
                var configMap = runtime.getConfig().toMap();
                Object portObj = configMap.get("rtspPort");
                if (portObj != null) {
                    int p = Integer.parseInt(portObj.toString());
                    if (p > 0) {
                        rtspPort = p;
                    }
                }
            } catch (Exception ignored) {}
        }
        
        String rtspUrl;
        int rtspChannelForLog = channelNo;
        // 大华设备/NVR
        if (isDahuaDevice(deviceName, productKey)) {
            // ⚠️ 实测：本项目接入的大华 NVR 的 channel 参数为 1-indexed（从 1 开始）
            // 之前的 -1 会导致通道错位（例如 4->3），从而拉流超时/404。
            int dahuaChannel = channelNo;
            if (dahuaChannel < 1) dahuaChannel = 1;
            // subtype: 0=主码流(高清), 1=子码流(标清)
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=%d&subtype=%d",
                    username, password, ip, rtspPort, dahuaChannel, subtype);
            log.info("[ZLM] 大华设备通道号使用(1-indexed): dbChannelNo={} -> rtspChannel={}", channelNo, dahuaChannel);
            rtspChannelForLog = dahuaChannel;
        }
        // 海康设备/NVR
        else if (isHikvisionDevice(deviceName, productKey)) {
            // 海康通道号格式：X01=主码流, X02=子码流
            int streamType = (subtype == 1) ? 2 : 1; // 1=主码流, 2=子码流
            int hikChannel = channelNo * 100 + streamType;
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/Streaming/Channels/%d",
                    username, password, ip, rtspPort, hikChannel);
            rtspChannelForLog = hikChannel;
        }
        // 通用格式（适用于大多数 NVR）- 使用大华格式作为默认（0-indexed）
        else {
            int genericChannel = channelNo;
            if (genericChannel < 1) genericChannel = 1;
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=%d&subtype=%d",
                    username, password, ip, rtspPort, genericChannel, subtype);
            rtspChannelForLog = genericChannel;
        }
        
        String quality = (subtype == 0) ? "主码流/高清" : "子码流/标清";
        log.info("[ZLM] 构建 RTSP 地址: ip={}, dbChannel={}, rtspChannel={}, quality={}, url={}", 
                ip, channelNo, rtspChannelForLog, quality, rtspUrl);
        return rtspUrl;
    }

    private IotDeviceChannelDO loadChannel(Long channelId) {
        IbmsChannelDO channel = ibmsChannelMapper.selectById(channelId);
        if (channel == null) {
            return null;
        }
        JSONObject extra = parseExtra(channel.getExtra());
        return IotDeviceChannelDO.builder()
                .id(channel.getId())
                .deviceId(channel.getDeviceId())
                .deviceType(resolveDeviceType(channel, extra))
                .channelNo(channel.getChannelNo())
                .channelName(channel.getName())
                .channelCode(channel.getCode())
                .channelType(channel.getBusiness())
                .channelSubType(channel.getTypeCode())
                .location(channel.getSpace())
                .spaceId(channel.getSpaceId())
                // 与 DhVideo / 现场配置对齐：IPC 或 NVR 下挂通道的可达 IP 常写在 extra.ip / host
                .targetIp(firstNonBlank(
                        extra.getStr("targetIp"),
                        extra.getStr("ipcIp"),
                        extra.getStr("ip"),
                        extra.getStr("host"),
                        channel.getIp()))
                .targetPort(extra.getInt("targetPort"))
                .targetChannelNo(extra.getInt("targetChannelNo"))
                .protocol(extra.getStr("protocol"))
                .username(extra.getStr("username"))
                .password(extra.getStr("password"))
                .streamUrlMain(extra.getStr("streamUrlMain"))
                .streamUrlSub(extra.getStr("streamUrlSub"))
                .ptzSupport(extra.getBool("ptzSupport"))
                .build();
    }

    private JSONObject parseExtra(String extra) {
        if (StrUtil.isBlank(extra)) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(extra);
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    private String resolveDeviceType(IbmsChannelDO channel, JSONObject extra) {
        return firstNonBlank(
                extra.getStr("deviceType"),
                extra.getStr("dataSource"),
                channel.getDataSource(),
                channel.getCategory()
        );
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isInvalidRtspSourceUrl(String rtspUrl) {
        if (StrUtil.isBlank(rtspUrl)) {
            return false;
        }
        try {
            java.net.URI uri = java.net.URI.create(rtspUrl.trim());
            return "rtsp".equalsIgnoreCase(uri.getScheme()) && uri.getPort() == 80;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String maskCredential(String url) {
        if (StrUtil.isBlank(url)) {
            return url;
        }
        return url.replaceFirst("(?i)rtsp://([^:/@]+):([^@]+)@", "rtsp://$1:***@");
    }

    /**
     * 判断通道是否挂载在 NVR 下。
     * <p>只要满足任一条件即视为 NVR 通道：</p>
     * <ul>
     *     <li>通道自身解析出的 deviceType 为 NVR（来自 channel.dataSource / category 或 extra.deviceType）</li>
     *     <li>父设备的 deviceTypeCode 为 NVR（防止 ONVIF 直挂等来源让通道 deviceType 不为 NVR 时被误判为 IPC 直连）</li>
     * </ul>
     */
    private boolean isNvrAttached(IbmsDeviceDO device, IotDeviceChannelDO channel) {
        if (channel != null && "NVR".equalsIgnoreCase(channel.getDeviceType())) {
            return true;
        }
        return device != null && "NVR".equalsIgnoreCase(device.getDeviceTypeCode());
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
    public PlayUrlRespVO getPlaybackUrl(Long channelId, String startTime, String endTime, String playId) {
        // 1. 查询通道信息
        IotDeviceChannelDO channel = loadChannel(channelId);
        if (channel == null) {
            throw new RuntimeException("通道不存在: channelId=" + channelId);
        }

        // 2. 查询 IBMS 台账与运行态
        IbmsDeviceDO device = ibmsDeviceMapper.selectById(channel.getDeviceId());
        if (device == null) {
            throw new RuntimeException("设备不存在: deviceId=" + channel.getDeviceId());
        }
        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeService.getByDeviceId(channel.getDeviceId());

        // 3. 构建回放流标 hookup: 同一窗口使用固定 playId，点击时间轴会复用 streamKey 并强制刷新
        String streamKey = buildPlaybackStreamKey(channelId, startTime, endTime, playId);

        // playId 存在时，先关闭旧流与代理，避免缓存导致时间不生效
        if (StrUtil.isNotBlank(playId)) {
            closePlaybackStreamIfExists(streamKey);
        }

        // 4. 检查流是否已存在，不存在则拉流
        if (!zlmApiClient.isStreamOnline(APP_PLAYBACK, streamKey)) {
            log.info("[ZLM流服务] 回放流不存在，开始拉流: channelId={}, streamKey={}", channelId, streamKey);

            String rtspUrl = buildPlaybackRtspUrl(device, runtime, channel, startTime, endTime);
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
     * 生成回放流标识
     */
    private String buildPlaybackStreamKey(Long channelId, String startTime, String endTime, String playId) {
        if (StrUtil.isNotBlank(playId)) {
            String safePlayId = playId.replaceAll("[^a-zA-Z0-9_-]", "_");
            return String.format("playback_%d_%s", channelId, safePlayId);
        }
        String timeHash = String.valueOf((startTime + endTime).hashCode() & 0x7fffffff);
        return String.format("playback_%d_%s", channelId, timeHash);
    }

    /**
     * 关闭回放流与代理（用于窗口重复点播刷新）
     */
    private void closePlaybackStreamIfExists(String streamKey) {
        try {
            // 先关闭在线流
            if (zlmApiClient.isStreamOnline(APP_PLAYBACK, streamKey)) {
                boolean closed = zlmApiClient.closeStream(APP_PLAYBACK, streamKey);
                log.info("[ZLM流服务] 关闭回放流: streamKey={}, closed={}", streamKey, closed);
            }

            // 再删除代理
            String proxyKey = streamProxyCache.remove(streamKey);
            if (StrUtil.isNotBlank(proxyKey) && !proxyKey.startsWith("exists:")) {
                boolean deleted = zlmApiClient.delStreamProxy(proxyKey);
                log.info("[ZLM流服务] 删除回放代理: streamKey={}, proxyKey={}, deleted={}",
                        streamKey, proxyKey, deleted);
            }
        } catch (Exception e) {
            log.warn("[ZLM流服务] 关闭回放流失败: streamKey={}, error={}", streamKey, e.getMessage());
        }
    }

    /**
     * 构建录像回放 RTSP URL
     * <ul>
     *     <li>大华 NVR：rtsp://user:pass@ip:554/cam/playback?channel=5&subtype=0&starttime=2026_01_16_10_00_00&endtime=2026_01_16_11_00_00</li>
     *     <li>海康 NVR：rtsp://user:pass@ip:554/Streaming/tracks/501?starttime=20260116t100000z&endtime=20260116t110000z</li>
     * </ul>
     */
    private String buildPlaybackRtspUrl(IbmsDeviceDO device, IbmsDeviceRuntimeDO runtime, IotDeviceChannelDO channel, String startTime, String endTime) {
        IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(device, runtime);
        // 📍 确定目标 IP（与实时流逻辑保持一致）
        boolean isNvr = isNvrAttached(device, channel);
        String ip;
        if (isNvr) {
            ip = net.ip;
            log.debug("[ZLM] NVR 通道回放，使用 NVR IP: {}", ip);
        } else {
            ip = channel.getTargetIp();
            if (StrUtil.isBlank(ip)) {
                ip = net.ip;
            }
        }
        if (StrUtil.isBlank(ip)) {
            log.error("[ZLM] 无法获取设备 IP: channelId={}, deviceId={}", channel.getId(), device.getId());
            return null;
        }

        // 🔐 NVR 模式下优先用设备级账号，避免被通道级 IPC 凭据污染
        String username;
        String password;
        if (isNvr) {
            username = StrUtil.isNotBlank(net.username) ? net.username : channel.getUsername();
            password = StrUtil.isNotBlank(net.password) ? net.password : channel.getPassword();
        } else {
            username = channel.getUsername();
            password = channel.getPassword();
            if (StrUtil.isBlank(username)) {
                username = net.username;
            }
            if (StrUtil.isBlank(password)) {
                password = net.password;
            }
        }
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            if (runtime != null && runtime.getConfig() != null) {
                try {
                    var configMap = runtime.getConfig().toMap();
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
        if (isNvr) {
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

        int rtspPort = 554;
        if (runtime != null && runtime.getConfig() != null) {
            try {
                var configMap = runtime.getConfig().toMap();
                Object portObj = configMap.get("rtspPort");
                if (portObj != null) {
                    rtspPort = Integer.parseInt(portObj.toString());
                }
            } catch (Exception ignored) {}
        }
        String productKey = device.getProductKey();
        String deviceName = device.getName();

        String rtspUrl;
        // 大华设备/NVR - 使用大华回放格式
        if (isDahuaDevice(deviceName, productKey)) {
            // ⚠️ 实测：本项目接入的大华 NVR 的 channel 参数为 1-indexed（从 1 开始）
            int dahuaChannel = channelNo;
            if (dahuaChannel < 1) dahuaChannel = 1;
            log.info("[ZLM] 大华设备回放通道号使用(1-indexed): dbChannelNo={} -> rtspChannel={}", channelNo, dahuaChannel);
            // 大华格式时间：YYYY_MM_DD_HH_mm_ss
            String dahuaStartTime = formatToDahuaTime(startTime);
            String dahuaEndTime = formatToDahuaTime(endTime);
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/cam/playback?channel=%d&subtype=0&starttime=%s&endtime=%s",
                    username, password, ip, rtspPort, dahuaChannel, dahuaStartTime, dahuaEndTime);
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
        // 通用格式 - 使用大华格式（同样使用 0-indexed）
        else {
            int genericChannel = channelNo;
            if (genericChannel < 1) genericChannel = 1;
            String dahuaStartTime = formatToDahuaTime(startTime);
            String dahuaEndTime = formatToDahuaTime(endTime);
            rtspUrl = String.format("rtsp://%s:%s@%s:%d/cam/playback?channel=%d&subtype=0&starttime=%s&endtime=%s",
                    username, password, ip, rtspPort, genericChannel, dahuaStartTime, dahuaEndTime);
        }

        log.info("[ZLM] 构建回放 RTSP 地址: ip={}, dbChannel={}, rtspChannel={}, startTime={}, endTime={}, url={}", 
                ip, channelNo, channelNo, startTime, endTime, rtspUrl);
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

    // ==================== 通道封面快照 ====================

    @Override
    public byte[] getChannelSnapshot(Long channelId, Integer subtype) {
        if (subtype == null) {
            subtype = 1; // 封面默认走子码流，体积更小、抓帧更快
        }

        // 1. 查询通道、设备、运行态（与 getLivePlayUrl 完全一致）
        IotDeviceChannelDO channel = loadChannel(channelId);
        if (channel == null) {
            log.warn("[ZLM快照] 通道不存在: channelId={}", channelId);
            return null;
        }
        IbmsDeviceDO device = ibmsDeviceMapper.selectById(channel.getDeviceId());
        if (device == null) {
            log.warn("[ZLM快照] 设备不存在: deviceId={}", channel.getDeviceId());
            return null;
        }
        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeService.getByDeviceId(channel.getDeviceId());

        // 2. 构建 RTSP 源 URL
        String rtspUrl = buildRtspUrl(device, runtime, channel, subtype);
        if (StrUtil.isBlank(rtspUrl)) {
            log.warn("[ZLM快照] 无法构建 RTSP: channelId={}", channelId);
            return null;
        }

        // 3. 调用 ZLM 抓帧
        //    - timeout_sec=5：首次抓帧最多等 5 秒
        //    - expire_sec=60：jpeg 在 ZLM 磁盘缓存 60 秒，期间重复请求秒级命中
        return zlmApiClient.getSnap(rtspUrl, 5, 60);
    }
}
