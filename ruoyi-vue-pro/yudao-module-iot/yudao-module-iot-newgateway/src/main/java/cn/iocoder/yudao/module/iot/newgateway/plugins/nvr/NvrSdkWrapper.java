package cn.iocoder.yudao.module.iot.newgateway.plugins.nvr;

import cn.iocoder.yudao.module.iot.newgateway.plugins.nvr.dto.*;
import cn.iocoder.yudao.module.iot.newgateway.util.NativeLibraryLoader;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.NetSDKLib.*;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NVR 设备 SDK 封装
 * 
 * <p>封装大华 NetSDK 的 NVR 相关操作：</p>
 * <ul>
 *     <li>SDK 初始化和清理</li>
 *     <li>设备登录/登出</li>
 *     <li>PTZ 云台控制</li>
 *     <li>录像回放控制</li>
 *     <li>录像文件查询</li>
 *     <li>通道信息查询</li>
 *     <li>硬盘状态查询</li>
 *     <li>抓图</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see NvrPlugin
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "nvr", havingValue = "true", matchIfMissing = true)
public class NvrSdkWrapper {

    private static final String LOG_PREFIX = "[NvrSdkWrapper]";
    /**
     * 说明：禁止在运行态伪造/模拟设备返回数据。
     * <p>
     * 该类仅允许通过真实 SDK 与设备交互；若某能力尚未接入，请返回失败并提示。
     */
    private static final String UNSUPPORTED_MSG =
            "NVR 功能尚未接入真实 SDK 调用（已移除内存模拟），请通过 newgateway 真实设备回调/结果驱动";
    
    /**
     * SDK 实例 - 懒加载，避免类加载阶段触发 JNA 原生库加载导致 Spring 启动失败
     */
    private static volatile NetSDKLib netsdk;

    private static NetSDKLib getNetSdk() {
        if (netsdk == null) {
            synchronized (NvrSdkWrapper.class) {
                if (netsdk == null) {
                    netsdk = NetSDKLib.NETSDK_INSTANCE;
                }
            }
        }
        return netsdk;
    }
    
    private static final int TIMEOUT_MS = 10000;
    
    private volatile boolean sdkInitialized = false;
    
    /** 回放句柄存储 */
    private final Map<Long, Long> playbackHandles = new ConcurrentHashMap<>();
    
    /** 通道信息缓存 */
    private final Map<Long, List<Map<String, Object>>> channelCache = new ConcurrentHashMap<>();

    /** 直连 IPC 登录句柄缓存：key=ip, value=登录句柄 */
    private final Map<String, LLong> ipcLoginCache = new ConcurrentHashMap<>();
    
    /** IPC 登录时间缓存：key=ip, value=登录时间(毫秒) */
    private final Map<String, Long> ipcLoginTimeCache = new ConcurrentHashMap<>();
    
    /** IPC 登录会话超时时间：60秒不活动则登出 */
    private static final long IPC_SESSION_TIMEOUT_MS = 60000;

    /** 断线监听器列表 */
    private final List<DisconnectListener> disconnectListeners = new ArrayList<>();
    
    /**
     * 断线监听器接口
     */
    public interface DisconnectListener {
        /**
         * 设备断线回调
         *
         * @param loginHandle 登录句柄
         * @param ip          设备IP
         * @param port        设备端口
         */
        void onDisconnect(long loginHandle, String ip, int port);
    }
    
    /**
     * 注册断线监听器
     *
     * @param listener 断线监听器
     */
    public void addDisconnectListener(DisconnectListener listener) {
        if (listener != null && !disconnectListeners.contains(listener)) {
            disconnectListeners.add(listener);
            log.info("{} 注册断线监听器: {}", LOG_PREFIX, listener.getClass().getSimpleName());
        }
    }
    
    /**
     * 移除断线监听器
     *
     * @param listener 断线监听器
     */
    public void removeDisconnectListener(DisconnectListener listener) {
        if (listener != null) {
            disconnectListeners.remove(listener);
        }
    }

    /** 断线回调 */
    private final fDisConnect disconnectCallback = new fDisConnect() {
        @Override
        public void invoke(LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            String deviceKey = pchDVRIP + ":" + nDVRPort;
            long loginHandle = lLoginID.longValue();
            log.warn("{} SDK 检测到设备断线: {} (loginHandle={})", LOG_PREFIX, deviceKey, loginHandle);
            
            // 通知所有监听器
            for (DisconnectListener listener : disconnectListeners) {
                try {
                    listener.onDisconnect(loginHandle, pchDVRIP, nDVRPort);
                } catch (Exception e) {
                    log.error("{} 断线监听器处理异常: {}", LOG_PREFIX, e.getMessage(), e);
                }
            }
        }
    };

    // ==================== 生命周期方法 ====================

    @PostConstruct
    public void initialize() {
        if (sdkInitialized) {
            log.info("{} SDK 已初始化，跳过", LOG_PREFIX);
            return;
        }
        
        try {
            log.info("{} 正在初始化 SDK...", LOG_PREFIX);
            
            NativeLibraryLoader.loadDahuaLibraries();
            
            boolean initResult = getNetSdk().CLIENT_Init(disconnectCallback, null);
            if (!initResult) {
                log.error("{} SDK 初始化失败", LOG_PREFIX);
                return;
            }
            
            getNetSdk().CLIENT_SetConnectTime(5000, 1);
            
            NET_PARAM netParam = new NET_PARAM();
            netParam.nConnectTime = 10000;
            netParam.nGetConnInfoTime = 3000;
            netParam.nGetDevInfoTime = 3000;
            getNetSdk().CLIENT_SetNetworkParam(netParam);
            
            sdkInitialized = true;
            log.info("{} ✅ SDK 初始化成功", LOG_PREFIX);
            
        } catch (Exception e) {
            log.error("{} SDK 初始化异常: {}", LOG_PREFIX, e.getMessage(), e);
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("{} 正在清理 SDK 资源...", LOG_PREFIX);
        
        // 清除所有 IPC 会话
        clearAllIpcSessions();
        
        if (sdkInitialized) {
            try {
                getNetSdk().CLIENT_Cleanup();
                sdkInitialized = false;
                log.info("{} SDK 资源已清理", LOG_PREFIX);
            } catch (Exception e) {
                log.error("{} SDK 清理异常: {}", LOG_PREFIX, e.getMessage(), e);
            }
        }
        
        playbackHandles.clear();
        channelCache.clear();
    }

    public boolean isInitialized() {
        return sdkInitialized;
    }

    // ==================== 设备登录/登出 ====================

    public NvrLoginResult login(String ip, int port, String username, String password) {
        if (!sdkInitialized) {
            return NvrLoginResult.failure("SDK 未初始化");
        }
        
        if (!StringUtils.hasText(ip)) {
            return NvrLoginResult.failure("IP 地址不能为空");
        }
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return NvrLoginResult.failure("用户名和密码不能为空");
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
                return NvrLoginResult.failure("登录失败: " + errorMsg);
            }
            
            NET_DEVICEINFO_Ex deviceInfo = loginOut.stuDeviceInfo;
            String serialNumber = new String(deviceInfo.sSerialNumber).trim();
            int channelCount = deviceInfo.byChanNum & 0xFF;
            int diskCount = deviceInfo.byDiskNum & 0xFF;
            
            long handle = loginHandle.longValue();
            
            log.info("{} ✅ 登录成功: ip={}, handle={}, sn={}, channels={}, disks={}", 
                    LOG_PREFIX, ip, handle, serialNumber, channelCount, diskCount);
            
            return NvrLoginResult.success(handle, serialNumber, channelCount, diskCount, deviceInfo.byDVRType);
            
        } catch (Exception e) {
            log.error("{} 登录异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrLoginResult.failure("登录异常: " + e.getMessage());
        }
    }

