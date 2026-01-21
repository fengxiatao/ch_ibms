package cn.iocoder.yudao.module.iot.service.video.nvr;

import cn.iocoder.yudao.module.iot.enums.device.NvrDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * NVR 命令服务
 * 
 * <p>通过消息总线发送 NVR 设备命令，替代原有的 OnvifGatewayClient HTTP 调用。</p>
 * <p>所有命令通过 DeviceCommandPublisher 发送到 DEVICE_SERVICE_INVOKE 主题。</p>
 * 
 * <p>Requirements: 6.1, 6.2</p>
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NvrCommandService {

    private static final String LOG_PREFIX = "[NvrCommandService]";

    private final DeviceCommandPublisher deviceCommandPublisher;

    /**
     * 发送 PTZ 控制命令（连续移动）
     * 
     * <p>通过消息总线发送 PTZ 控制命令到 NewGateway。</p>
     * <p>命令结果通过 WebSocket 异步推送给前端。</p>
     * 
     * @param deviceId   设备ID
     * @param channelNo  通道号
     * @param pan        水平移动速度 (-1.0 到 1.0)
     * @param tilt       垂直移动速度 (-1.0 到 1.0)
     * @param zoom       变焦速度 (-1.0 到 1.0)
     * @param timeoutMs  超时时间（毫秒）
     * @return requestId 请求ID，用于关联响应
     */
    public String ptzMove(Long deviceId, int channelNo, double pan, double tilt, double zoom, Integer timeoutMs) {
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channelNo);
        params.put(NvrDeviceTypeConstants.PARAM_PAN, pan);
        params.put(NvrDeviceTypeConstants.PARAM_TILT, tilt);
        params.put(NvrDeviceTypeConstants.PARAM_ZOOM, zoom);
        if (timeoutMs != null) {
            params.put(NvrDeviceTypeConstants.PARAM_TIMEOUT_MS, timeoutMs);
        }
        // 设置 PTZ 命令类型
        params.put("ptzCommand", "CONTINUOUS_MOVE");

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                NvrDeviceTypeConstants.COMMAND_PTZ_CONTROL,
                params
        );

        log.info("{} PTZ 控制命令已发送: deviceId={}, channel={}, pan={}, tilt={}, zoom={}, requestId={}",
                LOG_PREFIX, deviceId, channelNo, pan, tilt, zoom, requestId);

        return requestId;
    }

    /**
     * 发送 PTZ 停止命令
     * 
     * <p>通过消息总线发送 PTZ 停止命令到 NewGateway。</p>
     * 
     * @param deviceId  设备ID
     * @param channelNo 通道号
     * @param panTilt   是否停止水平/垂直移动
     * @param zoom      是否停止变焦
     * @return requestId 请求ID
     */
    public String ptzStop(Long deviceId, int channelNo, boolean panTilt, boolean zoom) {
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channelNo);
        params.put("panTilt", panTilt);
        params.put("zoom", zoom);
        // 设置 PTZ 命令类型
        params.put("ptzCommand", "STOP");

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                NvrDeviceTypeConstants.COMMAND_PTZ_STOP,
                params
        );

        log.info("{} PTZ 停止命令已发送: deviceId={}, channel={}, panTilt={}, zoom={}, requestId={}",
                LOG_PREFIX, deviceId, channelNo, panTilt, zoom, requestId);

        return requestId;
    }

    /**
     * 发送 PTZ 控制命令（命令模式）
     * 
     * <p>通过消息总线发送直接的 PTZ 命令到 NewGateway。</p>
     * <p>支持的命令：UP, DOWN, LEFT, RIGHT, ZOOM_IN, ZOOM_OUT, FOCUS_NEAR, FOCUS_FAR 等</p>
     * 
     * @param deviceId  设备ID
     * @param channelNo 通道号
     * @param command   PTZ 命令（如 UP, DOWN, LEFT, RIGHT）
     * @param speed     速度 1-8
     * @param stop      是否为停止命令
     * @return requestId 请求ID
     */
    public String ptzControl(Long deviceId, int channelNo, String command, int speed, boolean stop) {
        return ptzControl(deviceId, channelNo, command, speed, stop, null, null, null);
    }

    /**
     * 发送 PTZ 控制命令（命令模式，支持直连 IPC）
     * 
     * <p>当指定 targetIp 时，gateway 会直接连接该 IP 的设备进行 PTZ 控制，
     * 而不是通过 NVR 转发。这对于 NVR 下挂的远程 IPC（如球机）特别有用。</p>
     * 
     * @param deviceId   设备ID（NVR）
     * @param channelNo  通道号
     * @param command    PTZ 命令
     * @param speed      速度 1-8
     * @param stop       是否为停止命令
     * @param targetIp   目标设备IP（如果指定，则直接连接该IP设备）
     * @param username   目标设备用户名
     * @param password   目标设备密码
     * @return requestId 请求ID
     */
    public String ptzControl(Long deviceId, int channelNo, String command, int speed, boolean stop,
                             String targetIp, String username, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channelNo);
        params.put("ptzCommand", stop ? command + "_STOP" : command);
        params.put("speed", speed);
        
        // 如果指定了目标IP，则传递给gateway，让其直接连接该设备
        if (targetIp != null && !targetIp.isEmpty()) {
            params.put("targetIp", targetIp);
            params.put("targetUsername", username != null ? username : "admin");
            params.put("targetPassword", password != null ? password : "admin123");
            log.info("{} PTZ 控制将直连目标设备: targetIp={}", LOG_PREFIX, targetIp);
        }

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                NvrDeviceTypeConstants.COMMAND_PTZ_CONTROL,
                params
        );

        log.info("{} PTZ 控制命令已发送: deviceId={}, channel={}, command={}, speed={}, stop={}, targetIp={}, requestId={}",
                LOG_PREFIX, deviceId, channelNo, command, speed, stop, targetIp, requestId);

        return requestId;
    }

    /**
     * 发送截图命令
     * 
     * <p>通过消息总线发送截图命令到 NewGateway。</p>
     * <p>截图结果通过 WebSocket 异步推送给前端（Base64 编码的图片数据）。</p>
     * 
     * @param deviceId  设备ID
     * @param channelNo 通道号
     * @return requestId 请求ID
     */
    public String captureSnapshot(Long deviceId, int channelNo) {
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channelNo);

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                NvrDeviceTypeConstants.COMMAND_CAPTURE_SNAPSHOT,
                params
        );

        log.info("{} 截图命令已发送: deviceId={}, channel={}, requestId={}",
                LOG_PREFIX, deviceId, channelNo, requestId);

        return requestId;
    }

    /**
     * 发送获取能力集命令
     * 
     * <p>通过消息总线发送获取能力集命令到 NewGateway。</p>
     * <p>能力集结果通过 WebSocket 异步推送给前端。</p>
     * 
     * @param deviceId 设备ID
     * @return requestId 请求ID
     */
    public String getCapabilities(Long deviceId) {
        Map<String, Object> params = new HashMap<>();

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                NvrDeviceTypeConstants.COMMAND_GET_CAPABILITIES,
                params
        );

        log.info("{} 获取能力集命令已发送: deviceId={}, requestId={}",
                LOG_PREFIX, deviceId, requestId);

        return requestId;
    }
}
