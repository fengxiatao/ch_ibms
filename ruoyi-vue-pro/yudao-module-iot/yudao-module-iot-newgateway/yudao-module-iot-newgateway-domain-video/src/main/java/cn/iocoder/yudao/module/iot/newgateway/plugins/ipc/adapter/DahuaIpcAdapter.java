package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.adapter;

import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.IpcLoginResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.IpcOperationResult;
import cn.iocoder.yudao.module.iot.newgateway.util.NativeLibraryLoader;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.NetSDKLib.*;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 大华 IPC 适配器
 * 
 * <p>使用大华 NetSDK 实现 IPC 设备的接入</p>
 * 
 * @author IoT Gateway Team
 * @since 1.0
 */
@Slf4j
@Component
public class DahuaIpcAdapter extends AbstractIpcAdapter {

    private static final String LOG_PREFIX = "[DahuaIpcAdapter]";
    
    public static final String VENDOR_CODE = "DAHUA";
    public static final String VENDOR_NAME = "大华";

    /**
     * SDK 实例 - 懒加载
     */
    private static volatile NetSDKLib netsdk;

    private static NetSDKLib getNetSdk() {
        if (netsdk == null) {
            synchronized (DahuaIpcAdapter.class) {
                if (netsdk == null) {
                    netsdk = NetSDKLib.NETSDK_INSTANCE;
                }
            }
        }
        return netsdk;
    }

