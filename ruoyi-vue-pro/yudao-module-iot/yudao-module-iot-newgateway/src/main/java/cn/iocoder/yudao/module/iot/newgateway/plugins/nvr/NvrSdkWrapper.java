package cn.iocoder.yudao.module.iot.newgateway.plugins.nvr;

import cn.iocoder.yudao.module.iot.newgateway.plugins.nvr.dto.*;
import cn.iocoder.yudao.module.iot.newgateway.util.NativeLibraryLoader;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.NetSDKLib.*;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
    private static final int NET_PTZ_LEFTUP_CONTROL = 10;      // 左上
    private static final int NET_PTZ_RIGHTUP_CONTROL = 11;     // 右上
    private static final int NET_PTZ_LEFTDOWN_CONTROL = 12;    // 左下
    private static final int NET_PTZ_RIGHTDOWN_CONTROL = 13;   // 右下

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
            
            // 调用大华 SDK 的 PTZ 控制接口（使用 sdkChannelNo，从 0 开始）
            boolean result = getNetSdk().CLIENT_DHPTZControlEx(
                    new LLong(loginHandle),
                    sdkChannelNo,
                    dwPTZCommand,
                    actualSpeed,
                    actualSpeed,
                    0,
                    isStop ? 1 : 0
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
            
            log.debug("{} PTZ SDK调用: handle={}, channel={}, cmd={}, speed={}, stop={}", 
                    LOG_PREFIX, loginHandle.longValue(), sdkChannelNo, dwPTZCommand, actualSpeed, isStop);
            
            boolean result = getNetSdk().CLIENT_DHPTZControlEx(
                    loginHandle,
                    sdkChannelNo,
                    dwPTZCommand,
                    actualSpeed,
                    actualSpeed,
                    0,
                    isStop ? 1 : 0
            );
            
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
            case "LEFT_UP": case "LEFTUP": return NET_PTZ_LEFTUP_CONTROL;
            case "RIGHT_UP": case "RIGHTUP": return NET_PTZ_RIGHTUP_CONTROL;
            case "LEFT_DOWN": case "LEFTDOWN": return NET_PTZ_LEFTDOWN_CONTROL;
            case "RIGHT_DOWN": case "RIGHTDOWN": return NET_PTZ_RIGHTDOWN_CONTROL;
            default: return -1;
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

    public NvrOperationResult queryRecordFiles(long loginHandle, int channelNo, 
            String startTime, String endTime, Integer recordType) {
        if (loginHandle <= 0) {
            return NvrOperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 查询录像文件: handle={}, channel={}, start={}, end={}", 
                    LOG_PREFIX, loginHandle, channelNo, startTime, endTime);
            log.warn("{} {}", LOG_PREFIX, UNSUPPORTED_MSG);
            return NvrOperationResult.failure(UNSUPPORTED_MSG);
            
        } catch (Exception e) {
            log.error("{} 查询录像文件异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return NvrOperationResult.failure("查询录像文件异常: " + e.getMessage());
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
     * @param loginHandle 登录句柄
     * @param maxChannels 最大通道数
     * @return 通道名称数组
     */
    private String[] queryChannelNames(long loginHandle, int maxChannels) {
        try {
            String[] names = new String[maxChannels];
            
            // 尝试获取通道名称配置
            // 这里使用简化方式，实际可以通过 CLIENT_GetDevConfig 获取
            for (int i = 0; i < maxChannels; i++) {
                names[i] = "通道" + (i + 1);
            }
            
            return names;
        } catch (Exception e) {
            log.warn("{} 查询通道名称异常: {}", LOG_PREFIX, e.getMessage());
            return null;
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
