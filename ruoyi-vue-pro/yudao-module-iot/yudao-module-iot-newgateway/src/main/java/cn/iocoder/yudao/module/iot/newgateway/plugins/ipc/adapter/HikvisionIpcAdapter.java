package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.adapter;

import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.IpcLoginResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.IpcOperationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 海康威视 IPC 适配器
 * 
 * <p>使用海康 HCNetSDK 实现 IPC 设备的接入</p>
 * <p>注意：实际使用需要引入海康 SDK 依赖</p>
 * 
 * @author IoT Gateway Team
 * @since 1.0
 */
@Slf4j
@Component
public class HikvisionIpcAdapter extends AbstractIpcAdapter {

    private static final String LOG_PREFIX = "[HikvisionIpcAdapter]";
    
    public static final String VENDOR_CODE = "HIKVISION";
    public static final String VENDOR_NAME = "海康威视";

    /** 模拟登录句柄生成器（实际使用时由 SDK 返回） */
    private final AtomicLong handleGenerator = new AtomicLong(100000);

    // ==================== 基础信息 ====================

    @Override
    public String getVendorCode() {
        return VENDOR_CODE;
    }

    @Override
    public String getVendorName() {
        return VENDOR_NAME;
    }

    @Override
    public boolean supports(Map<String, Object> deviceInfo) {
        if (deviceInfo == null) {
            return false;
        }
        
        // 通过 vendor 字段判断
        Object vendor = deviceInfo.get("vendor");
        if (vendor != null) {
            String vendorStr = vendor.toString().toUpperCase();
            if (vendorStr.contains("HIKVISION") || vendorStr.contains("海康") || vendorStr.contains("HIK")) {
                return true;
            }
        }
        
        // 通过 productKey 判断
        Object productKey = deviceInfo.get("productKey");
        if (productKey != null) {
            String pkStr = productKey.toString().toUpperCase();
            if (pkStr.contains("HIKVISION") || pkStr.contains("HIK_") || pkStr.contains("DS-")) {
                return true;
            }
        }
        
        // 通过设备型号判断
        Object model = deviceInfo.get("deviceModel");
        if (model != null) {
            String modelStr = model.toString().toUpperCase();
            if (modelStr.startsWith("DS-") || modelStr.startsWith("IDS-")) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public int getPriority() {
        return 20; // 次高优先级
    }

    // ==================== SDK 生命周期 ====================

    @Override
    public boolean initialize() {
        if (initialized) {
            log.info("{} SDK 已初始化，跳过", LOG_PREFIX);
            return true;
        }
        
        try {
            log.info("{} 正在初始化海康 SDK...", LOG_PREFIX);
            
            // TODO: 实际使用时需要加载海康 SDK
            // boolean initResult = HCNetSDK.INSTANCE.NET_DVR_Init();
            // if (!initResult) {
            //     log.error("{} SDK 初始化失败", LOG_PREFIX);
            //     return false;
            // }
            // HCNetSDK.INSTANCE.NET_DVR_SetConnectTime(5000, 1);
            // HCNetSDK.INSTANCE.NET_DVR_SetReconnect(10000, true);
            
            initialized = true;
            log.info("{} ✅ 海康 SDK 初始化成功（模拟模式）", LOG_PREFIX);
            return true;
            
        } catch (Exception e) {
            log.error("{} SDK 初始化异常: {}", LOG_PREFIX, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void cleanup() {
        log.info("{} 正在清理海康 SDK 资源...", LOG_PREFIX);
        
        if (initialized) {
            try {
                // TODO: 实际使用时调用 HCNetSDK.INSTANCE.NET_DVR_Cleanup()
                initialized = false;
                log.info("{} SDK 资源已清理", LOG_PREFIX);
            } catch (Exception e) {
                log.error("{} SDK 清理异常: {}", LOG_PREFIX, e.getMessage(), e);
            }
        }
        
        handleIpMap.clear();
        handlePortMap.clear();
    }

    // ==================== 设备登录/登出 ====================

    @Override
    public IpcLoginResult login(String ip, int port, String username, String password) {
        if (!initialized) {
            // 尝试自动初始化
            if (!initialize()) {
                return IpcLoginResult.failure("SDK 未初始化");
            }
        }
        
        if (!StringUtils.hasText(ip)) {
            return IpcLoginResult.failure("IP 地址不能为空");
        }
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return IpcLoginResult.failure("用户名和密码不能为空");
        }
        
        try {
            log.info("{} 登录设备: ip={}, port={}", LOG_PREFIX, ip, port);
            
            // TODO: 实际使用时调用海康 SDK
            // NET_DVR_DEVICEINFO_V30 deviceInfo = new NET_DVR_DEVICEINFO_V30();
            // int lUserID = HCNetSDK.INSTANCE.NET_DVR_Login_V30(ip, port, username, password, deviceInfo);
            // if (lUserID < 0) {
            //     int errorCode = HCNetSDK.INSTANCE.NET_DVR_GetLastError();
            //     return IpcLoginResult.failure("登录失败，错误码: " + errorCode);
            // }
            
            // 模拟登录成功
            long handle = handleGenerator.incrementAndGet();
            String serialNumber = "HIK_" + ip.replace(".", "") + "_" + System.currentTimeMillis();
            
            // 注册登录信息
            registerLoginInfo(handle, ip, port);
            
            log.info("{} ✅ 登录成功（模拟）: ip={}, handle={}, sn={}", LOG_PREFIX, ip, handle, serialNumber);
            
            IpcLoginResult result = IpcLoginResult.success(handle, serialNumber, "DS-2CD Series", "V5.5.0", 1);
            result.setPtzSupported(true);
            result.setAudioSupported(true);
            
            return result;
            
        } catch (Exception e) {
            log.error("{} 登录异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return IpcLoginResult.failure("登录异常: " + e.getMessage());
        }
    }

    @Override
    public boolean logout(long loginHandle) {
        if (loginHandle == 0) {
            return true;
        }
        
        try {
            log.info("{} 登出设备: handle={}", LOG_PREFIX, loginHandle);
            
            // TODO: 实际使用时调用 HCNetSDK.INSTANCE.NET_DVR_Logout(lUserID)
            
            // 移除登录信息
            removeLoginInfo(loginHandle);
            
            log.info("{} ✅ 登出成功（模拟）: handle={}", LOG_PREFIX, loginHandle);
            return true;
            
        } catch (Exception e) {
            log.error("{} 登出异常: handle={}, error={}", LOG_PREFIX, loginHandle, e.getMessage(), e);
            return false;
        }
    }

    // ==================== PTZ 控制 ====================

    @Override
    public IpcOperationResult ptzControl(long loginHandle, int channelNo, String ptzCommand, int speed) {
        if (loginHandle <= 0) {
            return IpcOperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} PTZ控制: handle={}, channel={}, cmd={}, speed={}", 
                    LOG_PREFIX, loginHandle, channelNo, ptzCommand, speed);
            
            // TODO: 实际使用时调用海康 SDK
            // int dwPTZCommand = convertPtzCommand(ptzCommand);
            // boolean isStop = ptzCommand.endsWith("_STOP");
            // boolean result = HCNetSDK.INSTANCE.NET_DVR_PTZControlWithSpeed_Other(
            //         lUserID, channelNo, dwPTZCommand, isStop ? 1 : 0, speed);
            
            log.info("{} ✅ PTZ控制成功（模拟）", LOG_PREFIX);
            return IpcOperationResult.success("PTZ控制成功");
            
        } catch (Exception e) {
            log.error("{} PTZ控制异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return IpcOperationResult.failure("PTZ控制异常: " + e.getMessage());
        }
    }

    // ==================== 视频预览 ====================

    @Override
    public String buildRtspUrl(String ip, int port, String username, String password, int channelNo, int subtype) {
        if (port <= 0) {
            port = 554;
        }
        if (channelNo <= 0) {
            channelNo = 1;
        }
        
        // 海康 RTSP 格式：
        // 主码流：rtsp://user:pass@ip:554/Streaming/Channels/101
        // 子码流：rtsp://user:pass@ip:554/Streaming/Channels/102
        // 通道号格式：X01=主码流, X02=子码流
        int streamType = (subtype == 1) ? 2 : 1;
        int hikChannel = channelNo * 100 + streamType;
        
        return String.format("rtsp://%s:%s@%s:%d/Streaming/Channels/%d",
                username, password, ip, port, hikChannel);
    }

    // ==================== 抓图 ====================

    @Override
    public IpcOperationResult capturePicture(long loginHandle, int channelNo) {
        if (loginHandle <= 0) {
            return IpcOperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 抓图: handle={}, channel={}", LOG_PREFIX, loginHandle, channelNo);
            
            // TODO: 实际使用时调用海康 SDK 抓图
            // HCNetSDK.NET_DVR_JPEGPARA jpegPara = new HCNetSDK.NET_DVR_JPEGPARA();
            // jpegPara.wPicQuality = 2; // 图片质量：高
            // jpegPara.wPicSize = 0;    // 图片大小：CIF
            // boolean result = HCNetSDK.INSTANCE.NET_DVR_CaptureJPEGPicture(lUserID, channelNo, jpegPara, savePath);
            
            return IpcOperationResult.success("抓图命令已发送（模拟）", Map.of(
                    "channelNo", channelNo,
                    "message", "抓图结果将通过事件回调返回"
            ));
            
        } catch (Exception e) {
            log.error("{} 抓图异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return IpcOperationResult.failure("抓图异常: " + e.getMessage());
        }
    }
}