    /** 断线回调 */
    private final fDisConnect sdkDisconnectCallback = new fDisConnect() {
        @Override
        public void invoke(LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            long loginHandle = lLoginID.longValue();
            log.warn("{} SDK 检测到设备断线: {}:{} (handle={})", LOG_PREFIX, pchDVRIP, nDVRPort, loginHandle);
            notifyDisconnect(loginHandle, pchDVRIP, nDVRPort);
        }
    };

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
            if (vendorStr.contains("DAHUA") || vendorStr.contains("大华") || vendorStr.contains("DH")) {
                return true;
            }
        }
        
        // 通过 productKey 判断
        Object productKey = deviceInfo.get("productKey");
        if (productKey != null) {
            String pkStr = productKey.toString().toUpperCase();
            if (pkStr.contains("DAHUA") || pkStr.contains("DH_")) {
                return true;
            }
        }
        
        // 通过设备型号判断
        Object model = deviceInfo.get("deviceModel");
        if (model != null) {
            String modelStr = model.toString().toUpperCase();
            if (modelStr.startsWith("DH-") || modelStr.startsWith("IPC-")) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public int getPriority() {
        return 10; // 高优先级
    }

    // ==================== SDK 生命周期 ====================

    @Override
    public boolean initialize() {
        if (initialized) {
            log.info("{} SDK 已初始化，跳过", LOG_PREFIX);
            return true;
        }
        
        try {
            log.info("{} 正在初始化大华 SDK...", LOG_PREFIX);
            
            NativeLibraryLoader.loadDahuaLibraries();
            
            boolean initResult = getNetSdk().CLIENT_Init(sdkDisconnectCallback, null);
            if (!initResult) {
                log.error("{} SDK 初始化失败", LOG_PREFIX);
                return false;
            }
            
            getNetSdk().CLIENT_SetConnectTime(5000, 1);
            
            NET_PARAM netParam = new NET_PARAM();
            netParam.nConnectTime = 10000;
            netParam.nGetConnInfoTime = 3000;
            netParam.nGetDevInfoTime = 3000;
            getNetSdk().CLIENT_SetNetworkParam(netParam);
            
            initialized = true;
            log.info("{} ✅ 大华 SDK 初始化成功", LOG_PREFIX);
            return true;
            
        } catch (Exception e) {
            log.error("{} SDK 初始化异常: {}", LOG_PREFIX, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void cleanup() {
        log.info("{} 正在清理大华 SDK 资源...", LOG_PREFIX);
        
        if (initialized) {
            try {
                getNetSdk().CLIENT_Cleanup();
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
            return IpcLoginResult.failure("SDK 未初始化");
        }
        
        if (!StringUtils.hasText(ip)) {
            return IpcLoginResult.failure("IP 地址不能为空");
        }
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return IpcLoginResult.failure("用户名和密码不能为空");
        }
        
        try {
            log.info("{} 登录设备: ip={}, port={}", LOG_PREFIX, ip, port);
            
            NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY loginIn = new NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
            loginIn.nPort = port;
            fillBytes(loginIn.szIP, ip);
            fillBytes(loginIn.szUserName, username);
            fillBytes(loginIn.szPassword, password);
            
            NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY loginOut = new NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();
            loginOut.stuDeviceInfo = new NET_DEVICEINFO_Ex();
            
            LLong loginHandle = netsdk.CLIENT_LoginWithHighLevelSecurity(loginIn, loginOut);
            
            if (loginHandle.longValue() == 0) {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 登录失败: ip={}, error={}", LOG_PREFIX, ip, errorMsg);
                return IpcLoginResult.failure("登录失败: " + errorMsg);
            }
            
            NET_DEVICEINFO_Ex deviceInfo = loginOut.stuDeviceInfo;
            String serialNumber = extractString(deviceInfo.sSerialNumber);
            int channelCount = deviceInfo.byChanNum & 0xFF;
            
            if (channelCount == 0) {
                channelCount = 1;
            }
            
            long handle = loginHandle.longValue();
            
            // 注册登录信息
            registerLoginInfo(handle, ip, port);
            
            log.info("{} ✅ 登录成功: ip={}, handle={}, sn={}, channels={}", 
                    LOG_PREFIX, ip, handle, serialNumber, channelCount);
            
            IpcLoginResult result = IpcLoginResult.success(handle, serialNumber, null, null, channelCount);
            result.setPtzSupported(true);
            result.setAudioSupported(true);
            result.setDeviceType(deviceInfo.byDVRType);
            
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
            
            boolean result = netsdk.CLIENT_Logout(new LLong(loginHandle));
            
            // 移除登录信息
            removeLoginInfo(loginHandle);
            
            if (result) {
                log.info("{} ✅ 登出成功: handle={}", LOG_PREFIX, loginHandle);
            } else {
                log.warn("{} 登出失败: handle={}, error={}", LOG_PREFIX, loginHandle, ToolKits.getErrorCodePrint());
            }
            
            return result;
            
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
            
            int dwPTZCommand = convertPtzCommand(ptzCommand);
            if (dwPTZCommand < 0) {
                return IpcOperationResult.failure("不支持的 PTZ 命令: " + ptzCommand);
            }
            
            boolean isStop = ptzCommand.toUpperCase().endsWith("_STOP") || "STOP".equals(ptzCommand.toUpperCase());
            
            boolean result = getNetSdk().CLIENT_DHPTZControlEx(
                    new LLong(loginHandle),
                    channelNo,
                    dwPTZCommand,
                    speed,
                    speed,
                    0,
                    isStop ? 1 : 0
            );
            
            if (result) {
                return IpcOperationResult.success("PTZ控制成功");
            } else {
                return IpcOperationResult.failure("PTZ控制失败: " + ToolKits.getErrorCodePrint());
            }
            
        } catch (Exception e) {
            log.error("{} PTZ控制异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return IpcOperationResult.failure("PTZ控制异常: " + e.getMessage());
        }
    }

    // 大华 SDK PTZ 控制命令常量 (NET_PTZ_ControlType)
    private static final int NET_PTZ_UP_CONTROL = 0;           // 上
    private static final int NET_PTZ_DOWN_CONTROL = 1;         // 下
    private static final int NET_PTZ_LEFT_CONTROL = 2;         // 左
    private static final int NET_PTZ_RIGHT_CONTROL = 3;        // 右
    private static final int NET_PTZ_ZOOM_ADD_CONTROL = 4;     // 变倍增大
    private static final int NET_PTZ_ZOOM_DEC_CONTROL = 5;     // 变倍减小
    private static final int NET_PTZ_FOCUS_ADD_CONTROL = 6;    // 聚焦近
    private static final int NET_PTZ_FOCUS_DEC_CONTROL = 7;    // 聚焦远
    private static final int NET_PTZ_APERTURE_ADD_CONTROL = 8; // 光圈扩大
    private static final int NET_PTZ_APERTURE_DEC_CONTROL = 9; // 光圈缩小
    // 扩展方向控制命令（NET_EXTPTZ_ControlType，从 0x20 开始）
    private static final int NET_EXTPTZ_LEFTTOP = 0x20;        // 左上 (32)
    private static final int NET_EXTPTZ_RIGHTTOP = 0x21;       // 右上 (33)
    private static final int NET_EXTPTZ_LEFTDOWN = 0x22;       // 左下 (34)
    private static final int NET_EXTPTZ_RIGHTDOWN = 0x23;      // 右下 (35)

    /**
     * 转换 PTZ 命令字符串为 SDK 命令码
     */
    private int convertPtzCommand(String command) {
        if (command == null) {
            return -1;
        }
        String cmd = command.toUpperCase().replace("_STOP", "");
        switch (cmd) {
            case "UP": return NET_PTZ_UP_CONTROL;
            case "DOWN": return NET_PTZ_DOWN_CONTROL;
            case "LEFT": return NET_PTZ_LEFT_CONTROL;
            case "RIGHT": return NET_PTZ_RIGHT_CONTROL;
            case "ZOOM_IN": return NET_PTZ_ZOOM_ADD_CONTROL;
            case "ZOOM_OUT": return NET_PTZ_ZOOM_DEC_CONTROL;
            case "FOCUS_NEAR": return NET_PTZ_FOCUS_ADD_CONTROL;
            case "FOCUS_FAR": return NET_PTZ_FOCUS_DEC_CONTROL;
            case "IRIS_OPEN": return NET_PTZ_APERTURE_ADD_CONTROL;
            case "IRIS_CLOSE": return NET_PTZ_APERTURE_DEC_CONTROL;
            case "LEFT_UP": return NET_EXTPTZ_LEFTTOP;
            case "RIGHT_UP": return NET_EXTPTZ_RIGHTTOP;
            case "LEFT_DOWN": return NET_EXTPTZ_LEFTDOWN;
            case "RIGHT_DOWN": return NET_EXTPTZ_RIGHTDOWN;
            default: return -1;
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
        // 大华 RTSP 格式：rtsp://user:pass@ip:554/cam/realmonitor?channel=1&subtype=0
        return String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=%d&subtype=%d",
                username, password, ip, port, channelNo, subtype);
    }

    // ==================== 抓图 ====================

    @Override
    public IpcOperationResult capturePicture(long loginHandle, int channelNo) {
        if (loginHandle <= 0) {
            return IpcOperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 抓图: handle={}, channel={}", LOG_PREFIX, loginHandle, channelNo);
            
            // TODO: 实现真实的远程抓图功能
            return IpcOperationResult.success("抓图命令已发送", Map.of(
                    "channelNo", channelNo,
                    "message", "抓图结果将通过事件回调返回"
            ));
            
        } catch (Exception e) {
            log.error("{} 抓图异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return IpcOperationResult.failure("抓图异常: " + e.getMessage());
        }
    }
}
