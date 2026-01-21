package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.video.AccessVideoPlayParamsRespVO;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.AccessStreamInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁设备视频服务实现
 * <p>
 * 通过调用 Gateway HTTP 接口获取设备的 RTSP 连接参数，
 * 并构建前端 DHPlayer 播放器所需的视频播放参数。
 * <p>
 * Requirements:
 * - 2.1: 通过 Gateway 获取设备的 RTSP 流 URL
 * - 2.2: 返回包含 wsURL、rtspURL、username、password 的完整连接信息
 * - 2.3: 设备离线时返回设备不在线错误
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class AccessVideoServiceImpl implements AccessVideoService {

    /**
     * Gateway 服务地址
     */
    @Value("${iot.gateway.url:http://localhost:48083}")
    private String gatewayUrl;

    private final RestTemplate restTemplate;

    public AccessVideoServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AccessVideoPlayParamsRespVO getPlayParams(Long deviceId, Integer channelNo) {
        log.info("[门禁视频] 获取播放参数: deviceId={}, channelNo={}", deviceId, channelNo);

        // 1. 调用 Gateway 获取视频流信息
        AccessStreamInfoDTO streamInfo = getStreamInfoFromGateway(deviceId, channelNo);
        if (streamInfo == null) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }

        // 2. 构建播放参数
        return buildPlayParams(streamInfo);
    }

    @Override
    public Boolean checkVideoSupport(Long deviceId) {
        log.debug("[门禁视频] 检查视频支持: deviceId={}", deviceId);

        try {
            String url = String.format("%s/api/gateway/access/video/support-check?deviceId=%d",
                    gatewayUrl, deviceId);

            ResponseEntity<CommonResult<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<CommonResult<Boolean>>() {}
            );

            CommonResult<Boolean> result = response.getBody();
            if (result != null && result.isSuccess()) {
                return result.getData();
            }

            log.warn("[门禁视频] 检查视频支持失败: deviceId={}, result={}", deviceId, result);
            return false;

        } catch (Exception e) {
            log.error("[门禁视频] 检查视频支持异常: deviceId={}", deviceId, e);
            return false;
        }
    }

    /**
     * 从 Gateway 获取视频流信息
     */
    private AccessStreamInfoDTO getStreamInfoFromGateway(Long deviceId, Integer channelNo) {
        try {
            String url = String.format("%s/api/gateway/access/video/stream-info?deviceId=%d&channelNo=%d",
                    gatewayUrl, deviceId, channelNo);

            log.debug("[门禁视频] 调用 Gateway: {}", url);

            ResponseEntity<CommonResult<AccessStreamInfoDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<CommonResult<AccessStreamInfoDTO>>() {}
            );

            CommonResult<AccessStreamInfoDTO> result = response.getBody();
            if (result != null && result.isSuccess()) {
                return result.getData();
            }

            log.warn("[门禁视频] Gateway 返回失败: deviceId={}, result={}", deviceId, result);
            return null;

        } catch (Exception e) {
            log.error("[门禁视频] 调用 Gateway 异常: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 构建播放参数
     * <p>
     * 根据 Gateway 返回的流信息，构建前端 DHPlayer 所需的参数。
     * 包括 WebSocket URL、RTSP URL 和认证信息。
     */
    private AccessVideoPlayParamsRespVO buildPlayParams(AccessStreamInfoDTO streamInfo) {
        String ip = streamInfo.getIpAddress();
        Integer httpPort = streamInfo.getHttpPort() != null ? streamInfo.getHttpPort() : 80;
        Integer channelNo = streamInfo.getChannelNo() != null ? streamInfo.getChannelNo() : 0;

        // 构建 WebSocket URL（用于 RTSP over WebSocket）
        String wsURL = String.format("ws://%s:%d/rtspoverwebsocket", ip, httpPort);

        // 构建 RTSP URL（大华设备格式）
        // channel 参数从 1 开始，subtype=0 表示主码流，subtype=1 表示子码流
        int rtspChannel = channelNo + 1;
        String rtspURL = String.format("rtsp://%s:%d/cam/realmonitor?channel=%d&subtype=1",
                ip, httpPort, rtspChannel);

        // 构建目标地址
        String target = String.format("%s:%d", ip, httpPort);

        // 构建通道名称
        String channelName = channelNo == 0 ? "内置摄像头" : "通道" + rtspChannel;

        return AccessVideoPlayParamsRespVO.builder()
                .wsURL(wsURL)
                .rtspURL(rtspURL)
                .username(streamInfo.getUsername())
                .password(streamInfo.getPassword())
                .target(target)
                .deviceName(streamInfo.getDeviceName())
                .channelName(channelName)
                .online(streamInfo.getOnline())
                .deviceId(streamInfo.getDeviceId())
                .channelNo(channelNo)
                .errorMessage(streamInfo.getErrorMessage())
                .build();
    }
}