    public boolean logout(long loginHandle) {
        if (loginHandle == 0) {
            return true;
        }
        
        try {
            log.info("{} 登出设备: handle={}", LOG_PREFIX, loginHandle);
            
            boolean result = netsdk.CLIENT_Logout(new LLong(loginHandle));
            
            playbackHandles.remove(loginHandle);
            channelCache.remove(loginHandle);
            
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

    // 大华 SDK PTZ 控制命令常量 (NET_PTZ_ControlType)
    private static final int NET_PTZ_UP_CONTROL = 0;           // 上
    private static final int NET_PTZ_DOWN_CONTROL = 1;         // 下
    private static final int NET_PTZ_LEFT_CONTROL = 2;         // 左
    private static final int NET_PTZ_RIGHT_CONTROL = 3;        // 右
    private static final int NET_PTZ_ZOOM_ADD_CONTROL = 4;     // 变倍增大（放大）
    private static final int NET_PTZ_ZOOM_DEC_CONTROL = 5;     // 变倍减小（缩小）
    private static final int NET_PTZ_FOCUS_ADD_CONTROL = 6;    // 聚焦近
    private static final int NET_PTZ_FOCUS_DEC_CONTROL = 7;    // 聚焦远
    private static final int NET_PTZ_APERTURE_ADD_CONTROL = 8; // 光圈扩大
    private static final int NET_PTZ_APERTURE_DEC_CONTROL = 9; // 光圈缩小
    // 扩展方向控制命令（NET_EXTPTZ_ControlType，从 0x20 开始）
    private static final int NET_EXTPTZ_LEFTTOP = 0x20;        // 左上 (32)
    private static final int NET_EXTPTZ_RIGHTTOP = 0x21;       // 右上 (33)
    private static final int NET_EXTPTZ_LEFTDOWN = 0x22;       // 左下 (34)
    private static final int NET_EXTPTZ_RIGHTDOWN = 0x23;      // 右下 (35)
    // 预设点控制命令（NET_PTZ_ControlType）- 标准命令
    private static final int NET_PTZ_POINT_MOVE_CONTROL = 10;  // 转到预设点
    private static final int NET_PTZ_POINT_SET_CONTROL = 11;   // 设置预设点
    private static final int NET_PTZ_POINT_DEL_CONTROL = 12;   // 删除预设点
    
    // 预设点控制命令（NET_EXTPTZ_ControlType）- 扩展命令（某些设备需要使用）
    // NET_EXTPTZ_GOTOPRESET = 0x48 = 72 (需要使用结构体 PTZ_CONTROL_GOTOPRESET)
    private static final int NET_EXTPTZ_GOTOPRESET = 0x48;     // 扩展命令：转到预设点
    
    // 3D 定位/快速定位命令（用于区域框选放大）
    // NET_EXTPTZ_FASTGOTO = 0x33 = 51
    // 参数说明: param1=水平坐标(0-8192), param2=垂直坐标(0-8192), param3=变倍(1-128)
    private static final int NET_EXTPTZ_FASTGOTO = 0x33;       // 快速定位（3D定位/定点放大）
    
    // 预设点配置命令（用于 CLIENT_GetNewDevConfig / CLIENT_SetNewDevConfig）
    private static final String CFG_CMD_PTZ_PRESET = "PtzPreset";

    public NvrOperationResult ptzControl(long loginHandle, int channelNo, String ptzCommand, int speed) {
        if (loginHandle <= 0) {
            return NvrOperationResult.failure("无效的登录句柄");
        }
        
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        try {
            // 大华 SDK 通道号从 0 开始，业务层通道号从 1 开始，需要减 1
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            log.info("{} PTZ控制: handle={}, bizChannel={}, sdkChannel={}, cmd={}, speed={}", 
                    LOG_PREFIX, loginHandle, channelNo, sdkChannelNo, ptzCommand, speed);
            
            int dwPTZCommand = convertPtzCommand(ptzCommand);
            if (dwPTZCommand < 0) {
                return NvrOperationResult.failure("不支持的 PTZ 命令: " + ptzCommand);
            }
            
            boolean isStop = ptzCommand.toUpperCase().endsWith("_STOP") || "STOP".equals(ptzCommand.toUpperCase());
            
            // 停止命令时速度参数必须为 0
            int actualSpeed = isStop ? 0 : speed;
            
            // 根据官方 Demo，不同命令的参数不同：
            // - 方向控制（上下左右及对角）: param1=speed, param2=speed
            // - 变倍/变焦/光圈控制: param1=0, param2=speed
            int param1, param2;
            boolean isZoomFocusIris = isZoomFocusIrisCommand(dwPTZCommand);
            
            if (isZoomFocusIris) {
                param1 = 0;
                param2 = actualSpeed;
            } else {
                param1 = actualSpeed;
                param2 = actualSpeed;
            }
            
            // 调用大华 SDK 的 PTZ 控制接口（使用 CLIENT_DHPTZControlEx2，sdkChannelNo 从 0 开始）
            boolean result = getNetSdk().CLIENT_DHPTZControlEx2(
                    new LLong(loginHandle),
                    sdkChannelNo,
                    dwPTZCommand,
                    param1,
                    param2,
                    0,
                    isStop ? 1 : 0,
                    null  // param4: 简单操作传 null
            );
            
            if (result) {
                log.info("{} PTZ控制成功: bizChannel={}, sdkChannel={}, cmd={}, stop={}", 
                        LOG_PREFIX, channelNo, sdkChannelNo, ptzCommand, isStop);
                return NvrOperationResult.success("PTZ控制成功");
            } else {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.warn("{} PTZ控制失败: bizChannel={}, sdkChannel={}, cmd={}, error={}", 
                        LOG_PREFIX, channelNo, sdkChannelNo, ptzCommand, errorMsg);
                return NvrOperationResult.failure("PTZ控制失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("{} PTZ控制异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return NvrOperationResult.failure("PTZ控制异常: " + e.getMessage());
        }
    }

    /**
     * 直连 IPC 进行 PTZ 控制
     * 
     * <p>当 NVR 下挂的是远程 IPC（如球机）时，PTZ 控制需要直接连接 IPC 设备，
     * 而不是通过 NVR 转发。此方法会临时登录 IPC，执行 PTZ 命令，然后登出。</p>
     * 
     * @param ip        IPC 设备 IP
     * @param username  用户名
     * @param password  密码
     * @param channelNo 通道号（通常为 0 或 1）
     * @param ptzCommand PTZ 命令
     * @param speed     速度 1-8
     * @return 操作结果
     */
    public NvrOperationResult ptzControlDirect(String ip, String username, String password, 
                                                int channelNo, String ptzCommand, int speed) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        if (ip == null || ip.isEmpty()) {
            return NvrOperationResult.failure("IP 地址不能为空");
        }
        
        try {
            log.info("{} 直连IPC PTZ控制: ip={}, channel={}, cmd={}, speed={}", 
                    LOG_PREFIX, ip, channelNo, ptzCommand, speed);
            
            // 1. 获取或创建 IPC 登录会话（使用缓存）
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            // 2. 执行 PTZ 控制
            int dwPTZCommand = convertPtzCommand(ptzCommand);
            if (dwPTZCommand < 0) {
                return NvrOperationResult.failure("不支持的 PTZ 命令: " + ptzCommand);
            }
            
            boolean isStop = ptzCommand.toUpperCase().endsWith("_STOP") || "STOP".equals(ptzCommand.toUpperCase());
            
            // 大华 SDK 通道号从 0 开始
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            // 停止命令时速度参数必须为 0
            int actualSpeed = isStop ? 0 : speed;
            
            // 根据官方 Demo，不同命令的参数不同：
            // - 方向控制（上下左右及对角）: param1=speed, param2=speed
            // - 变倍/变焦/光圈控制: param1=0, param2=speed
            int param1, param2;
            boolean isZoomFocusIris = isZoomFocusIrisCommand(dwPTZCommand);
            
            if (isZoomFocusIris) {
                // 变倍/变焦/光圈控制：param1=0, param2=speed
                param1 = 0;
                param2 = actualSpeed;
            } else {
                // 方向控制：param1=speed, param2=speed
                param1 = actualSpeed;
                param2 = actualSpeed;
            }
            
            log.info("{} PTZ SDK调用参数: handle={}, sdkChannel={}, cmd={}, param1={}, param2={}, dwStop={}, isZoomFocusIris={}", 
                    LOG_PREFIX, loginHandle.longValue(), sdkChannelNo, dwPTZCommand, param1, param2, isStop ? 1 : 0, isZoomFocusIris);
            
            log.info("{} >>> 开始调用 CLIENT_DHPTZControlEx2 控制...", LOG_PREFIX);
            boolean result;
            try {
                // 根据官方文档，使用 CLIENT_DHPTZControlEx2，param4 传 null
                result = getNetSdk().CLIENT_DHPTZControlEx2(
                        loginHandle,
                        sdkChannelNo,
                        dwPTZCommand,
                        param1,
                        param2,
                        0,
                        isStop ? 1 : 0,
                        null  // param4: 简单操作传 null
                );
                log.info("{} <<< CLIENT_DHPTZControlEx2 控制完成, result={}", LOG_PREFIX, result);
            } catch (Throwable t) {
                log.error("{} !!! CLIENT_DHPTZControlEx2 控制抛出异常: {}", LOG_PREFIX, t.getMessage(), t);
                throw t;
            }
            
            // 获取 SDK 错误码用于调试
            String sdkErrorInfo = ToolKits.getErrorCodePrint();
            int lastError = getNetSdk().CLIENT_GetLastError();
            log.info("{} PTZ方向SDK调用结果: result={}, lastError={}, errorInfo={}", LOG_PREFIX, result, lastError, sdkErrorInfo);
            
            if (result) {
                log.info("{} 直连IPC PTZ控制成功: ip={}, channel={}, cmd={}", LOG_PREFIX, ip, channelNo, ptzCommand);
                // 更新会话活跃时间
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success("PTZ控制成功");
            } else {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.warn("{} 直连IPC PTZ控制失败: ip={}, channel={}, cmd={}, error={}", 
                        LOG_PREFIX, ip, channelNo, ptzCommand, errorMsg);
                // 控制失败可能是会话失效，清除缓存
                clearIpcSession(ip);
                return NvrOperationResult.failure("PTZ控制失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("{} 直连IPC PTZ控制异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            // 异常时清除缓存
            clearIpcSession(ip);
            return NvrOperationResult.failure("PTZ控制异常: " + e.getMessage());
        }
        // 注意：不再在 finally 中登出，会话由缓存管理
    }

    /**
     * 3D 定位/区域放大控制（直连 IPC）
     * 
     * <p>用于在视频画面上框选区域进行快速定位放大。
     * 用户在前端画面上框选一个矩形区域，将矩形的中心点坐标和放大倍数传入此方法，
     * 摄像头会自动移动并放大到指定区域。</p>
     * 
     * <p>坐标系说明：</p>
     * <ul>
     *     <li>x, y 为归一化坐标（0-8192），其中 (0,0) 为画面左上角，(8192,8192) 为画面右下角</li>
     *     <li>zoom 为变倍值（1-128），数值越大放大倍数越高</li>
     * </ul>
     * 
     * @param ip        IPC 设备 IP
     * @param username  用户名
     * @param password  密码
     * @param channelNo 通道号
     * @param x         水平坐标（0-8192）
     * @param y         垂直坐标（0-8192）
     * @param zoom      变倍值（1-128）
     * @return 操作结果
     */
    public NvrOperationResult ptz3DPositionDirect(String ip, String username, String password, 
                                                   int channelNo, int x, int y, int zoom) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        if (ip == null || ip.isEmpty()) {
            return NvrOperationResult.failure("IP 地址不能为空");
        }
        
        // 参数校验
        if (x < 0 || x > 8192) {
            return NvrOperationResult.failure("水平坐标 x 必须在 0-8192 范围内");
        }
        if (y < 0 || y > 8192) {
            return NvrOperationResult.failure("垂直坐标 y 必须在 0-8192 范围内");
        }
        if (zoom < 1 || zoom > 128) {
            return NvrOperationResult.failure("变倍值 zoom 必须在 1-128 范围内");
        }
        
        try {
            log.info("{} 3D定位控制: ip={}, channel={}, x={}, y={}, zoom={}", 
                    LOG_PREFIX, ip, channelNo, x, y, zoom);
            
            // 获取或创建 IPC 登录会话
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            // 大华 SDK 通道号从 0 开始
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            log.info("{} 3D定位SDK调用参数: handle={}, sdkChannel={}, cmd={}, x={}, y={}, zoom={}", 
                    LOG_PREFIX, loginHandle.longValue(), sdkChannelNo, NET_EXTPTZ_FASTGOTO, x, y, zoom);
            
            // 调用 SDK 的 3D 定位命令
            // NET_EXTPTZ_FASTGOTO: param1=水平坐标, param2=垂直坐标, param3=变倍
            boolean result = getNetSdk().CLIENT_DHPTZControlEx(
                    loginHandle,
                    sdkChannelNo,
                    NET_EXTPTZ_FASTGOTO,
                    x,      // param1: 水平坐标 (0-8192)
                    y,      // param2: 垂直坐标 (0-8192)
                    zoom,   // param3: 变倍值 (1-128)
                    0       // dwStop: 0=执行
            );
            
            String errorInfo = ToolKits.getErrorCodePrint();
            int lastError = getNetSdk().CLIENT_GetLastError();
            log.info("{} 3D定位SDK调用结果: result={}, lastError={}, errorInfo={}", 
                    LOG_PREFIX, result, lastError, errorInfo);
            
            if (result) {
                log.info("{} 3D定位控制成功: ip={}, x={}, y={}, zoom={}", LOG_PREFIX, ip, x, y, zoom);
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success("3D定位控制成功");
            } else {
                log.warn("{} 3D定位控制失败: ip={}, x={}, y={}, zoom={}, error={}", 
                        LOG_PREFIX, ip, x, y, zoom, errorInfo);
                return NvrOperationResult.failure("3D定位控制失败: " + errorInfo);
            }
            
        } catch (Exception e) {
            log.error("{} 3D定位控制异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrOperationResult.failure("3D定位控制异常: " + e.getMessage());
        }
    }

    /**
     * 区域放大控制（直连 IPC）
     * 
     * <p>用于根据框选的矩形区域进行放大。与 3D 定位不同，此方法直接接收矩形的
     * 左上角和右下角坐标，自动计算中心点和放大倍数。</p>
     * 
     * @param ip        IPC 设备 IP
     * @param username  用户名
     * @param password  密码
     * @param channelNo 通道号
     * @param startX    矩形左上角 X 坐标（0-8192）
     * @param startY    矩形左上角 Y 坐标（0-8192）
     * @param endX      矩形右下角 X 坐标（0-8192）
     * @param endY      矩形右下角 Y 坐标（0-8192）
     * @return 操作结果
     */
    public NvrOperationResult ptzAreaZoomDirect(String ip, String username, String password, 
                                                 int channelNo, int startX, int startY, int endX, int endY) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        // 确保坐标正确（左上角到右下角）
        int x1 = Math.min(startX, endX);
        int y1 = Math.min(startY, endY);
        int x2 = Math.max(startX, endX);
        int y2 = Math.max(startY, endY);
        
        // 计算中心点
        int centerX = (x1 + x2) / 2;
        int centerY = (y1 + y2) / 2;
        
        // 计算放大倍数（基于框选区域大小）
        // 框选区域越小，放大倍数越大
        int width = x2 - x1;
        int height = y2 - y1;
        
        // 使用较大的维度来计算放大倍数
        int maxDimension = Math.max(width, height);
        
        // 计算放大倍数：全画面 (8192) 时为 1 倍，框选区域越小倍数越大
        // 公式：zoom = 8192 / maxDimension，但限制在 1-128 范围内
        int zoom;
        if (maxDimension <= 0) {
            zoom = 128; // 点击时最大放大
        } else {
            zoom = Math.max(1, Math.min(128, 8192 / maxDimension));
        }
        
        log.info("{} 区域放大: 矩形({},{}) -> ({},{}), 中心({},{}), 放大倍数={}", 
                LOG_PREFIX, x1, y1, x2, y2, centerX, centerY, zoom);
        
        // 调用 3D 定位方法
        return ptz3DPositionDirect(ip, username, password, channelNo, centerX, centerY, zoom);
    }
    
    /**
     * 获取或创建 IPC 登录会话
     * 
     * <p>复用已有的登录会话，避免频繁登录/登出导致设备拒绝连接</p>
     */
    private synchronized LLong getOrCreateIpcSession(String ip, String username, String password) {
        // 检查是否有缓存的会话
        LLong cachedHandle = ipcLoginCache.get(ip);
        Long loginTime = ipcLoginTimeCache.get(ip);
        
        // 如果有缓存且未超时，直接返回
        if (cachedHandle != null && cachedHandle.longValue() > 0 && loginTime != null) {
            long elapsed = System.currentTimeMillis() - loginTime;
            if (elapsed < IPC_SESSION_TIMEOUT_MS) {
                log.debug("{} 复用 IPC 会话: ip={}, handle={}, elapsed={}ms", 
                        LOG_PREFIX, ip, cachedHandle.longValue(), elapsed);
                return cachedHandle;
            } else {
                // 会话超时，清除并重新登录
                log.info("{} IPC 会话超时，重新登录: ip={}, elapsed={}ms", LOG_PREFIX, ip, elapsed);
                clearIpcSession(ip);
            }
        }
        
        // 创建新的登录会话
        try {
            NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY loginIn = new NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
            loginIn.nPort = 37777;  // 大华默认端口
            System.arraycopy(ip.getBytes(), 0, loginIn.szIP, 0, Math.min(ip.length(), loginIn.szIP.length - 1));
            System.arraycopy(username.getBytes(), 0, loginIn.szUserName, 0, Math.min(username.length(), loginIn.szUserName.length - 1));
            System.arraycopy(password.getBytes(), 0, loginIn.szPassword, 0, Math.min(password.length(), loginIn.szPassword.length - 1));
            
            NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY loginOut = new NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();
            loginOut.stuDeviceInfo = new NET_DEVICEINFO_Ex();
            
            LLong loginHandle = getNetSdk().CLIENT_LoginWithHighLevelSecurity(loginIn, loginOut);
            
            if (loginHandle == null || loginHandle.longValue() == 0) {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.warn("{} 直连IPC登录失败: ip={}, error={}", LOG_PREFIX, ip, errorMsg);
                return null;
            }
            
            log.info("{} 直连IPC登录成功(新会话): ip={}, handle={}", LOG_PREFIX, ip, loginHandle.longValue());
            
            // 缓存会话
            ipcLoginCache.put(ip, loginHandle);
            ipcLoginTimeCache.put(ip, System.currentTimeMillis());
            
            return loginHandle;
        } catch (Exception e) {
            log.error("{} 创建 IPC 会话异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 清除 IPC 登录会话
     */
    private void clearIpcSession(String ip) {
        LLong handle = ipcLoginCache.remove(ip);
        ipcLoginTimeCache.remove(ip);
        
        if (handle != null && handle.longValue() > 0) {
            try {
                getNetSdk().CLIENT_Logout(handle);
                log.info("{} 已清除 IPC 会话: ip={}", LOG_PREFIX, ip);
            } catch (Exception e) {
                log.warn("{} 清除 IPC 会话异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage());
            }
        }
    }
    
    /**
     * 清除所有 IPC 会话（在 SDK 清理时调用）
     */
    private void clearAllIpcSessions() {
        for (String ip : new ArrayList<>(ipcLoginCache.keySet())) {
            clearIpcSession(ip);
        }
        log.info("{} 已清除所有 IPC 会话", LOG_PREFIX);
    }

    /**
     * 判断是否是变倍/变焦/光圈命令
     * 这些命令的参数格式与方向控制不同：param1=0, param2=speed
     */
    private boolean isZoomFocusIrisCommand(int dwPTZCommand) {
        return dwPTZCommand == NET_PTZ_ZOOM_ADD_CONTROL ||    // 变倍+
               dwPTZCommand == NET_PTZ_ZOOM_DEC_CONTROL ||    // 变倍-
               dwPTZCommand == NET_PTZ_FOCUS_ADD_CONTROL ||   // 聚焦近
               dwPTZCommand == NET_PTZ_FOCUS_DEC_CONTROL ||   // 聚焦远
               dwPTZCommand == NET_PTZ_APERTURE_ADD_CONTROL || // 光圈+
               dwPTZCommand == NET_PTZ_APERTURE_DEC_CONTROL;   // 光圈-
    }

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
            case "ZOOM_IN": case "ZOOMTELE": return NET_PTZ_ZOOM_ADD_CONTROL;
            case "ZOOM_OUT": case "ZOOMWIDE": return NET_PTZ_ZOOM_DEC_CONTROL;
            case "FOCUS_NEAR": case "FOCUSNEAR": return NET_PTZ_FOCUS_ADD_CONTROL;
            case "FOCUS_FAR": case "FOCUSFAR": return NET_PTZ_FOCUS_DEC_CONTROL;
            case "IRIS_OPEN": case "IRISLARGE": return NET_PTZ_APERTURE_ADD_CONTROL;
            case "IRIS_CLOSE": case "IRISSMALL": return NET_PTZ_APERTURE_DEC_CONTROL;
            case "LEFT_UP": case "LEFTUP": return NET_EXTPTZ_LEFTTOP;
            case "RIGHT_UP": case "RIGHTUP": return NET_EXTPTZ_RIGHTTOP;
            case "LEFT_DOWN": case "LEFTDOWN": return NET_EXTPTZ_LEFTDOWN;
            case "RIGHT_DOWN": case "RIGHTDOWN": return NET_EXTPTZ_RIGHTDOWN;
            default: return -1;
        }
    }

    // ==================== 预设点控制 ====================

    /**
     * 预设点控制（通过 NVR）
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（业务层从1开始）
     * @param presetNo    预设点编号（1-255）
     * @param action      动作：GOTO=转到, SET=设置, CLEAR=删除
     * @return 操作结果
     */
    public NvrOperationResult presetControl(long loginHandle, int channelNo, int presetNo, String action) {
        if (loginHandle <= 0) {
            return NvrOperationResult.failure("无效的登录句柄");
        }
        
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }

        if (presetNo < 1 || presetNo > 255) {
            return NvrOperationResult.failure("预设点编号必须在 1-255 之间");
        }

        try {
            // 大华 SDK 通道号从 0 开始，业务层通道号从 1 开始
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            int ptzCommand = convertPresetAction(action);
            if (ptzCommand < 0) {
                return NvrOperationResult.failure("不支持的预设点操作: " + action);
            }
            
            log.info("{} 预设点控制: handle={}, bizChannel={}, sdkChannel={}, presetNo={}, action={}", 
                    LOG_PREFIX, loginHandle, channelNo, sdkChannelNo, presetNo, action);
            
            // 调用大华 SDK 的 PTZ 控制接口（使用 CLIENT_DHPTZControlEx2）
            // 参照方向控制：param1=预设点编号，param2=速度(可选)，param3=0
            boolean result = getNetSdk().CLIENT_DHPTZControlEx2(
                    new LLong(loginHandle),
                    sdkChannelNo,
                    ptzCommand,
                    presetNo,  // param1: 预设点编号
                    4,         // param2: 速度参数（参照方向控制）
                    0,         // param3: 未使用
                    0,         // dwStop: 0=执行
                    null       // param4: 简单操作传 null
            );
            
            if (result) {
                log.info("{} 预设点控制成功: channel={}, presetNo={}, action={}", 
                        LOG_PREFIX, channelNo, presetNo, action);
                return NvrOperationResult.success("预设点控制成功");
            } else {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.warn("{} 预设点控制失败: channel={}, presetNo={}, action={}, error={}", 
                        LOG_PREFIX, channelNo, presetNo, action, errorMsg);
                return NvrOperationResult.failure("预设点控制失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("{} 预设点控制异常: channel={}, presetNo={}, action={}, error={}", 
                    LOG_PREFIX, channelNo, presetNo, action, e.getMessage(), e);
            return NvrOperationResult.failure("预设点控制异常: " + e.getMessage());
        }
    }

    /**
     * 预设点控制（直连 IPC）
     * 
     * @param ip        IPC 设备 IP
     * @param username  用户名
     * @param password  密码
     * @param channelNo 通道号
     * @param presetNo  预设点编号（1-255）
     * @param action    动作：GOTO=转到, SET=设置, CLEAR=删除
     * @param presetName 预设点名称（SET操作时使用，可选）
     * @return 操作结果
     */
    public NvrOperationResult presetControlDirect(String ip, String username, String password,
                                                   int channelNo, int presetNo, String action, String presetName) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        if (ip == null || ip.isEmpty()) {
            return NvrOperationResult.failure("IP 地址不能为空");
        }

        if (presetNo < 1 || presetNo > 255) {
            return NvrOperationResult.failure("预设点编号必须在 1-255 之间");
        }

        try {
            log.info("{} 直连IPC预设点控制: ip={}, channel={}, presetNo={}, action={}", 
                    LOG_PREFIX, ip, channelNo, presetNo, action);
            
            // 获取或创建 IPC 登录会话
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            int ptzCommand = convertPresetAction(action);
            if (ptzCommand < 0) {
                return NvrOperationResult.failure("不支持的预设点操作: " + action);
            }
            
            // 直连 IPC 时，使用与方向控制相同的通道号（已验证可用）
            // 双镜头球机：channelNo=2 -> sdkChannel=1（PTZ通道）
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            log.info("{} 直连IPC预设点控制: sdkChannel={} (原始 channelNo={})", 
                    LOG_PREFIX, sdkChannelNo, channelNo);
            
            // 根据官方示例，参数规范：
            // GOTO (转到预设点): param1=presetIndex, param2=speed, param3=0, dwStop=0
            // SET  (设置预设点): param1=presetIndex, param2=0, param3=0, dwStop=0
            // DEL  (删除预设点): param1=presetIndex, param2=0, param3=0, dwStop=0
            int param2 = (ptzCommand == NET_PTZ_POINT_MOVE_CONTROL) ? 4 : 0;  // 只有 GOTO 需要速度
            
            // 根据官方示例，预设点控制使用 CLIENT_DHPTZControlEx
            // 参数顺序：handle, channel, command, param1, param2, param3, stop
            // 预设点编号应该在 param2 位置！
            // SET:  param1=0, param2=presetNo, param3=0, stop=0
            // DEL:  param1=0, param2=presetNo, param3=0, stop=0
            // GOTO: param1=0, param2=presetNo, param3=0, stop=0
            boolean result = false;
            
            log.info("{} 预设点SDK调用参数(官方格式): handle={}, sdkChannel={}, ptzCommand={}, param1=0, param2(presetNo)={}, param3=0, stop=0", 
                    LOG_PREFIX, loginHandle.longValue(), sdkChannelNo, ptzCommand, presetNo);
            
            try {
                log.info("{} >>> 开始调用 CLIENT_DHPTZControlEx 预设点控制(官方格式)...", LOG_PREFIX);
                
                // 使用官方示例的参数格式：param1=0, param2=presetNo, param3=0, stop=0
                result = getNetSdk().CLIENT_DHPTZControlEx(
                        loginHandle,
                        sdkChannelNo,
                        ptzCommand,
                        0,            // param1: 0（固定）
                        presetNo,     // param2: 预设点编号（关键：预设点ID在这里！）
                        0,            // param3: 0（固定）
                        0             // dwStop: 0=执行
                );
                
                int lastError = getNetSdk().CLIENT_GetLastError();
                log.info("{} <<< CLIENT_DHPTZControlEx 调用完成: result={}, errorCode={}", 
                        LOG_PREFIX, result, lastError);
                
                // 对于 GOTO 操作，使用官方格式已经足够，不需要扩展命令
                if (ptzCommand == NET_PTZ_POINT_MOVE_CONTROL) {
                    log.info("{} GOTO操作使用官方格式完成", LOG_PREFIX);
                }
            } catch (Throwable t) {
                log.error("{} !!! CLIENT_DHPTZControlEx 调用抛出异常: {}", LOG_PREFIX, t.getMessage(), t);
                throw t;
            }
            
            // 始终获取 SDK 错误码用于调试
            String sdkErrorInfo = ToolKits.getErrorCodePrint();
            int lastError = getNetSdk().CLIENT_GetLastError();
            
            log.info("{} 预设点SDK调用结果: result={}, lastError={}, errorInfo={}", 
                    LOG_PREFIX, result, lastError, sdkErrorInfo);
            
            if (result) {
                log.info("{} 直连IPC预设点控制SDK返回成功: ip={}, channel={}, presetNo={}, action={}", 
                        LOG_PREFIX, ip, channelNo, presetNo, action);
                
                // 对于 SET 操作，设置预设点名称
                // 参照官方Demo：设置位置和设置名称是两个独立操作，需要间隔一定时间
                if ("SET".equalsIgnoreCase(action)) {
                    String actualName = (presetName != null && !presetName.isEmpty()) ? presetName : ("预置点" + presetNo);
                    boolean nameSetSuccess = false;
                    
                    // 延时500ms，等待设备处理完位置设置
                    // 官方Demo中设置位置和设置名称是通过两个独立的HTTP请求完成的
                    try {
                        log.info("{} 等待500ms后设置预设点名称...", LOG_PREFIX);
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // 方法1：使用 CLIENT_DHPTZControlEx2 设置名称（官方Demo的方法）
                    try {
                        log.info("{} 尝试使用SDK设置预设点名称: sdkChannel={}, presetNo={}, name={}", 
                                LOG_PREFIX, sdkChannelNo, presetNo, actualName);
                        
                        Pointer namePointer = ToolKits.GetGBKStringToPointer(actualName);
                        
                        // 参照官方Demo DHNetServiceImpl.UpdateYZDName：
                        // CLIENT_DHPTZControlEx2(handle, nChannelID, NET_PTZ_POINT_SET_CONTROL, 0, yzdId, 0, 0, namePointer)
                        boolean nameResult = getNetSdk().CLIENT_DHPTZControlEx2(
                                loginHandle,
                                sdkChannelNo,           // 通道号（SDK从0开始）
                                NET_PTZ_POINT_SET_CONTROL,
                                0,                      // param1: 0（固定）
                                presetNo,               // param2: 预设点编号
                                0,                      // param3: 0（固定）
                                0,                      // dwStop: 0（执行）
                                namePointer             // param4: 预设点名称（GBK编码）
                        );
                        
                        int nameLastError = getNetSdk().CLIENT_GetLastError();
                        if (nameResult) {
                            log.info("{} ✅ SDK设置预设点名称成功: name={}", LOG_PREFIX, actualName);
                            nameSetSuccess = true;
                        } else {
                            String nameError = ToolKits.getErrorCodePrint();
                            log.warn("{} SDK设置预设点名称失败: name={}, errorCode={}, error={}", 
                                    LOG_PREFIX, actualName, nameLastError, nameError);
                        }
                    } catch (Exception e) {
                        log.warn("{} SDK设置预设点名称异常: {}", LOG_PREFIX, e.getMessage());
                    }
                    
                    // 方法2：如果SDK方法失败，尝试使用配置API设置预设点名称
                    if (!nameSetSuccess) {
                        try {
                            // 再等待200ms
                            Thread.sleep(200);
                            log.info("{} SDK设置名称失败，尝试使用配置API设置预设点名称: presetNo={}, name={}", 
                                    LOG_PREFIX, presetNo, actualName);
                            NvrOperationResult configResult = setPresetNameByConfig(ip, username, password, sdkChannelNo, presetNo, actualName);
                            if (configResult.isSuccess()) {
                                log.info("{} ✅ 配置API设置预设点名称成功: name={}", LOG_PREFIX, actualName);
                            } else {
                                log.warn("{} 配置API设置预设点名称失败: name={}, error={}", 
                                        LOG_PREFIX, actualName, configResult.getMessage());
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                            log.warn("{} 配置API设置预设点名称异常: {}", LOG_PREFIX, e.getMessage());
                        }
                    }
                }
                
                // 对于 GOTO 操作，额外尝试 HTTP CGI 接口
                if ("GOTO".equalsIgnoreCase(action)) {
                    try {
                        log.info("{} SDK命令完成，额外尝试HTTP CGI接口转到预设点...", LOG_PREFIX);
                        boolean cgiResult = gotoPresetByCgi(ip, username, password, sdkChannelNo, presetNo);
                        if (cgiResult) {
                            log.info("{} ✅ HTTP CGI转到预设点成功!", LOG_PREFIX);
                        } else {
                            log.warn("{} HTTP CGI转到预设点失败，但SDK命令已返回成功", LOG_PREFIX);
                        }
                    } catch (Exception e) {
                        log.warn("{} HTTP CGI转到预设点异常: {}", LOG_PREFIX, e.getMessage());
                    }
                    
                    // 获取预设点列表进行调试
                    try {
                        log.debug("{} 尝试通过配置API获取预设点列表进行调试...", LOG_PREFIX);
                        NvrOperationResult configResult = getPresetListByConfig(ip, username, password, channelNo);
                        if (configResult.isSuccess()) {
                            log.info("{} 配置API获取预设点列表成功: {}", LOG_PREFIX, configResult.getMessage());
                        } else {
                            log.info("{} 配置API获取预设点列表失败: {}", LOG_PREFIX, configResult.getMessage());
                        }
                    } catch (Exception e) {
                        log.debug("{} 配置API调试失败: {}", LOG_PREFIX, e.getMessage());
                    }
                }
                
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success("预设点控制成功");
            } else {
                log.warn("{} 直连IPC预设点控制失败: ip={}, channel={}, presetNo={}, action={}, error={}", 
                        LOG_PREFIX, ip, channelNo, presetNo, action, sdkErrorInfo);
                clearIpcSession(ip);
                return NvrOperationResult.failure("预设点控制失败: " + sdkErrorInfo);
            }
            
        } catch (Exception e) {
            log.error("{} 直连IPC预设点控制异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            clearIpcSession(ip);
            return NvrOperationResult.failure("预设点控制异常: " + e.getMessage());
        }
    }

    /**
     * 转换预设点操作为 SDK 命令码
     */
    private int convertPresetAction(String action) {
        if (action == null) {
            return -1;
        }
        switch (action.toUpperCase()) {
            case "GOTO": case "MOVE": return NET_PTZ_POINT_MOVE_CONTROL;
            case "SET": case "ADD": return NET_PTZ_POINT_SET_CONTROL;
            case "CLEAR": case "DELETE": case "DEL": return NET_PTZ_POINT_DEL_CONTROL;
            default: return -1;
        }
    }
    
    /**
     * 通过配置 API 设置预设点（某些设备不支持 PTZ 控制命令设置预设点，需要用配置 API）
     * 这是备选方案，当 PTZ 控制命令不工作时使用
     */
    public NvrOperationResult setPresetByConfig(String ip, String username, String password, int channelNo, int presetNo, String presetName) {
        try {
            log.info("{} 尝试通过配置API设置预设点: ip={}, channel={}, presetNo={}, name={}", 
                    LOG_PREFIX, ip, channelNo, presetNo, presetName);
            
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            // 直连 IPC 时直接使用原始通道号，不做 -1 转换
            // 配置 API 的 channel 参数含义可能与 PTZ 控制不同
            int configChannel = channelNo;
            
            // 构建预设点配置 JSON
            // 大华设备的预设点配置格式为 JSON
            String name = (presetName != null && !presetName.isEmpty()) ? presetName : ("Preset" + presetNo);
            String jsonConfig = String.format(
                "{ \"PtzPreset\" : [ { \"Index\" : %d, \"Name\" : \"%s\", \"Enable\" : true } ] }",
                presetNo, name
            );
            
            log.info("{} 设置预设点配置JSON: channel={}, config={}", LOG_PREFIX, configChannel, jsonConfig);
            
            byte[] inBuffer = jsonConfig.getBytes("UTF-8");
            IntByReference error = new IntByReference(0);
            IntByReference restart = new IntByReference(0);
            
            boolean result = getNetSdk().CLIENT_SetNewDevConfig(
                    loginHandle,
                    CFG_CMD_PTZ_PRESET,
                    configChannel,
                    inBuffer,
                    inBuffer.length,
                    error,
                    restart,
                    5000
            );
            
            if (result) {
                log.info("{} 配置API设置预设点成功: channel={}, presetNo={}", LOG_PREFIX, configChannel, presetNo);
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success("预设点设置成功(配置API)");
            } else {
                String sdkErrorInfo = ToolKits.getErrorCodePrint();
                int lastError = getNetSdk().CLIENT_GetLastError();
                log.warn("{} 配置API设置预设点失败: channel={}, presetNo={}, error={}, lastError={}", 
                        LOG_PREFIX, configChannel, presetNo, sdkErrorInfo, lastError);
                return NvrOperationResult.failure("配置API设置预设点失败: " + sdkErrorInfo);
            }
        } catch (Exception e) {
            log.error("{} 配置API设置预设点异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrOperationResult.failure("配置API设置预设点异常: " + e.getMessage());
        }
    }

    /**
     * 通过配置 API 设置预设点（指定SDK通道号，不做转换）
     * 用于遍历尝试不同通道号
     */
    private NvrOperationResult setPresetByConfigWithChannel(String ip, String username, String password, int sdkChannel, int presetNo, String presetName) {
        try {
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            String name = (presetName != null && !presetName.isEmpty()) ? presetName : ("Preset" + presetNo);
            String jsonConfig = String.format(
                "{ \"PtzPreset\" : [ { \"Index\" : %d, \"Name\" : \"%s\", \"Enable\" : true } ] }",
                presetNo, name
            );
            
            log.debug("{} 配置API设置预设点: channel={}, config={}", LOG_PREFIX, sdkChannel, jsonConfig);
            
            byte[] inBuffer = jsonConfig.getBytes("UTF-8");
            IntByReference error = new IntByReference(0);
            IntByReference restart = new IntByReference(0);
            
            boolean result = getNetSdk().CLIENT_SetNewDevConfig(
                    loginHandle,
                    CFG_CMD_PTZ_PRESET,
                    sdkChannel,
                    inBuffer,
                    inBuffer.length,
                    error,
                    restart,
                    5000
            );
            
            if (result) {
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success("预设点设置成功(配置API, channel=" + sdkChannel + ")");
            } else {
                return NvrOperationResult.failure("配置API设置预设点失败: " + ToolKits.getErrorCodePrint());
            }
        } catch (Exception e) {
            return NvrOperationResult.failure("配置API设置预设点异常: " + e.getMessage());
        }
    }

    /**
     * 通过配置 API 设置预设点名称（不覆盖位置）
     * 
     * 注意：根据官方Demo，配置API通道号使用 -1 表示所有通道
     */
    private NvrOperationResult setPresetNameByConfig(String ip, String username, String password, int sdkChannel, int presetNo, String presetName) {
        try {
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            // 构建预设点配置 JSON - 只设置名称
            // 注意：Index 是预设点编号（1-based），与 presetNo 一致
            String jsonConfig = String.format(
                "{ \"PtzPreset\" : [ { \"Index\" : %d, \"Name\" : \"%s\", \"Enable\" : true } ] }",
                presetNo, presetName
            );
            
            log.info("{} 配置API设置预设点名称: sdkChannel={}, presetNo={}, name={}, json={}", 
                    LOG_PREFIX, sdkChannel, presetNo, presetName, jsonConfig);
            
            byte[] inBuffer = jsonConfig.getBytes("UTF-8");
            IntByReference error = new IntByReference(0);
            IntByReference restart = new IntByReference(0);
            
            // 先尝试使用 -1（所有通道），这是官方Demo的用法
            boolean result = getNetSdk().CLIENT_SetNewDevConfig(
                    loginHandle,
                    CFG_CMD_PTZ_PRESET,
                    -1,  // 使用 -1 表示所有通道，符合官方Demo
                    inBuffer,
                    inBuffer.length,
                    error,
                    restart,
                    5000
            );
            
            // 如果 -1 失败，尝试使用具体的SDK通道号
            if (!result) {
                log.info("{} 配置API(-1)失败，尝试使用sdkChannel={}", LOG_PREFIX, sdkChannel);
                result = getNetSdk().CLIENT_SetNewDevConfig(
                        loginHandle,
                        CFG_CMD_PTZ_PRESET,
                        sdkChannel,
                        inBuffer,
                        inBuffer.length,
                        error,
                        restart,
                        5000
                );
            }
            
            if (result) {
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                log.info("{} ✅ 配置API设置预设点名称成功: presetNo={}, name={}", LOG_PREFIX, presetNo, presetName);
                return NvrOperationResult.success("预设点名称设置成功");
            } else {
                return NvrOperationResult.failure("配置API设置名称失败: " + ToolKits.getErrorCodePrint());
            }
        } catch (Exception e) {
            return NvrOperationResult.failure("配置API设置名称异常: " + e.getMessage());
        }
    }

    /**
     * 删除预设点（直连 IPC）
     * 
     * @param ip        IPC 设备 IP
     * @param username  用户名
     * @param password  密码
     * @param channelNo 通道号
     * @param presetNo  预设点编号（1-255）
     * @return 操作结果
     */
    public NvrOperationResult deletePresetDirect(String ip, String username, String password, int channelNo, int presetNo) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        try {
            log.info("{} 删除预设点: ip={}, channel={}, presetNo={}", LOG_PREFIX, ip, channelNo, presetNo);
            
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            // 使用 NET_PTZ_POINT_DEL_CONTROL 删除预设点
            log.info("{} 删除预设点SDK调用: handle={}, sdkChannel={}, presetNo={}", 
                    LOG_PREFIX, loginHandle.longValue(), sdkChannelNo, presetNo);
            
            boolean result = getNetSdk().CLIENT_DHPTZControlEx(
                    loginHandle,
                    sdkChannelNo,
                    NET_PTZ_POINT_DEL_CONTROL,
                    0,           // param1: 0
                    presetNo,    // param2: 预设点编号
                    0,           // param3: 0
                    0            // dwStop: 0
            );
            
            String errorInfo = ToolKits.getErrorCodePrint();
            
            if (result) {
                log.info("{} 删除预设点成功: ip={}, presetNo={}", LOG_PREFIX, ip, presetNo);
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success("删除预设点成功");
            } else {
                log.warn("{} 删除预设点失败: ip={}, presetNo={}, error={}", LOG_PREFIX, ip, presetNo, errorInfo);
                return NvrOperationResult.failure("删除预设点失败: " + errorInfo);
            }
            
        } catch (Exception e) {
            log.error("{} 删除预设点异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrOperationResult.failure("删除预设点异常: " + e.getMessage());
        }
    }

    /**
     * 通过 PTZ 扩展命令同步巡航组到设备（推荐方式）
     * 
     * 使用以下命令序列：
     * 1. NET_EXTPTZ_CLOSELOOP (0x26) - 清除巡航组
     * 2. NET_EXTPTZ_ADDTOLOOP (0x24) - 逐个添加预设点到巡航组
     * 
     * @param ip         IPC 设备 IP
     * @param username   用户名
     * @param password   密码
     * @param channelNo  通道号
     * @param tourNo     巡航组编号（1-8）
     * @param tourName   巡航组名称（用于日志）
     * @param presetNos  预设点编号列表
     * @param dwellTimes 每个预设点的停留时间列表（秒）- 暂不支持，由设备默认
     * @param speeds     每个预设点的转动速度列表（1-10）- 暂不支持，由设备默认
     * @return 操作结果
     */
    public NvrOperationResult syncTourByConfig(String ip, String username, String password, int channelNo,
                                                int tourNo, String tourName, List<Integer> presetNos,
                                                List<Integer> dwellTimes, List<Integer> speeds) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        if (presetNos == null || presetNos.isEmpty()) {
            return NvrOperationResult.failure("预设点列表不能为空");
        }
        
        try {
            log.info("{} 同步巡航组到设备: ip={}, channel={}, tourNo={}, name={}, presetCount={}", 
                    LOG_PREFIX, ip, channelNo, tourNo, tourName, presetNos.size());
            
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            // 步骤1：清除巡航组
            log.info("{} 步骤1: 清除巡航组 tourNo={}", LOG_PREFIX, tourNo);
            boolean clearResult = getNetSdk().CLIENT_DHPTZControlEx(
                    loginHandle,
                    sdkChannelNo,
                    NET_EXTPTZ_CLOSELOOP,  // 0x26 - 清除巡航
                    tourNo,                 // param1: 巡航组编号
                    0,                      // param2: 0
                    0,                      // param3: 0
                    0                       // dwStop: 0
            );
            
            if (!clearResult) {
                String errorInfo = ToolKits.getErrorCodePrint();
                log.warn("{} 清除巡航组失败: tourNo={}, error={}", LOG_PREFIX, tourNo, errorInfo);
                // 继续尝试添加预设点，即使清除失败
            } else {
                log.info("{} 清除巡航组成功: tourNo={}", LOG_PREFIX, tourNo);
            }
            
            // 等待设备处理
            Thread.sleep(200);
            
            // 步骤2：逐个添加预设点到巡航组
            int successCount = 0;
            for (int i = 0; i < presetNos.size(); i++) {
                int presetNo = presetNos.get(i);
                
                log.info("{} 步骤2.{}: 添加预设点到巡航组 tourNo={}, presetNo={}", 
                        LOG_PREFIX, i + 1, tourNo, presetNo);
                
                boolean addResult = getNetSdk().CLIENT_DHPTZControlEx(
                        loginHandle,
                        sdkChannelNo,
                        NET_EXTPTZ_ADDTOLOOP,  // 0x24 - 加入预设点到巡航
                        tourNo,                 // param1: 巡航组编号
                        presetNo,               // param2: 预设点编号
                        0,                      // param3: 0
                        0                       // dwStop: 0
                );
                
                if (addResult) {
                    successCount++;
                    log.info("{} 添加预设点成功: tourNo={}, presetNo={}", LOG_PREFIX, tourNo, presetNo);
                } else {
                    String errorInfo = ToolKits.getErrorCodePrint();
                    log.warn("{} 添加预设点失败: tourNo={}, presetNo={}, error={}", 
                            LOG_PREFIX, tourNo, presetNo, errorInfo);
                }
                
                // 每个命令之间稍微延迟
                Thread.sleep(100);
            }
            
            ipcLoginTimeCache.put(ip, System.currentTimeMillis());
            
            if (successCount == presetNos.size()) {
                log.info("{} ✅ 巡航组同步成功: tourNo={}, presetCount={}", LOG_PREFIX, tourNo, successCount);
                return NvrOperationResult.success("巡航组同步成功，共添加 " + successCount + " 个预设点");
            } else if (successCount > 0) {
                log.warn("{} ⚠️ 巡航组部分同步: tourNo={}, success={}/{}", 
                        LOG_PREFIX, tourNo, successCount, presetNos.size());
                return NvrOperationResult.success("巡航组部分同步，成功 " + successCount + "/" + presetNos.size() + " 个预设点");
            } else {
                log.error("{} ❌ 巡航组同步失败: tourNo={}", LOG_PREFIX, tourNo);
                return NvrOperationResult.failure("巡航组同步失败，所有预设点添加失败");
            }
            
        } catch (Exception e) {
            log.error("{} 巡航组同步异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrOperationResult.failure("巡航组同步异常: " + e.getMessage());
        }
    }

    /**
     * 通过 HTTP CGI 接口设置预设点
     * 
     * <p>大华设备支持 CGI 接口来控制 PTZ，格式为：</p>
     * <pre>
     * http://ip/cgi-bin/ptz.cgi?action=start&channel=X&code=SetPreset&arg1=0&arg2=N&arg3=0
     * </pre>
     * 
     * @param ip        设备 IP
     * @param username  用户名
     * @param password  密码
     * @param sdkChannel SDK通道号（0-based）
     * @param presetNo  预设点编号（1-based）
     * @return true=成功, false=失败
     */
    private boolean setPresetByCgi(String ip, String username, String password, int sdkChannel, int presetNo) {
        // 大华 CGI 接口格式
        // action=start 表示开始执行
        // channel=X 通道号（0-based）
        // code=SetPreset 设置预设点
        // arg1=0 固定为0
        // arg2=N 预设点编号（1-based）
        // arg3=0 固定为0
        String cgiUrl = String.format(
            "http://%s/cgi-bin/ptz.cgi?action=start&channel=%d&code=SetPreset&arg1=0&arg2=%d&arg3=0",
            ip, sdkChannel, presetNo
        );
        
        return executeHttpCgiWithDigest(cgiUrl, ip, username, password, "SetPreset");
    }
    
    /**
     * 通过 HTTP CGI 接口转到预设点
     * 
     * @param ip        设备 IP
     * @param username  用户名
     * @param password  密码
     * @param sdkChannel SDK通道号（0-based）
     * @param presetNo  预设点编号（1-based）
     * @return true=成功, false=失败
     */
    private boolean gotoPresetByCgi(String ip, String username, String password, int sdkChannel, int presetNo) {
        // code=GotoPreset 转到预设点
        String cgiUrl = String.format(
            "http://%s/cgi-bin/ptz.cgi?action=start&channel=%d&code=GotoPreset&arg1=0&arg2=%d&arg3=0",
            ip, sdkChannel, presetNo
        );
        
        return executeHttpCgiWithDigest(cgiUrl, ip, username, password, "GotoPreset");
    }
    
    /**
     * 执行 HTTP CGI 请求（支持 Digest 认证）
     * 
     * <p>大华设备使用 Digest 认证，需要两次请求：</p>
     * <ol>
     *     <li>第一次请求获取 WWW-Authenticate 头中的 realm, nonce 等参数</li>
     *     <li>第二次请求带上计算后的 Authorization 头</li>
     * </ol>
     */
    private boolean executeHttpCgiWithDigest(String cgiUrl, String ip, String username, String password, String operation) {
        HttpURLConnection conn = null;
        try {
            log.info("{} HTTP CGI {}: url={}", LOG_PREFIX, operation, cgiUrl);
            
            URL url = new URL(cgiUrl);
            
            // 第一次请求，获取 Digest 认证参数
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 401) {
                // 获取 WWW-Authenticate 头
                String wwwAuth = conn.getHeaderField("WWW-Authenticate");
                log.debug("{} HTTP CGI Digest 认证头: {}", LOG_PREFIX, wwwAuth);
                
                if (wwwAuth != null && wwwAuth.startsWith("Digest")) {
                    // 解析 Digest 参数
                    String realm = extractDigestParam(wwwAuth, "realm");
                    String nonce = extractDigestParam(wwwAuth, "nonce");
                    String qop = extractDigestParam(wwwAuth, "qop");
                    
                    // 计算 Digest 响应
                    String uri = url.getPath() + (url.getQuery() != null ? "?" + url.getQuery() : "");
                    String nc = "00000001";
                    String cnonce = Long.toHexString(System.currentTimeMillis());
                    
                    String ha1 = md5(username + ":" + realm + ":" + password);
                    String ha2 = md5("GET:" + uri);
                    String response;
                    
                    if (qop != null && !qop.isEmpty()) {
                        response = md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);
                    } else {
                        response = md5(ha1 + ":" + nonce + ":" + ha2);
                    }
                    
                    // 构建 Authorization 头
                    StringBuilder authHeader = new StringBuilder();
                    authHeader.append("Digest username=\"").append(username).append("\"");
                    authHeader.append(", realm=\"").append(realm).append("\"");
                    authHeader.append(", nonce=\"").append(nonce).append("\"");
                    authHeader.append(", uri=\"").append(uri).append("\"");
                    if (qop != null && !qop.isEmpty()) {
                        authHeader.append(", qop=").append(qop);
                        authHeader.append(", nc=").append(nc);
                        authHeader.append(", cnonce=\"").append(cnonce).append("\"");
                    }
                    authHeader.append(", response=\"").append(response).append("\"");
                    
                    conn.disconnect();
                    
                    // 第二次请求，带上认证信息
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("Authorization", authHeader.toString());
                    
                    responseCode = conn.getResponseCode();
                }
            }
            
            if (responseCode == 200) {
                // 读取响应内容
                StringBuilder respBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        respBuilder.append(line);
                    }
                }
                
                String responseBody = respBuilder.toString();
                log.info("{} HTTP CGI 响应: code={}, body={}", LOG_PREFIX, responseCode, responseBody);
                
                // 大华设备成功时返回 "OK" 或空响应
                return responseBody.isEmpty() || responseBody.contains("OK") || responseBody.contains("ok");
            } else {
                log.warn("{} HTTP CGI 失败: responseCode={}", LOG_PREFIX, responseCode);
                return false;
            }
            
        } catch (Exception e) {
            log.warn("{} HTTP CGI 异常: {}", LOG_PREFIX, e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    /**
     * 从 WWW-Authenticate 头中提取 Digest 参数
     */
    private String extractDigestParam(String wwwAuth, String param) {
        String search = param + "=\"";
        int start = wwwAuth.indexOf(search);
        if (start < 0) {
            // 尝试不带引号的格式
            search = param + "=";
            start = wwwAuth.indexOf(search);
            if (start < 0) {
                return null;
            }
            start += search.length();
            int end = wwwAuth.indexOf(",", start);
            if (end < 0) {
                end = wwwAuth.length();
            }
            return wwwAuth.substring(start, end).trim();
        }
        start += search.length();
        int end = wwwAuth.indexOf("\"", start);
        if (end < 0) {
            return null;
        }
        return wwwAuth.substring(start, end);
    }
    
    /**
     * 计算 MD5 哈希
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 计算失败", e);
        }
    }

    /**
     * 通过配置 API 获取设备的预设点列表
     * 用于测试设备是否支持配置方式的预设点操作
     */
    public NvrOperationResult getPresetListByConfig(String ip, String username, String password, int channelNo) {
        try {
            log.info("{} 尝试通过配置API获取预设点列表: ip={}, channel={}", LOG_PREFIX, ip, channelNo);
            
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            // 使用配置 API 获取预设点列表
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            byte[] outBuffer = new byte[512 * 1024]; // 512KB buffer (预设点配置可能很大)
            IntByReference error = new IntByReference(0);
            
            boolean result = getNetSdk().CLIENT_GetNewDevConfig(
                    loginHandle,
                    CFG_CMD_PTZ_PRESET,
                    sdkChannelNo,
                    outBuffer,
                    outBuffer.length,
                    error,
                    5000,
                    null
            );
            
            if (result) {
                String jsonConfig = new String(outBuffer).trim();
                // 去除末尾的 null 字符
                int nullIndex = jsonConfig.indexOf('\0');
                if (nullIndex > 0) {
                    jsonConfig = jsonConfig.substring(0, nullIndex);
                }
                log.info("{} 获取预设点配置成功: channel={}, config={}", LOG_PREFIX, sdkChannelNo, jsonConfig);
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success(jsonConfig);
            } else {
                String sdkErrorInfo = ToolKits.getErrorCodePrint();
                int lastError = getNetSdk().CLIENT_GetLastError();
                log.warn("{} 获取预设点配置失败: channel={}, error={}, lastError={}", 
                        LOG_PREFIX, sdkChannelNo, sdkErrorInfo, lastError);
                return NvrOperationResult.failure("获取预设点配置失败: " + sdkErrorInfo);
            }
        } catch (Exception e) {
            log.error("{} 获取预设点配置异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrOperationResult.failure("获取预设点配置异常: " + e.getMessage());
        }
    }

    // ==================== 录像回放 ====================

    public NvrOperationResult startPlayback(long loginHandle, int channelNo, String startTime, String endTime) {
        if (loginHandle <= 0) {
            return NvrOperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 开始录像回放: handle={}, channel={}, start={}, end={}", 
                    LOG_PREFIX, loginHandle, channelNo, startTime, endTime);
            log.warn("{} {}", LOG_PREFIX, UNSUPPORTED_MSG);
            return NvrOperationResult.failure(UNSUPPORTED_MSG);
            
        } catch (Exception e) {
            log.error("{} 开始录像回放异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return NvrOperationResult.failure("开始录像回放异常: " + e.getMessage());
        }
    }

    public NvrOperationResult stopPlayback(long playbackHandle) {
        try {
            log.info("{} 停止录像回放: playbackHandle={}", LOG_PREFIX, playbackHandle);
            log.warn("{} {}", LOG_PREFIX, UNSUPPORTED_MSG);
            return NvrOperationResult.failure(UNSUPPORTED_MSG);
            
        } catch (Exception e) {
            log.error("{} 停止录像回放异常: error={}", LOG_PREFIX, e.getMessage(), e);
            return NvrOperationResult.failure("停止录像回放异常: " + e.getMessage());
        }
    }

    // ==================== 录像文件查询 ====================

    /**
     * 查询录像文件列表
     * 
     * <p>通过大华 SDK 的 CLIENT_QueryRecordFile 接口查询 NVR 中指定通道的录像文件。</p>
     * <p>NVR 通常按小时分割录像文件，返回的是录像文件列表而非时间段。</p>
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（从1开始，SDK内部会转为从0开始）
     * @param startTime   开始时间，格式: yyyy-MM-dd HH:mm:ss
     * @param endTime     结束时间，格式: yyyy-MM-dd HH:mm:ss
     * @param recordType  录像类型（可为null，默认0=所有录像）
     * @return 操作结果，成功时 data 包含录像文件列表
     */
    public NvrOperationResult queryRecordFiles(long loginHandle, int channelNo, 
            String startTime, String endTime, Integer recordType) {
        if (loginHandle <= 0) {
            return NvrOperationResult.failure("无效的登录句柄");
        }
        
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        try {
            log.info("{} 查询录像文件: handle={}, channel={}, start={}, end={}, recordType={}", 
                    LOG_PREFIX, loginHandle, channelNo, startTime, endTime, recordType);
            
            // SDK 通道号从 0 开始，业务层从 1 开始
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            // 解析时间
            NET_TIME stTimeStart = new NET_TIME();
            NET_TIME stTimeEnd = new NET_TIME();
            parseDateTime(startTime, stTimeStart);
            parseDateTime(endTime, stTimeEnd);
            
            // 录像类型：0=所有录像, 1=外部报警, 2=动态监测报警, 3=所有报警, 4=卡号查询, 5=组合条件查询
            int nRecordFileType = recordType != null ? recordType : 0;
            
            // 创建录像文件信息数组（最大2000个文件）
            // 重要：必须使用 toArray() 方法创建连续内存的结构体数组
            // 参考大华官方示例 DownLoadRecord.java 第71行的写法
            int maxFileCount = 2000;
            NET_RECORDFILE_INFO[] stFileInfo = (NET_RECORDFILE_INFO[]) new NET_RECORDFILE_INFO().toArray(maxFileCount);
            
            log.info("{} [toArray版本-v2] 查询录像文件参数: channel={}, arrayLen={}, structSize={}, bufferSize={}", 
                    LOG_PREFIX, channelNo, stFileInfo.length, stFileInfo[0].size(), 
                    stFileInfo.length * stFileInfo[0].size());
            
            // 查询结果数量
            IntByReference nFindCount = new IntByReference(0);
            
            // 调用 SDK 查询录像文件（参考官方示例 DownLoadRecordModule.java）
            boolean bRet = getNetSdk().CLIENT_QueryRecordFile(
                    new LLong(loginHandle),
                    sdkChannelNo,
                    nRecordFileType,
                    stTimeStart,
                    stTimeEnd,
                    null,  // 查询条件扩展参数
                    stFileInfo,  // 直接传递数组，与官方示例一致
                    stFileInfo.length * stFileInfo[0].size(),
                    nFindCount,
                    TIMEOUT_MS,
                    false  // 是否按时间倒序
            );
            
            if (!bRet) {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.warn("{} 查询录像文件失败: channel={}, error={}", LOG_PREFIX, channelNo, errorMsg);
                return NvrOperationResult.failure("查询录像文件失败: " + errorMsg);
            }
            
            int fileCount = nFindCount.getValue();
            log.info("{} 查询录像文件成功: channel={}, 文件数量={}", LOG_PREFIX, channelNo, fileCount);
            
            // 转换结果
            List<Map<String, Object>> recordFiles = new ArrayList<>();
            for (int i = 0; i < fileCount; i++) {
                NET_RECORDFILE_INFO fileInfo = stFileInfo[i];
                Map<String, Object> record = new HashMap<>();
                
                // 通道号（转回业务层从1开始）
                record.put("channelNo", fileInfo.ch + 1);
                
                // 文件名
                String fileName = new String(fileInfo.filename).trim();
                record.put("fileName", fileName);
                
                // 文件大小（字节）
                record.put("fileSize", fileInfo.size);
                
                // 开始时间
                record.put("startTime", formatNetTime(fileInfo.starttime));
                
                // 结束时间
                record.put("endTime", formatNetTime(fileInfo.endtime));
                
                // 计算时长（秒）
                long duration = calculateDuration(fileInfo.starttime, fileInfo.endtime);
                record.put("duration", duration);
                
                // 录像类型
                record.put("recordType", fileInfo.nRecordFileType);
                
                // 帧数
                record.put("frameNum", fileInfo.framenum);
                
                recordFiles.add(record);
            }
            
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("files", recordFiles);
            resultData.put("totalCount", fileCount);
            resultData.put("channelNo", channelNo);
            resultData.put("startTime", startTime);
            resultData.put("endTime", endTime);
            
            return NvrOperationResult.success("查询成功", resultData);
            
        } catch (Exception e) {
            log.error("{} 查询录像文件异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return NvrOperationResult.failure("查询录像文件异常: " + e.getMessage());
        }
    }
    
    /**
     * 解析日期时间字符串到 NET_TIME
     * @param dateTimeStr 日期时间字符串，格式: yyyy-MM-dd HH:mm:ss
     * @param netTime SDK 时间结构
     */
    private void parseDateTime(String dateTimeStr, NET_TIME netTime) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return;
        }
        try {
            String[] parts = dateTimeStr.split(" ");
            String[] dateParts = parts[0].split("-");
            String[] timeParts = parts.length > 1 ? parts[1].split(":") : new String[]{"0", "0", "0"};
            
            netTime.dwYear = Integer.parseInt(dateParts[0]);
            netTime.dwMonth = Integer.parseInt(dateParts[1]);
            netTime.dwDay = Integer.parseInt(dateParts[2]);
            netTime.dwHour = Integer.parseInt(timeParts[0]);
            netTime.dwMinute = Integer.parseInt(timeParts[1]);
            netTime.dwSecond = Integer.parseInt(timeParts[2]);
        } catch (Exception e) {
            log.warn("{} 解析时间失败: {} - {}", LOG_PREFIX, dateTimeStr, e.getMessage());
        }
    }
    
    /**
     * 格式化 NET_TIME 为字符串
     * @param netTime SDK 时间结构
     * @return 格式化的日期时间字符串
     */
    private String formatNetTime(NET_TIME netTime) {
        if (netTime == null || netTime.dwYear == 0) {
            return null;
        }
        return String.format("%04d-%02d-%02d %02d:%02d:%02d",
                netTime.dwYear, netTime.dwMonth, netTime.dwDay,
                netTime.dwHour, netTime.dwMinute, netTime.dwSecond);
    }
    
    /**
     * 计算时长（秒）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时长（秒）
     */
    private long calculateDuration(NET_TIME startTime, NET_TIME endTime) {
        try {
            java.time.LocalDateTime start = java.time.LocalDateTime.of(
                    startTime.dwYear, startTime.dwMonth, startTime.dwDay,
                    startTime.dwHour, startTime.dwMinute, startTime.dwSecond);
            java.time.LocalDateTime end = java.time.LocalDateTime.of(
                    endTime.dwYear, endTime.dwMonth, endTime.dwDay,
                    endTime.dwHour, endTime.dwMinute, endTime.dwSecond);
            return java.time.Duration.between(start, end).getSeconds();
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== 通道信息查询 ====================

    /**
     * 查询 NVR 通道信息
     * 
     * <p>通过大华 SDK 查询 NVR 下所有通道的状态信息，包括：</p>
     * <ul>
     *     <li>通道号</li>
     *     <li>通道名称</li>
     *     <li>在线状态</li>
     *     <li>是否正在录像</li>
     *     <li>设备类型（IPC/球机等）</li>
     * </ul>
     * 
     * @param loginHandle 登录句柄
     * @return 操作结果，成功时 data 包含通道列表
     */
    public NvrOperationResult queryChannels(long loginHandle) {
        if (loginHandle <= 0) {
            return NvrOperationResult.failure("无效的登录句柄");
        }
        
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        try {
            log.info("{} 查询通道信息: handle={}", LOG_PREFIX, loginHandle);
            
            List<Map<String, Object>> channelList = new ArrayList<>();
            
            // 1. 首先获取设备信息以确定通道数量
            //    登录时返回的 NET_DEVICEINFO_Ex 包含 byChanNum
            //    这里我们通过 CLIENT_QueryDevState 获取更详细的通道状态
            
            // 2. 查询远程通道信息（NVR 下的 IPC）
            //    使用 NET_DEVSTATE_CHANNELINFO 获取通道信息
            int maxChannels = 64; // 最大支持 64 通道
            
            // 获取通道状态
            byte[] channelStatus = queryChannelStatus(loginHandle, maxChannels);
            
            // 获取通道名称
            String[] channelNames = queryChannelNames(loginHandle, maxChannels);
            
            // 获取录像状态
            byte[] recordStatus = queryRecordStatus(loginHandle, maxChannels);
            
            // ✅ 获取支持 PTZ 的通道列表
            Set<Integer> ptzSet = queryPtzSet(loginHandle);
            log.info("{} 支持 PTZ 的通道列表: {}", LOG_PREFIX, ptzSet);
            
            // 构建通道列表
            for (int i = 0; i < maxChannels; i++) {
                // 通道状态: 0=正常在线, 1=视频丢失, 2=无此通道
                // 如果是无此通道，跳过
                if (channelStatus != null && i < channelStatus.length) {
                    int status = channelStatus[i] & 0xFF;
                    if (status == 2) {
                        continue; // 无此通道
                    }
                    
                    String channelName = channelNames != null && i < channelNames.length ? 
                            channelNames[i] : "通道" + (i + 1);
                    
                    Map<String, Object> channel = new HashMap<>();
                    int channelNo = i + 1; // 通道号从 1 开始
                    channel.put("channelNo", channelNo);
                    channel.put("channelName", channelName);
                    channel.put("online", status == 0); // 0 表示正常在线
                    channel.put("recording", recordStatus != null && i < recordStatus.length && 
                            (recordStatus[i] & 0xFF) == 1);
                    channel.put("videoLoss", status == 1);
                    
                    // ✅ 检测 PTZ 能力：优先使用 ptzSet，其次使用名称推断
                    boolean ptzSupport = ptzSet.contains(channelNo) || 
                            detectPtzSupport(loginHandle, channelNo, channelName);
                    
                    // 设备类型和能力
                    Map<String, Object> capabilities = new HashMap<>();
                    capabilities.put("ptzSupport", ptzSupport);
                    capabilities.put("audioSupport", true); // 默认支持音频
                    channel.put("capabilities", capabilities);
                    
                    if (ptzSupport) {
                        log.info("{} 通道 {} ({}) 支持云台 [ptzSet={}, nameMatch={}]", 
                                LOG_PREFIX, channelNo, channelName, 
                                ptzSet.contains(channelNo), detectPtzSupport(loginHandle, channelNo, channelName));
                    }
                    
                    channelList.add(channel);
                    
                    log.debug("{} 通道 {}: online={}, recording={}, name={}", 
                            LOG_PREFIX, i + 1, status == 0, 
                            recordStatus != null && i < recordStatus.length && (recordStatus[i] & 0xFF) == 1,
                            channelNames != null && i < channelNames.length ? channelNames[i] : "N/A");
                }
            }
            
            // 如果没有获取到任何通道状态，使用备用方法
            if (channelList.isEmpty()) {
                log.warn("{} 未能通过 SDK 获取通道状态，使用备用方法", LOG_PREFIX);
                channelList = queryChannelsFallback(loginHandle);
            }
            
            log.info("{} ✅ 查询通道完成: handle={}, count={}", LOG_PREFIX, loginHandle, channelList.size());
            
            // 缓存通道信息
            channelCache.put(loginHandle, channelList);
            
            Map<String, Object> result = new HashMap<>();
            result.put("channels", channelList);
            result.put("totalCount", channelList.size());
            
            return NvrOperationResult.success("查询通道成功", result);
            
        } catch (Exception e) {
            log.error("{} 查询通道信息异常: error={}", LOG_PREFIX, e.getMessage(), e);
            return NvrOperationResult.failure("查询通道信息异常: " + e.getMessage());
        }
    }
    
    /**
     * 查询通道视频信号状态
     * 
     * @param loginHandle 登录句柄
     * @param maxChannels 最大通道数
     * @return 通道状态数组，0=正常, 1=视频丢失, 2=无此通道
     */
    private byte[] queryChannelStatus(long loginHandle, int maxChannels) {
        try {
            // 使用 CLIENT_QueryDevState 查询视频信号状态
            // NET_DEVSTATE_VIDEO_SIGNAL (2) - 查询视频信号状态
            com.sun.jna.Memory statusBuf = new com.sun.jna.Memory(maxChannels);
            com.sun.jna.ptr.IntByReference returnLen = new com.sun.jna.ptr.IntByReference(0);
            
            boolean result = getNetSdk().CLIENT_QueryDevState(
                    new LLong(loginHandle),
                    2, // NET_DEVSTATE_VIDEO_SIGNAL
                    statusBuf,
                    maxChannels,
                    returnLen,
                    TIMEOUT_MS
            );
            
            if (result && returnLen.getValue() > 0) {
                log.debug("{} 查询视频信号状态成功: returnLen={}", LOG_PREFIX, returnLen.getValue());
                byte[] status = new byte[returnLen.getValue()];
                statusBuf.read(0, status, 0, returnLen.getValue());
                return status;
            } else {
                log.warn("{} 查询视频信号状态失败: error={}", LOG_PREFIX, ToolKits.getErrorCodePrint());
                return null;
            }
        } catch (Exception e) {
            log.warn("{} 查询视频信号状态异常: {}", LOG_PREFIX, e.getMessage());
            return null;
        }
    }
    
    /**
     * 查询通道名称
     * 
     * <p>通过 CLIENT_GetNewDevConfig 获取设备配置的通道名称（ChannelTitle）</p>
     * 
     * @param loginHandle 登录句柄
     * @param maxChannels 最大通道数
     * @return 通道名称数组
     */
    private String[] queryChannelNames(long loginHandle, int maxChannels) {
        String[] names = new String[maxChannels];
        
        // 初始化默认名称
        for (int i = 0; i < maxChannels; i++) {
            names[i] = "通道" + (i + 1);
        }
        
        try {
            // 使用 CLIENT_GetNewDevConfig + "ChannelTitle" 获取通道标题（JSON格式）
            byte[] outBuffer = new byte[64 * 1024]; // 64KB buffer
            IntByReference error = new IntByReference(0);
            
            boolean result = getNetSdk().CLIENT_GetNewDevConfig(
                    new LLong(loginHandle),
                    "ChannelTitle",   // 配置命令：获取通道标题
                    -1,               // -1 表示获取所有通道
                    outBuffer,
                    outBuffer.length,
                    error,
                    TIMEOUT_MS,
                    (Pointer) null
            );
            
            if (result) {
                String jsonConfig = new String(outBuffer, java.nio.charset.StandardCharsets.UTF_8).trim();
                // 去除末尾的 null 字符
                int nullIndex = jsonConfig.indexOf('\0');
                if (nullIndex > 0) {
                    jsonConfig = jsonConfig.substring(0, nullIndex);
                }
                
                log.info("{} 获取通道名称配置成功: {}", LOG_PREFIX, 
                        jsonConfig.length() > 500 ? jsonConfig.substring(0, 500) + "..." : jsonConfig);
                
                // 解析 JSON 获取通道名称
                // 格式类似: { "ChannelTitle" : [ { "Name" : "大堂前门" }, { "Name" : "IPC222" }, ... ] }
                parseChannelTitlesFromJson(jsonConfig, names);
            } else {
                String sdkErrorInfo = ToolKits.getErrorCodePrint();
                log.warn("{} 获取通道名称配置失败: error={}, sdkError={}", LOG_PREFIX, error.getValue(), sdkErrorInfo);
                
                // 尝试使用备用方法：逐个通道获取
                queryChannelNamesOneByOne(loginHandle, names, maxChannels);
            }
            
            log.info("{} 查询通道名称完成，部分名称: [{}]", LOG_PREFIX, 
                    names.length > 0 ? String.join(", ", Arrays.copyOf(names, Math.min(6, names.length))) : "无");
            return names;
            
        } catch (Exception e) {
            log.warn("{} 查询通道名称异常: {}", LOG_PREFIX, e.getMessage());
            return names; // 返回默认名称
        }
    }
    
    /**
     * 从 JSON 配置中解析通道标题
     */
    private void parseChannelTitlesFromJson(String jsonConfig, String[] names) {
        try {
            // 简单解析 JSON: { "ChannelTitle" : [ { "Name" : "xxx" }, ... ] }
            // 也可能是: { "table" : { "ChannelTitle" : [ ... ] } }
            
            // 查找 Name 字段
            int idx = 0;
            int searchStart = 0;
            while (idx < names.length) {
                // 查找 "Name" : "xxx" 模式
                int nameKeyIdx = jsonConfig.indexOf("\"Name\"", searchStart);
                if (nameKeyIdx < 0) {
                    break;
                }
                
                // 找到冒号后的值
                int colonIdx = jsonConfig.indexOf(":", nameKeyIdx + 6);
                if (colonIdx < 0) {
                    break;
                }
                
                // 找到引号包围的值
                int quoteStart = jsonConfig.indexOf("\"", colonIdx + 1);
                if (quoteStart < 0) {
                    break;
                }
                
                int quoteEnd = jsonConfig.indexOf("\"", quoteStart + 1);
                if (quoteEnd < 0) {
                    break;
                }
                
                String channelName = jsonConfig.substring(quoteStart + 1, quoteEnd).trim();
                if (!channelName.isEmpty()) {
                    names[idx] = channelName;
                    log.debug("{} 解析到通道 {} 名称: {}", LOG_PREFIX, idx + 1, channelName);
                }
                
                idx++;
                searchStart = quoteEnd + 1;
            }
            
            log.info("{} 从JSON解析到 {} 个通道名称", LOG_PREFIX, idx);
        } catch (Exception e) {
            log.warn("{} 解析通道名称JSON异常: {}", LOG_PREFIX, e.getMessage());
        }
    }
    
    /**
     * 逐个通道获取名称（备用方法）
     */
    private void queryChannelNamesOneByOne(long loginHandle, String[] names, int maxChannels) {
        try {
            byte[] outBuffer = new byte[4096];
            IntByReference error = new IntByReference(0);
            
            for (int channel = 0; channel < maxChannels; channel++) {
                try {
                    boolean result = getNetSdk().CLIENT_GetNewDevConfig(
                            new LLong(loginHandle),
                            "ChannelTitle",
                            channel,   // 具体通道号
                            outBuffer,
                            outBuffer.length,
                            error,
                            3000,
                            (Pointer) null
                    );
                    
                    if (result) {
                        String jsonConfig = new String(outBuffer, java.nio.charset.StandardCharsets.UTF_8).trim();
                        int nullIndex = jsonConfig.indexOf('\0');
                        if (nullIndex > 0) {
                            jsonConfig = jsonConfig.substring(0, nullIndex);
                        }
                        
                        // 解析单个通道名称
                        int nameIdx = jsonConfig.indexOf("\"Name\"");
                        if (nameIdx >= 0) {
                            int colonIdx = jsonConfig.indexOf(":", nameIdx + 6);
                            int quoteStart = jsonConfig.indexOf("\"", colonIdx + 1);
                            int quoteEnd = jsonConfig.indexOf("\"", quoteStart + 1);
                            if (quoteStart >= 0 && quoteEnd > quoteStart) {
                                String name = jsonConfig.substring(quoteStart + 1, quoteEnd).trim();
                                if (!name.isEmpty()) {
                                    names[channel] = name;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略单个通道的错误
                }
            }
        } catch (Exception e) {
            log.warn("{} 逐个获取通道名称异常: {}", LOG_PREFIX, e.getMessage());
        }
    }
    
    /**
     * 查询录像状态
     * 
     * @param loginHandle 登录句柄
     * @param maxChannels 最大通道数
     * @return 录像状态数组，1=正在录像, 0=未录像
     */
    private byte[] queryRecordStatus(long loginHandle, int maxChannels) {
        try {
            // 使用 CLIENT_QueryDevState 查询录像状态
            // NET_DEVSTATE_RECORDING (3) - 查询录像状态
            com.sun.jna.Memory statusBuf = new com.sun.jna.Memory(maxChannels);
            com.sun.jna.ptr.IntByReference returnLen = new com.sun.jna.ptr.IntByReference(0);
            
            boolean result = getNetSdk().CLIENT_QueryDevState(
                    new LLong(loginHandle),
                    3, // NET_DEVSTATE_RECORDING
                    statusBuf,
                    maxChannels,
                    returnLen,
                    TIMEOUT_MS
            );
            
            if (result && returnLen.getValue() > 0) {
                log.debug("{} 查询录像状态成功: returnLen={}", LOG_PREFIX, returnLen.getValue());
                byte[] status = new byte[returnLen.getValue()];
                statusBuf.read(0, status, 0, returnLen.getValue());
                return status;
            } else {
                log.debug("{} 查询录像状态失败: error={}", LOG_PREFIX, ToolKits.getErrorCodePrint());
                return null;
            }
        } catch (Exception e) {
            log.warn("{} 查询录像状态异常: {}", LOG_PREFIX, e.getMessage());
            return null;
        }
    }
    
    /**
     * 查询支持 PTZ 云台控制的通道列表
     * 
     * <p>通过 CLIENT_QueryDevInfo 配合 NET_QUERY_VIDEOCHANNELSINFO 查询设备的 PTZ 能力信息，
     * 返回支持云台控制的通道号集合。</p>
     * 
     * @param loginHandle 登录句柄
     * @return 支持 PTZ 的通道号集合（通道号从 1 开始）
     */
    private java.util.Set<Integer> queryPtzSet(long loginHandle) {
        java.util.Set<Integer> ptzSet = new java.util.HashSet<>();
        
        try {
            log.info("{} 开始查询 PTZ 通道列表: handle={}", LOG_PREFIX, loginHandle);
            
            // 使用 CLIENT_QueryDevInfo 配合 NET_QUERY_VIDEOCHANNELSINFO 获取 PTZ 支持通道
            NetSDKLib.NET_IN_GET_VIDEOCHANNELSINFO inVC = new NetSDKLib.NET_IN_GET_VIDEOCHANNELSINFO();
            inVC.emType = NetSDKLib.NET_VIDEO_CHANNEL_TYPE.NET_VIDEO_CHANNEL_TYPE_INPUT;
            inVC.write();
            
            NetSDKLib.NET_OUT_GET_VIDEOCHANNELSINFO outVC = new NetSDKLib.NET_OUT_GET_VIDEOCHANNELSINFO();
            outVC.write();
            
            boolean qok = getNetSdk().CLIENT_QueryDevInfo(
                    new LLong(loginHandle), 
                    NetSDKLib.NET_QUERY_VIDEOCHANNELSINFO,
                    inVC.getPointer(), 
                    outVC.getPointer(), 
                    null, 
                    TIMEOUT_MS
            );
            outVC.read();
            
            log.info("{} CLIENT_QueryDevInfo 返回: qok={}", LOG_PREFIX, qok);
            
            if (qok) {
                // 从返回结构中获取支持 PTZ 的通道列表
                int ptzCount = outVC.stInputChannelsEx.nPTZCount;
                short[] ptzChannels = outVC.stInputChannelsEx.nPTZ;
                
                log.info("{} PTZ 原始数据: count={}, channels={}", LOG_PREFIX, ptzCount, 
                        java.util.Arrays.toString(java.util.Arrays.copyOf(ptzChannels, Math.min(ptzCount + 2, ptzChannels.length))));
                
                for (int k = 0; k < ptzCount && k < ptzChannels.length; k++) {
                    int rawChannelNo = ptzChannels[k];
                    // 大华 SDK 返回的 PTZ 通道号通常是从 0 开始的索引
                    // 转换为从 1 开始的通道号
                    int channelNo = rawChannelNo + 1;
                    ptzSet.add(channelNo);
                    log.debug("{} PTZ 通道映射: raw={} -> channelNo={}", LOG_PREFIX, rawChannelNo, channelNo);
                }
                
                log.info("{} ✅ 获取 PTZ 支持通道: count={}, channels={}", LOG_PREFIX, ptzCount, ptzSet);
            } else {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.warn("{} CLIENT_QueryDevInfo(NET_QUERY_VIDEOCHANNELSINFO) 失败: {}", LOG_PREFIX, errorMsg);
            }
            
        } catch (Exception e) {
            log.warn("{} 查询 PTZ 通道列表异常: {}", LOG_PREFIX, e.getMessage());
        }
        
        return ptzSet;
    }
    
    /**
     * 检测通道是否支持 PTZ 云台控制
     * 
     * <p>检测策略（按优先级）：</p>
     * <ol>
     *     <li>通过通道名称推断（包含 PTZ/球机/DOME/IPC 等关键字）</li>
     *     <li>尝试通过 SDK 查询设备能力</li>
     * </ol>
     * 
     * @param loginHandle 登录句柄
     * @param channelNo 通道号（从 1 开始）
     * @param channelName 通道名称
     * @return true=支持云台, false=不支持
     */
    private boolean detectPtzSupport(long loginHandle, int channelNo, String channelName) {
        // 方式1：通过通道名称推断
        if (channelName != null) {
            String name = channelName.toUpperCase();
            if (name.contains("PTZ") || 
                name.contains("DOME") || 
                name.contains("球机") || 
                name.contains("球") ||
                name.contains("SPEED") ||
                name.contains("IPC")) {  // IPC 通常是可能支持 PTZ 的网络摄像机
                log.debug("{} 通道 {} 通过名称推断支持云台: name={}", LOG_PREFIX, channelNo, channelName);
                return true;
            }
        }
        
        // 方式2：尝试通过 SDK 查询设备能力（大华 SDK）
        try {
            // 使用 CLIENT_QueryDevState 查询云台能力
            // 或者尝试发送一个无操作的 PTZ 命令来检测
            // 这里暂时使用简化逻辑：通道号 > 0 且名称包含数字后缀的可能是 NVR 下的 IPC
            // 实际生产环境建议通过 CLIENT_GetDevCaps 获取精确能力
            
            // 备选：检查通道名称是否符合 IPC 模式（如 "IPC1", "IPC6"）
            if (channelName != null && channelName.matches("(?i).*IPC\\d*.*")) {
                log.debug("{} 通道 {} 匹配 IPC 模式，可能支持云台: name={}", LOG_PREFIX, channelNo, channelName);
                return true;
            }
        } catch (Exception e) {
            log.debug("{} 检测 PTZ 能力异常: channelNo={}, error={}", LOG_PREFIX, channelNo, e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 备用方法：基于登录信息构建通道列表
     * <p>当 SDK 查询失败时使用此方法，假设所有通道都在线</p>
     * 
     * @param loginHandle 登录句柄
     * @return 通道列表
     */
    private List<Map<String, Object>> queryChannelsFallback(long loginHandle) {
        List<Map<String, Object>> channelList = new ArrayList<>();
        
        // 从缓存获取通道数量（登录时保存）
        List<Map<String, Object>> cached = channelCache.get(loginHandle);
        int channelCount = cached != null ? cached.size() : 8; // 默认 8 通道
        
        // 如果有缓存，直接返回（保持之前的状态）
        if (cached != null && !cached.isEmpty()) {
            log.info("{} 使用缓存的通道信息: handle={}, count={}", LOG_PREFIX, loginHandle, cached.size());
            return cached;
        }
        
        // 构建默认通道列表（假设设备在线时通道也在线）
        for (int i = 0; i < channelCount; i++) {
            Map<String, Object> channel = new HashMap<>();
            channel.put("channelNo", i + 1);
            channel.put("channelName", "通道" + (i + 1));
            channel.put("online", true); // NVR 在线则假设通道在线
            channel.put("recording", false);
            channel.put("videoLoss", false);
            
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("ptzSupport", false);
            capabilities.put("audioSupport", true);
            channel.put("capabilities", capabilities);
            
            channelList.add(channel);
        }
        
        log.info("{} 使用备用方法生成通道列表: handle={}, count={}", LOG_PREFIX, loginHandle, channelList.size());
        return channelList;
    }
    
    /**
     * 设置通道数量（供外部调用，登录成功后设置）
     * 
     * @param loginHandle 登录句柄
     * @param channelCount 通道数量
     */
    public void setChannelCount(long loginHandle, int channelCount) {
        List<Map<String, Object>> channelList = new ArrayList<>();
        for (int i = 0; i < channelCount; i++) {
            Map<String, Object> channel = new HashMap<>();
            channel.put("channelNo", i + 1);
            channel.put("channelName", "通道" + (i + 1));
            channel.put("online", true);
            channel.put("recording", false);
            channelList.add(channel);
        }
        channelCache.put(loginHandle, channelList);
        log.debug("{} 设置通道数量缓存: handle={}, count={}", LOG_PREFIX, loginHandle, channelCount);
    }

    // ==================== 硬盘状态查询 ====================

    public NvrOperationResult queryDiskStatus(long loginHandle) {
        if (loginHandle <= 0) {
            return NvrOperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 查询硬盘状态: handle={}", LOG_PREFIX, loginHandle);
            log.warn("{} {}", LOG_PREFIX, UNSUPPORTED_MSG);
            return NvrOperationResult.failure(UNSUPPORTED_MSG);
            
        } catch (Exception e) {
            log.error("{} 查询硬盘状态异常: error={}", LOG_PREFIX, e.getMessage(), e);
            return NvrOperationResult.failure("查询硬盘状态异常: " + e.getMessage());
        }
    }

    // ==================== 抓图 ====================

    public NvrOperationResult capturePicture(long loginHandle, int channelNo) {
        if (loginHandle <= 0) {
            return NvrOperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 抓图: handle={}, channel={}", LOG_PREFIX, loginHandle, channelNo);
            log.warn("{} {}", LOG_PREFIX, UNSUPPORTED_MSG);
            return NvrOperationResult.failure(UNSUPPORTED_MSG);
            
        } catch (Exception e) {
            log.error("{} 抓图异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return NvrOperationResult.failure("抓图异常: " + e.getMessage());
        }
    }

    // ==================== 巡航组控制 ====================

    /**
     * PTZ 扩展命令常量
     */
    private static final int NET_EXTPTZ_ADDTOLOOP = 0x24;      // 加入预置点到巡航
    private static final int NET_EXTPTZ_DELFROMLOOP = 0x25;    // 从巡航中删除预置点
    private static final int NET_EXTPTZ_CLOSELOOP = 0x26;      // 清除巡航
    private static final int NET_EXTPTZ_STARTLOOP = 0x27;      // 开始巡航（官方可能使用 NET_PTZ_POINT_LOOP_CONTROL）
    private static final int NET_PTZ_POINT_LOOP_CONTROL = 13;  // 点间巡航

    /**
     * 启动设备巡航（直连 IPC）
     * 
     * @param ip        IPC 设备 IP
     * @param username  用户名
     * @param password  密码
     * @param channelNo 通道号
     * @param tourNo    巡航组编号（1-8）
     * @return 操作结果
     */
    public NvrOperationResult startTourDirect(String ip, String username, String password,
                                               int channelNo, int tourNo) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        try {
            log.info("{} 启动设备巡航: ip={}, channel={}, tourNo={}", LOG_PREFIX, ip, channelNo, tourNo);
            
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            // 使用 NET_PTZ_POINT_LOOP_CONTROL 命令启动巡航
            // param1: 巡航组编号, param2: 0, param3: 0, stop: 0
            log.info("{} 启动巡航SDK调用: handle={}, sdkChannel={}, tourNo={}", 
                    LOG_PREFIX, loginHandle.longValue(), sdkChannelNo, tourNo);
            
            boolean result = getNetSdk().CLIENT_DHPTZControlEx(
                    loginHandle,
                    sdkChannelNo,
                    NET_PTZ_POINT_LOOP_CONTROL,
                    0,           // param1: 0
                    tourNo,      // param2: 巡航组编号
                    0,           // param3: 0
                    0            // dwStop: 0=启动
            );
            
            String errorInfo = ToolKits.getErrorCodePrint();
            
            if (result) {
                log.info("{} 启动设备巡航成功: ip={}, tourNo={}", LOG_PREFIX, ip, tourNo);
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success("启动巡航成功");
            } else {
                log.warn("{} 启动设备巡航失败: ip={}, tourNo={}, error={}", LOG_PREFIX, ip, tourNo, errorInfo);
                return NvrOperationResult.failure("启动巡航失败: " + errorInfo);
            }
            
        } catch (Exception e) {
            log.error("{} 启动设备巡航异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrOperationResult.failure("启动巡航异常: " + e.getMessage());
        }
    }

    /**
     * 停止设备巡航（直连 IPC）
     * 
     * @param ip        IPC 设备 IP
     * @param username  用户名
     * @param password  密码
     * @param channelNo 通道号
     * @param tourNo    巡航组编号（1-8）
     * @return 操作结果
     */
    public NvrOperationResult stopTourDirect(String ip, String username, String password,
                                              int channelNo, int tourNo) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        try {
            log.info("{} 停止设备巡航: ip={}, channel={}, tourNo={}", LOG_PREFIX, ip, channelNo, tourNo);
            
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            // 使用 NET_PTZ_POINT_LOOP_CONTROL 命令停止巡航
            // param1: 巡航组编号, param2: 0, param3: 0, stop: 1
            log.info("{} 停止巡航SDK调用: handle={}, sdkChannel={}, tourNo={}", 
                    LOG_PREFIX, loginHandle.longValue(), sdkChannelNo, tourNo);
            
            boolean result = getNetSdk().CLIENT_DHPTZControlEx(
                    loginHandle,
                    sdkChannelNo,
                    NET_PTZ_POINT_LOOP_CONTROL,
                    0,           // param1: 0
                    tourNo,      // param2: 巡航组编号
                    0,           // param3: 0
                    1            // dwStop: 1=停止
            );
            
            String errorInfo = ToolKits.getErrorCodePrint();
            
            if (result) {
                log.info("{} 停止设备巡航成功: ip={}, tourNo={}", LOG_PREFIX, ip, tourNo);
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success("停止巡航成功");
            } else {
                log.warn("{} 停止设备巡航失败: ip={}, tourNo={}, error={}", LOG_PREFIX, ip, tourNo, errorInfo);
                return NvrOperationResult.failure("停止巡航失败: " + errorInfo);
            }
            
        } catch (Exception e) {
            log.error("{} 停止设备巡航异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrOperationResult.failure("停止巡航异常: " + e.getMessage());
        }
    }

    /**
     * 添加预设点到巡航组（直连 IPC）
     * 
     * @param ip        IPC 设备 IP
     * @param username  用户名
     * @param password  密码
     * @param channelNo 通道号
     * @param tourNo    巡航组编号（1-8）
     * @param presetNo  预设点编号
     * @return 操作结果
     */
    public NvrOperationResult addPresetToTourDirect(String ip, String username, String password,
                                                     int channelNo, int tourNo, int presetNo) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        try {
            log.info("{} 添加预设点到巡航组: ip={}, channel={}, tourNo={}, presetNo={}", 
                    LOG_PREFIX, ip, channelNo, tourNo, presetNo);
            
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            // 使用扩展命令 NET_EXTPTZ_ADDTOLOOP
            // param1: 巡航组编号, param2: 预设点编号
            log.info("{} 添加预设点到巡航SDK调用: handle={}, sdkChannel={}, tourNo={}, presetNo={}", 
                    LOG_PREFIX, loginHandle.longValue(), sdkChannelNo, tourNo, presetNo);
            
            boolean result = getNetSdk().CLIENT_DHPTZControlEx(
                    loginHandle,
                    sdkChannelNo,
                    NET_EXTPTZ_ADDTOLOOP,
                    tourNo,      // param1: 巡航组编号
                    presetNo,    // param2: 预设点编号
                    0,           // param3: 0
                    0            // dwStop: 0
            );
            
            String errorInfo = ToolKits.getErrorCodePrint();
            
            if (result) {
                log.info("{} 添加预设点到巡航组成功: ip={}, tourNo={}, presetNo={}", 
                        LOG_PREFIX, ip, tourNo, presetNo);
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success("添加预设点成功");
            } else {
                log.warn("{} 添加预设点到巡航组失败: ip={}, tourNo={}, presetNo={}, error={}", 
                        LOG_PREFIX, ip, tourNo, presetNo, errorInfo);
                return NvrOperationResult.failure("添加预设点失败: " + errorInfo);
            }
            
        } catch (Exception e) {
            log.error("{} 添加预设点到巡航组异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrOperationResult.failure("添加预设点异常: " + e.getMessage());
        }
    }

    /**
     * 清除巡航组（直连 IPC）
     * 
     * @param ip        IPC 设备 IP
     * @param username  用户名
     * @param password  密码
     * @param channelNo 通道号
     * @param tourNo    巡航组编号（1-8）
     * @return 操作结果
     */
    public NvrOperationResult clearTourDirect(String ip, String username, String password,
                                               int channelNo, int tourNo) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        try {
            log.info("{} 清除巡航组: ip={}, channel={}, tourNo={}", LOG_PREFIX, ip, channelNo, tourNo);
            
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            // 使用扩展命令 NET_EXTPTZ_CLOSELOOP
            log.info("{} 清除巡航组SDK调用: handle={}, sdkChannel={}, tourNo={}", 
                    LOG_PREFIX, loginHandle.longValue(), sdkChannelNo, tourNo);
            
            boolean result = getNetSdk().CLIENT_DHPTZControlEx(
                    loginHandle,
                    sdkChannelNo,
                    NET_EXTPTZ_CLOSELOOP,
                    tourNo,      // param1: 巡航组编号
                    0,           // param2: 0
                    0,           // param3: 0
                    0            // dwStop: 0
            );
            
            String errorInfo = ToolKits.getErrorCodePrint();
            
            if (result) {
                log.info("{} 清除巡航组成功: ip={}, tourNo={}", LOG_PREFIX, ip, tourNo);
                ipcLoginTimeCache.put(ip, System.currentTimeMillis());
                return NvrOperationResult.success("清除巡航组成功");
            } else {
                log.warn("{} 清除巡航组失败: ip={}, tourNo={}, error={}", LOG_PREFIX, ip, tourNo, errorInfo);
                return NvrOperationResult.failure("清除巡航组失败: " + errorInfo);
            }
            
        } catch (Exception e) {
            log.error("{} 清除巡航组异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrOperationResult.failure("清除巡航组异常: " + e.getMessage());
        }
    }

    /**
     * 同步巡航组配置到设备（直连 IPC）
     * 
     * <p>将本地巡航配置同步到设备，使用配置 API 设置巡航组名称、预设点及停留时间</p>
     * 
     * @param ip         IPC 设备 IP
     * @param username   用户名
     * @param password   密码
     * @param channelNo  通道号
     * @param tourNo     巡航组编号（1-8）
     * @param tourName   巡航组名称
     * @param presetNos  预设点编号列表（按顺序）
     * @param dwellTimes 每个预设点的停留时间（秒）
     * @return 操作结果
     */
    public NvrOperationResult syncTourToDeviceDirect(String ip, String username, String password,
                                                      int channelNo, int tourNo, String tourName,
                                                      List<Integer> presetNos, List<Integer> dwellTimes) {
        if (!sdkInitialized) {
            return NvrOperationResult.failure("SDK 未初始化");
        }
        
        if (presetNos == null || presetNos.isEmpty()) {
            return NvrOperationResult.failure("预设点列表为空");
        }
        
        try {
            log.info("{} 同步巡航组到设备: ip={}, channel={}, tourNo={}, tourName={}, presetCount={}, dwellTimes={}", 
                    LOG_PREFIX, ip, channelNo, tourNo, tourName, presetNos.size(), dwellTimes);
            
            LLong loginHandle = getOrCreateIpcSession(ip, username, password);
            if (loginHandle == null || loginHandle.longValue() == 0) {
                return NvrOperationResult.failure("IPC登录失败");
            }
            
            int sdkChannelNo = channelNo > 0 ? channelNo - 1 : 0;
            
            // 使用配置 API 设置巡航组（包含名称和停留时间）
            // 先尝试配置 API，如果失败则回退到 PTZ 命令
            boolean configSuccess = false;
            
            try {
                // 构建巡航组配置 JSON（格式参考大华 SDK 文档 CFG_PTZTOUR_INFO）
                StringBuilder presetsJson = new StringBuilder();
                for (int i = 0; i < presetNos.size(); i++) {
                    if (i > 0) presetsJson.append(",");
                    int presetId = presetNos.get(i);
                    int duration = (dwellTimes != null && i < dwellTimes.size()) ? dwellTimes.get(i) : 15;
                    int speed = 7; // 默认速度
                    presetsJson.append(String.format("{ \"PresetID\" : %d, \"Duration\" : %d, \"Speed\" : %d }", 
                            presetId, duration, speed));
                }
                
                // 巡航组索引（0-based）
                int tourIndex = tourNo - 1;
                if (tourIndex < 0) tourIndex = 0;
                
                // 构建完整的 PtzTour 配置
                // 注意：需要设置对应索引的巡航组
                String actualTourName = (tourName != null && !tourName.isEmpty()) ? tourName : ("巡航组" + tourNo);
                String jsonConfig = String.format(
                    "{ \"PtzTour\" : [ { \"Enable\" : true, \"Name\" : \"%s\", \"Presets\" : [ %s ] } ] }",
                    actualTourName,
                    presetsJson.toString()
                );
                
                log.info("{} 配置API同步巡航组JSON: {}", LOG_PREFIX, jsonConfig);
                
                byte[] inBuffer = jsonConfig.getBytes("GBK"); // 大华设备使用 GBK 编码
                IntByReference error = new IntByReference(0);
                IntByReference restart = new IntByReference(0);
                
                // 使用 CFG_CMD_PTZTOUR 命令，通道号使用 tourIndex 指定巡航组
                boolean result = getNetSdk().CLIENT_SetNewDevConfig(
                        loginHandle,
                        "PtzTour",
                        tourIndex,  // 使用巡航组索引作为通道参数
                        inBuffer,
                        inBuffer.length,
                        error,
                        restart,
                        5000
                );
                
                if (result) {
                    log.info("{} ✅ 配置API同步巡航组成功: ip={}, tourNo={}, tourName={}", 
                            LOG_PREFIX, ip, tourNo, actualTourName);
                    configSuccess = true;
                } else {
                    String errorInfo = ToolKits.getErrorCodePrint();
                    log.warn("{} 配置API同步巡航组失败，尝试PTZ命令方式: error={}", LOG_PREFIX, errorInfo);
                }
                
            } catch (Exception e) {
                log.warn("{} 配置API同步巡航组异常，回退到PTZ命令方式: {}", LOG_PREFIX, e.getMessage());
            }
            
            // 如果配置API失败，回退到PTZ命令方式（但无法设置名称和停留时间）
            if (!configSuccess) {
                log.info("{} 使用PTZ命令方式同步巡航组（无法设置名称和停留时间）", LOG_PREFIX);
                
                // 1. 先清除巡航组
                NvrOperationResult clearResult = clearTourDirect(ip, username, password, channelNo, tourNo);
                if (!clearResult.isSuccess()) {
                    log.warn("{} 清除巡航组失败，继续添加: {}", LOG_PREFIX, clearResult.getMessage());
                }
                
                Thread.sleep(500);
                
                // 2. 依次添加预设点
                int successCount = 0;
                for (int presetNo : presetNos) {
                    NvrOperationResult addResult = addPresetToTourDirect(ip, username, password, channelNo, tourNo, presetNo);
                    if (addResult.isSuccess()) {
                        successCount++;
                    }
                    Thread.sleep(100);
                }
                
                if (successCount > 0) {
                    return NvrOperationResult.success("PTZ命令同步成功，添加了 " + successCount + " 个预设点（名称和停留时间使用设备默认值）");
                } else {
                    return NvrOperationResult.failure("同步失败，没有成功添加预设点");
                }
            }
            
            ipcLoginTimeCache.put(ip, System.currentTimeMillis());
            return NvrOperationResult.success("巡航组同步成功");
            
        } catch (Exception e) {
            log.error("{} 同步巡航组异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return NvrOperationResult.failure("同步巡航组异常: " + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    private void fillBytes(byte[] dest, String src) {
        if (src == null || dest == null) {
            return;
        }
        byte[] srcBytes = src.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int len = Math.min(srcBytes.length, dest.length - 1);
        System.arraycopy(srcBytes, 0, dest, 0, len);
        dest[len] = 0;
    }
}
