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
     * 发送预设点控制命令
     * 
     * <p>通过消息总线发送预设点控制命令到 NewGateway。</p>
     * <p>支持的操作：GOTO（转到预设点）、SET（设置预设点）、CLEAR（删除预设点）</p>
     * 
     * @param deviceId  设备ID（NVR）
     * @param channelNo 通道号
     * @param presetNo  预设点编号（1-255）
     * @param action    操作类型：GOTO, SET, CLEAR
     * @return requestId 请求ID
     */
    public String presetControl(Long deviceId, int channelNo, int presetNo, String action) {
        return presetControl(deviceId, channelNo, presetNo, action, null, null, null, null);
    }

    /**
     * 发送预设点控制命令（支持直连 IPC）
     * 
     * <p>当指定 targetIp 时，gateway 会直接连接该 IP 的设备进行预设点控制。</p>
     * 
     * @param deviceId   设备ID（NVR）
     * @param channelNo  通道号
     * @param presetNo   预设点编号（1-255）
     * @param action     操作类型：GOTO, SET, CLEAR
     * @param targetIp   目标设备IP（如果指定，则直接连接该IP设备）
     * @param username   目标设备用户名
     * @param password   目标设备密码
     * @return requestId 请求ID
     */
    public String presetControl(Long deviceId, int channelNo, int presetNo, String action,
                                String targetIp, String username, String password) {
        return presetControl(deviceId, channelNo, presetNo, action, targetIp, username, password, null);
    }

    /**
     * 发送预设点控制命令（支持直连 IPC，支持预设点名称）
     * 
     * <p>当指定 targetIp 时，gateway 会直接连接该 IP 的设备进行预设点控制。</p>
     * 
     * @param deviceId    设备ID（NVR）
     * @param channelNo   通道号
     * @param presetNo    预设点编号（1-255）
     * @param action      操作类型：GOTO, SET, CLEAR
     * @param targetIp    目标设备IP（如果指定，则直接连接该IP设备）
     * @param username    目标设备用户名
     * @param password    目标设备密码
     * @param presetName  预设点名称（SET操作时使用）
     * @return requestId 请求ID
     */
    public String presetControl(Long deviceId, int channelNo, int presetNo, String action,
                                String targetIp, String username, String password, String presetName) {
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channelNo);
        params.put("presetNo", presetNo);
        params.put("presetAction", action);
        
        // 如果指定了预设点名称
        if (presetName != null && !presetName.isEmpty()) {
            params.put("presetName", presetName);
        }
        
        // 如果指定了目标IP，则传递给gateway，让其直接连接该设备
        if (targetIp != null && !targetIp.isEmpty()) {
            params.put("targetIp", targetIp);
            params.put("targetUsername", username != null ? username : "admin");
            params.put("targetPassword", password != null ? password : "admin123");
            log.info("{} 预设点控制将直连目标设备: targetIp={}", LOG_PREFIX, targetIp);
        }

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                NvrDeviceTypeConstants.COMMAND_PTZ_CONTROL,
                params
        );

        log.info("{} 预设点控制命令已发送: deviceId={}, channel={}, presetNo={}, action={}, presetName={}, targetIp={}, requestId={}",
                LOG_PREFIX, deviceId, channelNo, presetNo, action, presetName, targetIp, requestId);

        return requestId;
    }

    /**
     * 发送区域放大（3D定位）命令
     * 
     * <p>通过消息总线发送区域放大命令到 NewGateway。</p>
     * <p>用户在视频画面上框选区域，摄像头会自动移动并放大到该区域。</p>
     * 
     * <p>坐标系说明：所有坐标为归一化坐标（0-8192），
     * 其中 (0,0) 为画面左上角，(8192,8192) 为画面右下角。</p>
     * 
     * @param deviceId   设备ID（NVR）
     * @param channelNo  通道号
     * @param startX     框选起始点 X
     * @param startY     框选起始点 Y
     * @param endX       框选结束点 X
     * @param endY       框选结束点 Y
     * @param targetIp   目标设备IP（直连 IPC）
     * @param username   目标设备用户名
     * @param password   目标设备密码
     * @return requestId 请求ID
     */
    public String areaZoom(Long deviceId, int channelNo, int startX, int startY, int endX, int endY,
                           String targetIp, String username, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channelNo);
        params.put("areaStartX", startX);
        params.put("areaStartY", startY);
        params.put("areaEndX", endX);
        params.put("areaEndY", endY);
        
        // 区域放大需要直连 IPC
        if (targetIp != null && !targetIp.isEmpty()) {
            params.put("targetIp", targetIp);
            params.put("targetUsername", username != null ? username : "admin");
            params.put("targetPassword", password != null ? password : "admin123");
            log.info("{} 区域放大将直连目标设备: targetIp={}", LOG_PREFIX, targetIp);
        }

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                NvrDeviceTypeConstants.COMMAND_PTZ_CONTROL,
                params
        );

        log.info("{} 区域放大命令已发送: deviceId={}, channel={}, area=({},{}) -> ({},{}), targetIp={}, requestId={}",
                LOG_PREFIX, deviceId, channelNo, startX, startY, endX, endY, targetIp, requestId);

        return requestId;
    }

    /**
     * 发送 3D 定位命令
     * 
     * <p>直接指定中心点和放大倍数进行 3D 定位。</p>
     * 
     * @param deviceId   设备ID（NVR）
     * @param channelNo  通道号
     * @param x          中心点 X（0-8192）
     * @param y          中心点 Y（0-8192）
     * @param zoom       放大倍数（1-128）
     * @param targetIp   目标设备IP（直连 IPC）
     * @param username   目标设备用户名
     * @param password   目标设备密码
     * @return requestId 请求ID
     */
    public String position3D(Long deviceId, int channelNo, int x, int y, int zoom,
                             String targetIp, String username, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channelNo);
        params.put("positionX", x);
        params.put("positionY", y);
        params.put("positionZoom", zoom);
        
        // 3D 定位需要直连 IPC
        if (targetIp != null && !targetIp.isEmpty()) {
            params.put("targetIp", targetIp);
            params.put("targetUsername", username != null ? username : "admin");
            params.put("targetPassword", password != null ? password : "admin123");
            log.info("{} 3D定位将直连目标设备: targetIp={}", LOG_PREFIX, targetIp);
        }

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                NvrDeviceTypeConstants.COMMAND_PTZ_CONTROL,
                params
        );

        log.info("{} 3D定位命令已发送: deviceId={}, channel={}, position=({},{},{}), targetIp={}, requestId={}",
                LOG_PREFIX, deviceId, channelNo, x, y, zoom, targetIp, requestId);

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

    // ==================== 巡航控制命令 ====================

    /**
     * 同步巡航配置到设备
     * 
     * @param deviceId    设备ID
     * @param channelNo   通道号
     * @param tourNo      巡航组编号（1-8）
     * @param tourName    巡航组名称
     * @param presetNos   预设点编号列表
     * @param dwellTimes  每个预设点的停留时间（秒）
     * @param targetIp    目标设备IP
     * @param username    用户名
     * @param password    密码
     * @return requestId 请求ID
     */
    public String syncTourToDevice(Long deviceId, int channelNo, int tourNo, String tourName,
                                    java.util.List<Integer> presetNos, java.util.List<Integer> dwellTimes,
                                    String targetIp, String username, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channelNo);
        params.put("tourAction", "SYNC");
        params.put("tourNo", tourNo);
        params.put("tourName", tourName);
        params.put("presetNos", presetNos);
        params.put("dwellTimes", dwellTimes);
        
        if (targetIp != null && !targetIp.isEmpty()) {
            params.put("targetIp", targetIp);
            params.put("targetUsername", username != null ? username : "admin");
            params.put("targetPassword", password != null ? password : "admin123");
        }

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                "TOUR_CONTROL",
                params
        );

        log.info("{} 同步巡航命令已发送: deviceId={}, channel={}, tourNo={}, tourName={}, presetCount={}, requestId={}",
                LOG_PREFIX, deviceId, channelNo, tourNo, tourName, presetNos.size(), requestId);

        return requestId;
    }

    /**
     * 启动设备巡航
     * 
     * @param deviceId   设备ID
     * @param channelNo  通道号
     * @param tourNo     巡航组编号（1-8）
     * @param targetIp   目标设备IP
     * @param username   用户名
     * @param password   密码
     * @return requestId 请求ID
     */
    public String startDeviceTour(Long deviceId, int channelNo, int tourNo,
                                   String targetIp, String username, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channelNo);
        params.put("tourAction", "START");
        params.put("tourNo", tourNo);
        
        if (targetIp != null && !targetIp.isEmpty()) {
            params.put("targetIp", targetIp);
            params.put("targetUsername", username != null ? username : "admin");
            params.put("targetPassword", password != null ? password : "admin123");
        }

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                "TOUR_CONTROL",
                params
        );

        log.info("{} 启动设备巡航命令已发送: deviceId={}, channel={}, tourNo={}, requestId={}",
                LOG_PREFIX, deviceId, channelNo, tourNo, requestId);

        return requestId;
    }

    /**
     * 停止设备巡航
     * 
     * @param deviceId   设备ID
     * @param channelNo  通道号
     * @param tourNo     巡航组编号（1-8）
     * @param targetIp   目标设备IP
     * @param username   用户名
     * @param password   密码
     * @return requestId 请求ID
     */
    public String stopDeviceTour(Long deviceId, int channelNo, int tourNo,
                                  String targetIp, String username, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put(NvrDeviceTypeConstants.PARAM_CHANNEL_NO, channelNo);
        params.put("tourAction", "STOP");
        params.put("tourNo", tourNo);
        
        if (targetIp != null && !targetIp.isEmpty()) {
            params.put("targetIp", targetIp);
            params.put("targetUsername", username != null ? username : "admin");
            params.put("targetPassword", password != null ? password : "admin123");
        }

        String requestId = deviceCommandPublisher.publishCommand(
                NvrDeviceTypeConstants.NVR,
                deviceId,
                "TOUR_CONTROL",
                params
        );

        log.info("{} 停止设备巡航命令已发送: deviceId={}, channel={}, tourNo={}, requestId={}",
                LOG_PREFIX, deviceId, channelNo, tourNo, requestId);

        return requestId;
    }
}
