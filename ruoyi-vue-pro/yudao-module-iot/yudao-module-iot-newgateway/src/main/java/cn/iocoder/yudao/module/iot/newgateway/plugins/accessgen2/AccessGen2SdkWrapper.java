package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2;

import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.*;
import cn.iocoder.yudao.module.iot.newgateway.util.NativeLibraryLoader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.NetSDKLib.*;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 门禁二代设备 SDK 封装
 * 
 * <p>封装大华 NetSDK 的登录/登出和标准 API 操作，用于门禁二代设备的管理。</p>
 * 
 * <h2>主要功能</h2>
 * <ul>
 *     <li>SDK 初始化和清理</li>
 *     <li>设备登录/登出</li>
 *     <li>远程开门/关门</li>
 *     <li>用户管理（增删改查）</li>
 *     <li>卡号管理（增删改查）</li>
 *     <li>人脸下发/删除</li>
 *     <li>指纹下发/删除</li>
 * </ul>
 * 
 * <h2>与门禁一代的区别</h2>
 * <ul>
 *     <li>门禁一代：使用 Recordset 操作，通过 recNo 管理卡号/用户</li>
 *     <li>门禁二代：使用标准 API，支持人脸/指纹等生物识别功能</li>
 * </ul>
 * 
 * <h2>注意事项</h2>
 * <p>
 * 运行态禁止伪造/模拟设备数据。对于尚未接入真实 SDK 调用的能力，统一返回失败并提示。
 * </p>
 * 
 * @author IoT Gateway Team
 * @see AccessGen2Plugin
 * @see AccessGen2ConnectionManager
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "access-gen2", havingValue = "true", matchIfMissing = true)
public class AccessGen2SdkWrapper {

    private static final String LOG_PREFIX = "[AccessGen2SdkWrapper]";

    /**
     * 仅用于解析 SDK 返回的 JSON 配置（CLIENT_GetNewDevConfig）
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    /**
     * SDK 实例 - 懒加载，避免类加载阶段触发 JNA 原生库加载导致 Spring 启动失败
     */
    private static volatile NetSDKLib netsdk;

    private static NetSDKLib getNetSdk() {
        if (netsdk == null) {
            synchronized (AccessGen2SdkWrapper.class) {
                if (netsdk == null) {
                    netsdk = NetSDKLib.NETSDK_INSTANCE;
                }
            }
        }
        return netsdk;
    }
    
    /** SDK 操作超时时间（毫秒） */
    private static final int TIMEOUT_MS = 10000;

    /**
     * 说明：以下能力在未完成真实 SDK 对接前，禁止使用内存模拟伪造结果：
     * 用户/卡号/人脸/指纹管理、关门、门常开/常闭、门通道列表查询等。
     */
    private static final String UNSUPPORTED_MSG =
            "门禁二代：相关能力尚未接入真实 SDK 调用（已移除内存模拟），请以设备真实回调/结果为准";
    
    /** SDK 是否已初始化 */
    private volatile boolean sdkInitialized = false;
    
    /** 线程本地变量存储最后错误信息 */
    private final ThreadLocal<Integer> lastErrorCode = ThreadLocal.withInitial(() -> 0);
    private final ThreadLocal<String> lastErrorMessage = ThreadLocal.withInitial(() -> "");
    
    /** 
     * 设备锁映射表 - 按登录句柄存储锁，确保同一设备的操作串行化
     * <p>大华 SDK 对同一登录句柄的并发操作可能导致数据校验错误（错误码 21），
     * 因此需要对同一设备的操作进行串行化控制。</p>
     */
    private final ConcurrentHashMap<Long, ReentrantLock> deviceLocks = new ConcurrentHashMap<>();
    
    /** 设备锁等待超时时间（秒） */
    private static final int DEVICE_LOCK_TIMEOUT_SECONDS = 15;
    
    // 注意：移除运行态"内存模拟存储"。若后续需要支持用户/卡/人脸/指纹管理，请对接 NetSDK 标准接口。
    
    /** 断线监听器列表 */
    private final List<DisconnectListener> disconnectListeners = new ArrayList<>();
    
    /**
     * 获取设备锁（按登录句柄）
     * <p>如果锁不存在则创建一个新的锁</p>
     *
     * @param loginHandle 登录句柄
     * @return 对应的设备锁
     */
    private ReentrantLock getDeviceLock(long loginHandle) {
        return deviceLocks.computeIfAbsent(loginHandle, k -> new ReentrantLock(true)); // 使用公平锁
    }
    
    /**
     * 移除设备锁（在设备登出时调用）
     *
     * @param loginHandle 登录句柄
     */
    public void removeDeviceLock(long loginHandle) {
        deviceLocks.remove(loginHandle);
    }
    
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

    /**
     * 初始化 SDK
     */
    @PostConstruct
    public void initialize() {
        if (sdkInitialized) {
            log.info("{} SDK 已初始化，跳过", LOG_PREFIX);
            return;
        }
        
        try {
            log.info("{} 正在初始化 SDK...", LOG_PREFIX);
            
            // 加载本地库文件
            NativeLibraryLoader.loadDahuaLibraries();
            
            // 初始化 SDK，设置断线回调
            boolean initResult = getNetSdk().CLIENT_Init(disconnectCallback, null);
            if (!initResult) {
                log.error("{} SDK 初始化失败", LOG_PREFIX);
                return;
            }
            
            // 设置连接超时时间
            getNetSdk().CLIENT_SetConnectTime(5000, 1);
            
            // 设置网络参数
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

    /**
     * 清理 SDK 资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("{} 正在清理 SDK 资源...", LOG_PREFIX);
        
        if (sdkInitialized) {
            try {
                getNetSdk().CLIENT_Cleanup();
                sdkInitialized = false;
                log.info("{} SDK 资源已清理", LOG_PREFIX);
            } catch (Exception e) {
                log.error("{} SDK 清理异常: {}", LOG_PREFIX, e.getMessage(), e);
            }
        }
        
        // 运行态不保留任何模拟存储
    }

    /**
     * 检查 SDK 是否已初始化
     */
    public boolean isInitialized() {
        return sdkInitialized;
    }

    // ==================== 设备登录/登出 ====================

    /**
     * 登录设备
     * 
     * @param ip       设备 IP 地址
     * @param port     设备端口（默认 37777）
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    public AccessGen2LoginResult login(String ip, int port, String username, String password) {
        if (!sdkInitialized) {
            return AccessGen2LoginResult.failure("SDK 未初始化");
        }
        
        if (!StringUtils.hasText(ip)) {
            return AccessGen2LoginResult.failure("IP 地址不能为空");
        }
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return AccessGen2LoginResult.failure("用户名和密码不能为空");
        }
        
        try {
            log.info("{} 登录设备: ip={}, port={}", LOG_PREFIX, ip, port);
            
            // 准备登录参数
            NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY loginIn = new NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
            loginIn.nPort = port;
            fillBytes(loginIn.szIP, ip);
            fillBytes(loginIn.szUserName, username);
            fillBytes(loginIn.szPassword, password);
            
            // 准备输出参数
            NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY loginOut = new NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();
            loginOut.stuDeviceInfo = new NET_DEVICEINFO_Ex();
            
            // 执行登录
            LLong loginHandle = netsdk.CLIENT_LoginWithHighLevelSecurity(loginIn, loginOut);
            
            if (loginHandle.longValue() == 0) {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 登录失败: ip={}, error={}", LOG_PREFIX, ip, errorMsg);
                return AccessGen2LoginResult.failure("登录失败: " + errorMsg);
            }
            
            NET_DEVICEINFO_Ex deviceInfo = loginOut.stuDeviceInfo;
            String serialNumber = new String(deviceInfo.sSerialNumber).trim();
            int channelCount = deviceInfo.byChanNum & 0xFF;
            
            // 门禁二代设备能力（face/fingerprint）应通过能力查询确认。
            // 这里使用“保守默认值”，避免在未确认设备支持能力时，盲目对设备执行指纹相关命令导致失败。
            boolean supportsFace = true;
            boolean supportsFingerprint = false;
            
            long handle = loginHandle.longValue();
            
            log.info("{} ✅ 登录成功: ip={}, handle={}, sn={}, channels={}, face={}, fingerprint={}", 
                    LOG_PREFIX, ip, handle, serialNumber, channelCount, supportsFace, supportsFingerprint);
            
            return AccessGen2LoginResult.success(
                    handle,
                    serialNumber,
                    channelCount,
                    deviceInfo.byDVRType,
                    supportsFace,
                    supportsFingerprint
            );
            
        } catch (Exception e) {
            log.error("{} 登录异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return AccessGen2LoginResult.failure("登录异常: " + e.getMessage());
        }
    }

    /**
     * 登出设备
     * 
     * @param loginHandle 登录句柄
     * @return 是否成功
     */
    public boolean logout(long loginHandle) {
        if (loginHandle == 0) {
            return true;
        }
        
        try {
            log.info("{} 登出设备: handle={}", LOG_PREFIX, loginHandle);
            
            boolean result = netsdk.CLIENT_Logout(new LLong(loginHandle));
            
            // 运行态不保留任何模拟存储
            
            if (result) {
                log.info("{} ✅ 登出成功: handle={}", LOG_PREFIX, loginHandle);
            } else {
                log.warn("{} 登出失败: handle={}, error={}", LOG_PREFIX, loginHandle, ToolKits.getErrorCodePrint());
            }
            
            // 清理设备锁
            removeDeviceLock(loginHandle);
            
            return result;
            
        } catch (Exception e) {
            log.error("{} 登出异常: handle={}, error={}", LOG_PREFIX, loginHandle, e.getMessage(), e);
            // 即使登出失败，也尝试清理设备锁
            removeDeviceLock(loginHandle);
            return false;
        }
    }

    // ==================== 门控制操作 ====================

    /**
     * 远程开门
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（从 0 开始）
     * @return 操作结果
     */
    public AccessGen2OperationResult openDoor(long loginHandle, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        
        // 获取设备锁，确保同一设备的操作串行化
        ReentrantLock lock = getDeviceLock(loginHandle);
        boolean lockAcquired = false;
        
        try {
            // 尝试获取锁，带超时
            lockAcquired = lock.tryLock(DEVICE_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!lockAcquired) {
                log.warn("{} 获取设备锁超时，设备可能正忙: handle={}, channelNo={}", LOG_PREFIX, loginHandle, channelNo);
                return AccessGen2OperationResult.failure("设备忙，请稍后重试");
            }
            
            log.info("{} 远程开门: handle={}, channel={}", LOG_PREFIX, loginHandle, channelNo);
            
            NET_CTRL_ACCESS_OPEN openInfo = new NET_CTRL_ACCESS_OPEN();
            openInfo.nChannelID = channelNo;
            openInfo.emOpenDoorType = EM_OPEN_DOOR_TYPE.EM_OPEN_DOOR_TYPE_REMOTE;
            
            Pointer pointer = new Memory(openInfo.size());
            ToolKits.SetStructDataToPointer(openInfo, pointer, 0);
            
            boolean result = netsdk.CLIENT_ControlDeviceEx(
                    new LLong(loginHandle),
                    CtrlType.CTRLTYPE_CTRL_ACCESS_OPEN,
                    pointer,
                    null,
                    TIMEOUT_MS
            );
            
            if (result) {
                log.info("{} ✅ 开门成功: channel={}", LOG_PREFIX, channelNo);
                return AccessGen2OperationResult.success("开门成功");
            } else {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 开门失败: channel={}, error={}", LOG_PREFIX, channelNo, errorMsg);
                return AccessGen2OperationResult.failure("开门失败: " + errorMsg);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("{} 开门被中断: channelNo={}", LOG_PREFIX, channelNo);
            return AccessGen2OperationResult.failure("操作被中断");
        } catch (Exception e) {
            log.error("{} 开门异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return AccessGen2OperationResult.failure("开门异常: " + e.getMessage());
        } finally {
            if (lockAcquired) {
                lock.unlock();
            }
        }
    }

    /**
     * 远程关门
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（从 0 开始）
     * @return 操作结果
     */
    public AccessGen2OperationResult closeDoor(long loginHandle, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        
        // 获取设备锁，确保同一设备的操作串行化
        ReentrantLock lock = getDeviceLock(loginHandle);
        boolean lockAcquired = false;
        
        try {
            // 尝试获取锁，带超时
            lockAcquired = lock.tryLock(DEVICE_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!lockAcquired) {
                log.warn("{} 获取设备锁超时，设备可能正忙: handle={}, channelNo={}", LOG_PREFIX, loginHandle, channelNo);
                return AccessGen2OperationResult.failure("设备忙，请稍后重试");
            }
            
            log.info("{} 远程关门: handle={}, channel={}", LOG_PREFIX, loginHandle, channelNo);
            
            NET_CTRL_ACCESS_CLOSE closeInfo = new NET_CTRL_ACCESS_CLOSE();
            closeInfo.nChannelID = channelNo;
            closeInfo.write();
            
            boolean result = netsdk.CLIENT_ControlDeviceEx(
                    new LLong(loginHandle),
                    CtrlType.CTRLTYPE_CTRL_ACCESS_CLOSE,
                    closeInfo.getPointer(),
                    null,
                    TIMEOUT_MS
            );
            closeInfo.read();
            
            if (result) {
                log.info("{} ✅ 关门成功: channel={}", LOG_PREFIX, channelNo);
                return AccessGen2OperationResult.success("关门成功");
            } else {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 关门失败: channel={}, error={}", LOG_PREFIX, channelNo, errorMsg);
                return AccessGen2OperationResult.failure("关门失败: " + errorMsg);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("{} 关门被中断: channelNo={}", LOG_PREFIX, channelNo);
            return AccessGen2OperationResult.failure("操作被中断");
        } catch (Exception e) {
            log.error("{} 关门异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return AccessGen2OperationResult.failure("关门异常: " + e.getMessage());
        } finally {
            if (lockAcquired) {
                lock.unlock();
            }
        }
    }

    // ==================== 用户管理（未接入，禁止模拟） ====================

    /**
     * 添加用户
     * 
     * @param loginHandle 登录句柄
     * @param userInfo    用户信息
     * @return 操作结果
     */
    public AccessGen2OperationResult addUser(long loginHandle, AccessGen2UserInfo userInfo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        if (userInfo == null || !StringUtils.hasText(userInfo.getUserId())) {
            return AccessGen2OperationResult.failure("userId 不能为空");
        }
        try {
            // 详细日志：打印用户信息
            log.info("{} 添加用户: userId={}, userName={}, password={}, validStart={}, validEnd={}, doors={}", 
                    LOG_PREFIX, userInfo.getUserId(), userInfo.getUserName(),
                    StringUtils.hasText(userInfo.getPassword()) ? "***(" + userInfo.getPassword().length() + "字符)" : "无",
                    userInfo.getValidStartTime(), userInfo.getValidEndTime(),
                    userInfo.getDoors() != null ? Arrays.toString(userInfo.getDoors()) : "无");
            
            // 构建最小可用用户信息（按《智能楼宇分册》：CLIENT_OperateAccessUserService + NET_ACCESS_USER_INFO）
            NET_ACCESS_USER_INFO user = new NET_ACCESS_USER_INFO();
            fillBytes(user.szUserID, userInfo.getUserId());
            fillBytes(user.szName, StringUtils.hasText(userInfo.getUserName()) ? userInfo.getUserName() : userInfo.getUserId());
            if (StringUtils.hasText(userInfo.getPassword())) {
                fillBytes(user.szPsw, userInfo.getPassword());
                log.info("{} 设置用户密码: userId={}, passwordLen={}", LOG_PREFIX, userInfo.getUserId(), userInfo.getPassword().length());
            } else {
                log.warn("{} 用户没有密码: userId={}", LOG_PREFIX, userInfo.getUserId());
            }

            // 门权限：SDK 里 door 通常从 0 开始；业务侧常用从 1 开始
            int[] doors = normalizeDoorIndexes(userInfo.getDoors());
            if (doors.length > 0) {
                user.nDoorNum = doors.length;
                for (int i = 0; i < doors.length && i < user.nDoors.length; i++) {
                    user.nDoors[i] = doors[i];
                }
                // 时间段：255 表示全天有效（文档示例）
                user.nTimeSectionNum = doors.length;
                for (int i = 0; i < doors.length && i < user.nTimeSectionNo.length; i++) {
                    user.nTimeSectionNo[i] = 255;
                }
            }

            // 有效期（可选）：仅在传入时设置，避免写入非法默认值
            fillValidTimeIfPresent(user, userInfo.getValidStartTime(), userInfo.getValidEndTime());

            NET_ACCESS_USER_INFO[] users = new NET_ACCESS_USER_INFO[] { user };

            NET_IN_ACCESS_USER_SERVICE_INSERT stIn = new NET_IN_ACCESS_USER_SERVICE_INSERT();
            stIn.nInfoNum = 1;
            stIn.pUserInfo = new Memory(users[0].size());
            stIn.pUserInfo.clear(users[0].size());
            ToolKits.SetStructArrToPointerData(users, stIn.pUserInfo);

            FAIL_CODE[] failCodes = new FAIL_CODE[] { new FAIL_CODE() };
            NET_OUT_ACCESS_USER_SERVICE_INSERT stOut = new NET_OUT_ACCESS_USER_SERVICE_INSERT();
            stOut.nMaxRetNum = 1;
            stOut.pFailCode = new Memory(failCodes[0].size());
            stOut.pFailCode.clear(failCodes[0].size());
            ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

            users[0].write();
            stIn.write();
            stOut.write();
            failCodes[0].write();

            int emType = NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_INSERT;
            boolean ok = getNetSdk().CLIENT_OperateAccessUserService(new LLong(loginHandle), emType,
                    stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
            if (!ok) {
                return AccessGen2OperationResult.failure("添加用户失败: " + ToolKits.getErrorCodePrint());
            }
            ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);
            if (failCodes[0].nFailCode != 0) {
                return AccessGen2OperationResult.failure("添加用户失败: failCode=" + failCodes[0].nFailCode);
            }
            return AccessGen2OperationResult.success("添加用户成功");
        } catch (Exception e) {
            log.error("{} 添加用户异常: userId={}, error={}", LOG_PREFIX, userInfo.getUserId(), e.getMessage(), e);
            return AccessGen2OperationResult.failure("添加用户异常: " + e.getMessage());
        }
    }

    /**
     * 更新用户
     * 
     * @param loginHandle 登录句柄
     * @param userInfo    用户信息
     * @return 操作结果
     */
    public AccessGen2OperationResult updateUser(long loginHandle, AccessGen2UserInfo userInfo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        if (userInfo == null || !StringUtils.hasText(userInfo.getUserId())) {
            return AccessGen2OperationResult.failure("userId 不能为空");
        }
        // 文档中用户管理未单独列出 UPDATE，采用 remove + insert 语义
        AccessGen2OperationResult remove = deleteUser(loginHandle, userInfo.getUserId());
        if (!remove.isSuccess()) {
            log.warn("{} 更新用户：先删除失败，将继续尝试插入。userId={}, msg={}", LOG_PREFIX, userInfo.getUserId(), remove.getMessage());
        }
        return addUser(loginHandle, userInfo);
    }

    /**
     * 删除用户
     * 
     * @param loginHandle 登录句柄
     * @param userId      用户ID
     * @return 操作结果
     */
    public AccessGen2OperationResult deleteUser(long loginHandle, String userId) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        if (!StringUtils.hasText(userId)) {
            return AccessGen2OperationResult.failure("userId 不能为空");
        }
        try {
            NET_IN_ACCESS_USER_SERVICE_REMOVE stIn = new NET_IN_ACCESS_USER_SERVICE_REMOVE();
            stIn.nUserNum = 1;
            stIn.bUserIDEx = 0;
            stIn.szUserIDs = new USERID[] { new USERID() };
            fillBytes(stIn.szUserIDs[0].szUserID, userId);

            FAIL_CODE[] failCodes = new FAIL_CODE[] { new FAIL_CODE() };
            NET_OUT_ACCESS_USER_SERVICE_REMOVE stOut = new NET_OUT_ACCESS_USER_SERVICE_REMOVE();
            stOut.nMaxRetNum = 1;
            stOut.pFailCode = new Memory(failCodes[0].size());
            stOut.pFailCode.clear(failCodes[0].size());
            ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

            stIn.write();
            stOut.write();
            failCodes[0].write();

            int emType = NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_REMOVE;
            boolean ok = getNetSdk().CLIENT_OperateAccessUserService(new LLong(loginHandle), emType,
                    stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
            if (!ok) {
                return AccessGen2OperationResult.failure("删除用户失败: " + ToolKits.getErrorCodePrint());
            }
            ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);
            if (failCodes[0].nFailCode != 0) {
                return AccessGen2OperationResult.failure("删除用户失败: failCode=" + failCodes[0].nFailCode);
            }
            return AccessGen2OperationResult.success("删除用户成功");
        } catch (Exception e) {
            log.error("{} 删除用户异常: userId={}, error={}", LOG_PREFIX, userId, e.getMessage(), e);
            return AccessGen2OperationResult.failure("删除用户异常: " + e.getMessage());
        }
    }

    /**
     * 设置用户密码（使用密码记录集方式：NET_RECORDSET_ACCESS_CTL_PWD）
     * 
     * <p>大华门禁设备的密码需要通过密码记录集来设置，而不是通过 addUser 的 szPsw 字段。
     * 使用 CLIENT_ControlDevice + CTRLTYPE_CTRL_RECORDSET_INSERT + NET_RECORD_ACCESSCTLPWD</p>
     * 
     * @param loginHandle 登录句柄
     * @param userId      用户ID
     * @param password    密码
     * @param channelNo   门禁通道号（从0开始，默认0）
     * @return 操作结果
     */
    public AccessGen2OperationResult setPassword(long loginHandle, String userId, String password, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        if (!StringUtils.hasText(userId)) {
            return AccessGen2OperationResult.failure("userId 不能为空");
        }
        if (!StringUtils.hasText(password)) {
            return AccessGen2OperationResult.failure("password 不能为空");
        }
        
        try {
            log.info("{} 设置用户密码（记录集方式）: userId={}, passwordLen={}, channelNo={}", 
                    LOG_PREFIX, userId, password.length(), channelNo);
            
            // 构建密码记录集结构体
            NET_RECORDSET_ACCESS_CTL_PWD pwdRecord = new NET_RECORDSET_ACCESS_CTL_PWD();
            
            // 设置用户ID
            fillBytes(pwdRecord.szUserID, userId);
            
            // 设置开门密码
            fillBytes(pwdRecord.szDoorOpenPwd, password);
            
            // 设置门权限 - 与用户门权限保持一致
            pwdRecord.nDoorNum = 1;
            pwdRecord.sznDoors[0] = channelNo;
            pwdRecord.nTimeSectionNum = 1;
            pwdRecord.nTimeSectionIndex[0] = 255; // 255表示全天有效
            
            // 设置有效期（默认5年）
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime endTime = now.plusYears(5);
            
            pwdRecord.stuValidStartTime.dwYear = now.getYear();
            pwdRecord.stuValidStartTime.dwMonth = now.getMonthValue();
            pwdRecord.stuValidStartTime.dwDay = now.getDayOfMonth();
            pwdRecord.stuValidStartTime.dwHour = now.getHour();
            pwdRecord.stuValidStartTime.dwMinute = now.getMinute();
            pwdRecord.stuValidStartTime.dwSecond = now.getSecond();
            
            pwdRecord.stuValidEndTime.dwYear = endTime.getYear();
            pwdRecord.stuValidEndTime.dwMonth = endTime.getMonthValue();
            pwdRecord.stuValidEndTime.dwDay = endTime.getDayOfMonth();
            pwdRecord.stuValidEndTime.dwHour = endTime.getHour();
            pwdRecord.stuValidEndTime.dwMinute = endTime.getMinute();
            pwdRecord.stuValidEndTime.dwSecond = endTime.getSecond();
            
            // 构建记录集操作参数
            NET_CTRL_RECORDSET_INSERT_PARAM insertParam = new NET_CTRL_RECORDSET_INSERT_PARAM();
            insertParam.stuCtrlRecordSetInfo.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLPWD; // 门禁密码记录类型
            insertParam.stuCtrlRecordSetInfo.pBuf = pwdRecord.getPointer();
            
            pwdRecord.write();
            insertParam.write();
            
            // 调用设备控制接口插入密码记录
            boolean result = getNetSdk().CLIENT_ControlDevice(
                    new LLong(loginHandle),
                    CtrlType.CTRLTYPE_CTRL_RECORDSET_INSERT,
                    insertParam.getPointer(),
                    TIMEOUT_MS
            );
            
            insertParam.read();
            pwdRecord.read();
            
            if (result) {
                log.info("{} 设置用户密码成功（记录集方式）: userId={}, recNo={}", 
                        LOG_PREFIX, userId, insertParam.stuCtrlRecordSetResult.nRecNo);
                return AccessGen2OperationResult.success("设置密码成功");
            } else {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 设置用户密码失败（记录集方式）: userId={}, error={}", LOG_PREFIX, userId, errorMsg);
                
                // 如果记录集方式失败，尝试使用重置密码方式
                log.info("{} 尝试使用重置密码方式设置密码: userId={}", LOG_PREFIX, userId);
                return setPasswordByReset(loginHandle, userId, password, channelNo);
            }
        } catch (Exception e) {
            log.error("{} 设置用户密码异常: userId={}, error={}", LOG_PREFIX, userId, e.getMessage(), e);
            return AccessGen2OperationResult.failure("设置密码异常: " + e.getMessage());
        }
    }
    
    /**
     * 使用重置密码方式设置密码（备用方案）
     */
    private AccessGen2OperationResult setPasswordByReset(long loginHandle, String userId, String password, int channelNo) {
        try {
            // 构建密码重置结构体
            NET_CTRL_ACCESS_RESET_PASSWORD resetInfo = new NET_CTRL_ACCESS_RESET_PASSWORD();
            resetInfo.nChannelID = channelNo;
            resetInfo.emType = EM_ACCESS_PASSWORD_TYPE.EM_ACCESS_PASSWORD_OPENDOOR; // 开门密码
            fillBytes(resetInfo.szUserID, userId);
            fillBytes(resetInfo.szNewPassword, password);
            resetInfo.write();
            
            // 调用设备控制接口
            boolean result = getNetSdk().CLIENT_ControlDeviceEx(
                    new LLong(loginHandle),
                    CtrlType.CTRLTYPE_CTRL_ACCESS_RESET_PASSWORD,
                    resetInfo.getPointer(),
                    null,
                    TIMEOUT_MS
            );
            resetInfo.read();
            
            if (result) {
                log.info("{} 设置用户密码成功（重置方式）: userId={}", LOG_PREFIX, userId);
                return AccessGen2OperationResult.success("设置密码成功（重置方式）");
            } else {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 设置用户密码失败（重置方式）: userId={}, error={}", LOG_PREFIX, userId, errorMsg);
                return AccessGen2OperationResult.failure("设置密码失败: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("{} 设置用户密码异常（重置方式）: userId={}, error={}", LOG_PREFIX, userId, e.getMessage(), e);
            return AccessGen2OperationResult.failure("设置密码异常: " + e.getMessage());
        }
    }

    /**
     * 清空所有用户
     * 
     * @param loginHandle 登录句柄
     * @return 操作结果
     */
    public AccessGen2OperationResult clearAllUsers(long loginHandle) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        try {
            NET_IN_ACCESS_USER_SERVICE_CLEAR stIn = new NET_IN_ACCESS_USER_SERVICE_CLEAR();
            NET_OUT_ACCESS_USER_SERVICE_CLEAR stOut = new NET_OUT_ACCESS_USER_SERVICE_CLEAR();
            stIn.write();
            stOut.write();
            int emType = NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_CLEAR;
            boolean ok = getNetSdk().CLIENT_OperateAccessUserService(new LLong(loginHandle), emType,
                    stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
            return ok ? AccessGen2OperationResult.success("清空用户成功")
                    : AccessGen2OperationResult.failure("清空用户失败: " + ToolKits.getErrorCodePrint());
        } catch (Exception e) {
            log.error("{} 清空用户异常: {}", LOG_PREFIX, e.getMessage(), e);
            return AccessGen2OperationResult.failure("清空用户异常: " + e.getMessage());
        }
    }

    /**
     * 查询用户列表
     * 
     * @param loginHandle 登录句柄
     * @param userId      用户ID（可选，为空则查询所有）
     * @return 用户列表
     */
    public List<AccessGen2UserInfo> queryUsers(long loginHandle, String userId) {
        if (!validateHandle(loginHandle)) {
            return Collections.emptyList();
        }
        try {
            if (StringUtils.hasText(userId)) {
                return queryUsersByIds(loginHandle, List.of(userId));
            }
            return queryUsersByFind(loginHandle, 200);
        } catch (Exception e) {
            log.error("{} 查询用户异常: {}", LOG_PREFIX, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ==================== 卡号管理（二代门禁使用标准API） ====================

    /**
     * 添加卡号（二代门禁使用 CLIENT_OperateAccessCardService API）
     * 注意：二代门禁的卡信息结构体(NET_ACCESS_CARD_INFO)不支持密码和有效期字段
     *       密码和有效期需要通过 addUser 的 NET_ACCESS_USER_INFO 设置
     * 
     * @param loginHandle 登录句柄
     * @param userInfo    包含卡号的用户信息
     * @return 操作结果
     */
    public AccessGen2OperationResult addCard(long loginHandle, AccessGen2UserInfo userInfo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        if (userInfo == null || !StringUtils.hasText(userInfo.getCardNo())) {
            return AccessGen2OperationResult.failure("cardNo 不能为空");
        }
        try {
            log.info("{} 添加卡号（标准API）: cardNo={}, userId={}", 
                    LOG_PREFIX, userInfo.getCardNo(), userInfo.getUserId());
            
            NET_ACCESS_CARD_INFO card = new NET_ACCESS_CARD_INFO();
            fillBytes(card.szCardNo, userInfo.getCardNo());
            String uid = StringUtils.hasText(userInfo.getUserId()) ? userInfo.getUserId() : userInfo.getCardNo();
            fillBytes(card.szUserID, uid);
            card.bUserIDEx = 1;
            fillBytes(card.szUserIDEx, uid);

            NET_ACCESS_CARD_INFO[] cards = new NET_ACCESS_CARD_INFO[] { card };

            NET_IN_ACCESS_CARD_SERVICE_INSERT stIn = new NET_IN_ACCESS_CARD_SERVICE_INSERT();
            stIn.nInfoNum = 1;
            stIn.pCardInfo = new Memory(cards[0].size());
            stIn.pCardInfo.clear(cards[0].size());
            ToolKits.SetStructArrToPointerData(cards, stIn.pCardInfo);

            FAIL_CODE[] failCodes = new FAIL_CODE[] { new FAIL_CODE() };
            NET_OUT_ACCESS_CARD_SERVICE_INSERT stOut = new NET_OUT_ACCESS_CARD_SERVICE_INSERT();
            stOut.nMaxRetNum = 1;
            stOut.pFailCode = new Memory(failCodes[0].size());
            stOut.pFailCode.clear(failCodes[0].size());
            ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

            cards[0].write();
            stIn.write();
            stOut.write();
            failCodes[0].write();

            int emType = NET_EM_ACCESS_CTL_CARD_SERVICE.NET_EM_ACCESS_CTL_CARD_SERVICE_INSERT;
            boolean ok = getNetSdk().CLIENT_OperateAccessCardService(new LLong(loginHandle), emType,
                    stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
            if (!ok) {
                int errorCode = getNetSdk().CLIENT_GetLastError();
                int actualErrorCode = errorCode & 0x7FFFFFFF;
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 添加卡号失败: cardNo={}, errorCode={}, actualErrorCode={}, msg={}", 
                        LOG_PREFIX, userInfo.getCardNo(), errorCode, actualErrorCode, errorMsg);
                return AccessGen2OperationResult.failure("添加卡号失败: " + errorMsg, actualErrorCode);
            }
            ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);
            if (failCodes[0].nFailCode != 0) {
                log.error("{} 添加卡号失败: cardNo={}, failCode={}", 
                        LOG_PREFIX, userInfo.getCardNo(), failCodes[0].nFailCode);
                return AccessGen2OperationResult.failure("添加卡号失败: failCode=" + failCodes[0].nFailCode);
            }
            log.info("{} ✅ 添加卡号成功: cardNo={}", LOG_PREFIX, userInfo.getCardNo());
            return AccessGen2OperationResult.success("添加卡号成功");
        } catch (Exception e) {
            log.error("{} 添加卡号异常: cardNo={}, error={}", LOG_PREFIX, userInfo.getCardNo(), e.getMessage(), e);
            return AccessGen2OperationResult.failure("添加卡号异常: " + e.getMessage());
        }
    }

    /**
     * 删除卡号
     * 
     * @param loginHandle 登录句柄
     * @param cardNo      卡号
     * @return 操作结果
     */
    public AccessGen2OperationResult deleteCard(long loginHandle, String cardNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        if (!StringUtils.hasText(cardNo)) {
            return AccessGen2OperationResult.failure("cardNo 不能为空");
        }
        try {
            NET_IN_ACCESS_CARD_SERVICE_REMOVE stIn = new NET_IN_ACCESS_CARD_SERVICE_REMOVE();
            stIn.nCardNum = 1;
            stIn.szCardNos = new CARDNO[] { new CARDNO() };
            fillBytes(stIn.szCardNos[0].szCardNo, cardNo);

            FAIL_CODE[] failCodes = new FAIL_CODE[] { new FAIL_CODE() };
            NET_OUT_ACCESS_CARD_SERVICE_REMOVE stOut = new NET_OUT_ACCESS_CARD_SERVICE_REMOVE();
            stOut.nMaxRetNum = 1;
            stOut.pFailCode = new Memory(failCodes[0].size());
            stOut.pFailCode.clear(failCodes[0].size());
            ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

            stIn.write();
            stOut.write();
            failCodes[0].write();

            int emType = NET_EM_ACCESS_CTL_CARD_SERVICE.NET_EM_ACCESS_CTL_CARD_SERVICE_REMOVE;
            boolean ok = getNetSdk().CLIENT_OperateAccessCardService(new LLong(loginHandle), emType,
                    stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
            if (!ok) {
                return AccessGen2OperationResult.failure("删除卡号失败: " + ToolKits.getErrorCodePrint());
            }
            ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);
            if (failCodes[0].nFailCode != 0) {
                return AccessGen2OperationResult.failure("删除卡号失败: failCode=" + failCodes[0].nFailCode);
            }
            return AccessGen2OperationResult.success("删除卡号成功");
        } catch (Exception e) {
            log.error("{} 删除卡号异常: cardNo={}, error={}", LOG_PREFIX, cardNo, e.getMessage(), e);
            return AccessGen2OperationResult.failure("删除卡号异常: " + e.getMessage());
        }
    }

    /**
     * 清空所有卡号
     * 
     * @param loginHandle 登录句柄
     * @return 操作结果
     */
    public AccessGen2OperationResult clearAllCards(long loginHandle) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        try {
            NET_IN_ACCESS_CARD_SERVICE_CLEAR stIn = new NET_IN_ACCESS_CARD_SERVICE_CLEAR();
            NET_OUT_ACCESS_CARD_SERVICE_CLEAR stOut = new NET_OUT_ACCESS_CARD_SERVICE_CLEAR();
            stIn.write();
            stOut.write();
            int emType = NET_EM_ACCESS_CTL_CARD_SERVICE.NET_EM_ACCESS_CTL_CARD_SERVICE_CLEAR;
            boolean ok = getNetSdk().CLIENT_OperateAccessCardService(new LLong(loginHandle), emType,
                    stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
            return ok ? AccessGen2OperationResult.success("清空卡号成功")
                    : AccessGen2OperationResult.failure("清空卡号失败: " + ToolKits.getErrorCodePrint());
        } catch (Exception e) {
            log.error("{} 清空卡号异常: {}", LOG_PREFIX, e.getMessage(), e);
            return AccessGen2OperationResult.failure("清空卡号异常: " + e.getMessage());
        }
    }


    // ==================== 人脸管理（未接入，禁止模拟） ====================

    /**
     * 添加人脸
     * 
     * @param loginHandle 登录句柄
     * @param faceInfo    人脸信息
     * @return 操作结果
     */
    public AccessGen2OperationResult addFace(long loginHandle, AccessGen2FaceInfo faceInfo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        if (faceInfo == null || !StringUtils.hasText(faceInfo.getUserId()) || !StringUtils.hasText(faceInfo.getFaceData())) {
            return AccessGen2OperationResult.failure("userId/faceData 不能为空");
        }
        try {
            byte[] imgBytes = java.util.Base64.getDecoder().decode(faceInfo.getFaceData());
            log.info("{} 添加人脸: userId={}, imageSize={}KB", LOG_PREFIX, faceInfo.getUserId(), imgBytes.length / 1024);

            // 使用 CLIENT_FaceInfoOpreate API（与大华官方Demo一致）
            int emType = EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_ADD;
            
            // 入参
            NET_IN_ADD_FACE_INFO stIn = new NET_IN_ADD_FACE_INFO();
            // 用户ID
            fillBytes(stIn.szUserID, faceInfo.getUserId());
            // 人脸照片个数
            stIn.stuFaceInfo.nFacePhoto = 1;
            // 每张图片的大小
            stIn.stuFaceInfo.nFacePhotoLen[0] = imgBytes.length;
            // 人脸照片数据，大小不超过100K，图片格式为jpg
            Memory imageMemory = new Memory(imgBytes.length);
            imageMemory.clear(imgBytes.length);
            imageMemory.write(0, imgBytes, 0, imgBytes.length);
            stIn.stuFaceInfo.pszFacePhotoArr[0].pszFacePhoto = imageMemory;
            
            // 出参
            NET_OUT_ADD_FACE_INFO stOut = new NET_OUT_ADD_FACE_INFO();
            
            stIn.write();
            stOut.write();
            boolean ok = getNetSdk().CLIENT_FaceInfoOpreate(new LLong(loginHandle), emType, 
                    stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
            stIn.read();
            stOut.read();
            
            if (!ok) {
                return AccessGen2OperationResult.failure("添加人脸失败: " + ToolKits.getErrorCodePrint());
            }
            return AccessGen2OperationResult.success("添加人脸成功");
        } catch (Exception e) {
            log.error("{} 添加人脸异常: userId={}, error={}", LOG_PREFIX, faceInfo.getUserId(), e.getMessage(), e);
            return AccessGen2OperationResult.failure("添加人脸异常: " + e.getMessage());
        }
    }

    /**
     * 删除人脸
     * 
     * @param loginHandle 登录句柄
     * @param userId      用户ID
     * @return 操作结果
     */
    public AccessGen2OperationResult deleteFace(long loginHandle, String userId) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        if (!StringUtils.hasText(userId)) {
            return AccessGen2OperationResult.failure("userId 不能为空");
        }
        try {
            log.info("{} 删除人脸: userId={}", LOG_PREFIX, userId);
            
            // 使用 CLIENT_FaceInfoOpreate API（与大华官方Demo一致）
            int emType = EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_REMOVE;
            
            // 入参
            NET_IN_REMOVE_FACE_INFO stIn = new NET_IN_REMOVE_FACE_INFO();
            // 用户ID
            fillBytes(stIn.szUserID, userId);
            
            // 出参
            NET_OUT_REMOVE_FACE_INFO stOut = new NET_OUT_REMOVE_FACE_INFO();
            
            stIn.write();
            stOut.write();
            boolean ok = getNetSdk().CLIENT_FaceInfoOpreate(new LLong(loginHandle), emType, 
                    stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
            stIn.read();
            stOut.read();
            
            if (!ok) {
                // 删除失败可能是人脸不存在，不算严重错误
                String errorMsg = ToolKits.getErrorCodePrint();
                log.warn("{} 删除人脸失败（可能不存在）: userId={}, error={}", LOG_PREFIX, userId, errorMsg);
                return AccessGen2OperationResult.failure("删除人脸失败: " + errorMsg);
            }
            return AccessGen2OperationResult.success("删除人脸成功");
        } catch (Exception e) {
            log.error("{} 删除人脸异常: userId={}, error={}", LOG_PREFIX, userId, e.getMessage(), e);
            return AccessGen2OperationResult.failure("删除人脸异常: " + e.getMessage());
        }
    }

    // ==================== 指纹管理（未接入，禁止模拟） ====================

    /**
     * 添加指纹
     * 
     * @param loginHandle     登录句柄
     * @param fingerprintInfo 指纹信息
     * @return 操作结果
     */
    public AccessGen2OperationResult addFingerprint(long loginHandle, AccessGen2FingerprintInfo fingerprintInfo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        if (fingerprintInfo == null || !StringUtils.hasText(fingerprintInfo.getUserId())
                || !StringUtils.hasText(fingerprintInfo.getFingerprintData())) {
            return AccessGen2OperationResult.failure("userId/fingerprintData 不能为空");
        }
        try {
            byte[] fpBytes = java.util.Base64.getDecoder().decode(fingerprintInfo.getFingerprintData());
            NET_ACCESS_FINGERPRINT_INFO fp = new NET_ACCESS_FINGERPRINT_INFO();
            fillBytes(fp.szUserID, fingerprintInfo.getUserId());
            fp.nPacketLen = fpBytes.length;
            fp.nPacketNum = 1;
            fp.szFingerPrintInfo = new Memory(fpBytes.length);
            fp.szFingerPrintInfo.clear(fpBytes.length);
            fp.szFingerPrintInfo.write(0, fpBytes, 0, fpBytes.length);
            fp.nDuressIndex = Boolean.TRUE.equals(fingerprintInfo.getIsDuress()) ? 1 : 0;

            NET_IN_ACCESS_FINGERPRINT_SERVICE_INSERT stIn = new NET_IN_ACCESS_FINGERPRINT_SERVICE_INSERT();
            stIn.nFpNum = 1;
            stIn.pFingerPrintInfo = fp.getPointer();

            FAIL_CODE[] failCodes = new FAIL_CODE[] { new FAIL_CODE() };
            NET_OUT_ACCESS_FINGERPRINT_SERVICE_INSERT stOut = new NET_OUT_ACCESS_FINGERPRINT_SERVICE_INSERT();
            stOut.nMaxRetNum = 1;
            stOut.pFailCode = new Memory(failCodes[0].size());
            stOut.pFailCode.clear(failCodes[0].size());
            ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

            fp.write();
            stIn.write();
            stOut.write();
            failCodes[0].write();

            int emType = NET_EM_ACCESS_CTL_FINGERPRINT_SERVICE.NET_EM_ACCESS_CTL_FINGERPRINT_SERVICE_INSERT;
            boolean ok = getNetSdk().CLIENT_OperateAccessFingerprintService(new LLong(loginHandle), emType,
                    stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
            if (!ok) {
                return AccessGen2OperationResult.failure("下发指纹失败: " + ToolKits.getErrorCodePrint());
            }
            ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);
            if (failCodes[0].nFailCode != 0) {
                return AccessGen2OperationResult.failure("下发指纹失败: failCode=" + failCodes[0].nFailCode);
            }
            return AccessGen2OperationResult.success("下发指纹成功");
        } catch (Exception e) {
            log.error("{} 下发指纹异常: userId={}, error={}", LOG_PREFIX, fingerprintInfo.getUserId(), e.getMessage(), e);
            return AccessGen2OperationResult.failure("下发指纹异常: " + e.getMessage());
        }
    }

    /**
     * 删除指纹
     * 
     * @param loginHandle 登录句柄
     * @param userId      用户ID
     * @param fingerIndex 手指索引（null 表示删除所有）
     * @return 操作结果
     */
    public AccessGen2OperationResult deleteFingerprint(long loginHandle, String userId, Integer fingerIndex) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        if (!StringUtils.hasText(userId)) {
            return AccessGen2OperationResult.failure("userId 不能为空");
        }
        try {
            if (fingerIndex != null) {
                // SDK REMOVE 入参不包含 fingerIndex：按文档/结构体定义只能按用户删除
                log.warn("{} 删除指纹：SDK 暂不支持按 fingerIndex 删除，将执行“删除用户全部指纹”。userId={}, fingerIndex={}",
                        LOG_PREFIX, userId, fingerIndex);
            }
            NET_IN_ACCESS_FINGERPRINT_SERVICE_REMOVE stIn = new NET_IN_ACCESS_FINGERPRINT_SERVICE_REMOVE();
            stIn.nUserNum = 1;
            stIn.bUserIDEx = 0;
            stIn.szUserIDs = new USERID[] { new USERID() };
            fillBytes(stIn.szUserIDs[0].szUserID, userId);

            FAIL_CODE[] failCodes = new FAIL_CODE[] { new FAIL_CODE() };
            NET_OUT_ACCESS_FINGERPRINT_SERVICE_REMOVE stOut = new NET_OUT_ACCESS_FINGERPRINT_SERVICE_REMOVE();
            stOut.nMaxRetNum = 1;
            stOut.pFailCode = new Memory(failCodes[0].size());
            stOut.pFailCode.clear(failCodes[0].size());
            ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

            stIn.write();
            stOut.write();
            failCodes[0].write();

            int emType = NET_EM_ACCESS_CTL_FINGERPRINT_SERVICE.NET_EM_ACCESS_CTL_FINGERPRINT_SERVICE_REMOVE;
            boolean ok = getNetSdk().CLIENT_OperateAccessFingerprintService(new LLong(loginHandle), emType,
                    stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
            if (!ok) {
                return AccessGen2OperationResult.failure("删除指纹失败: " + ToolKits.getErrorCodePrint());
            }
            ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);
            if (failCodes[0].nFailCode != 0) {
                return AccessGen2OperationResult.failure("删除指纹失败: failCode=" + failCodes[0].nFailCode);
            }
            return AccessGen2OperationResult.success("删除指纹成功");
        } catch (Exception e) {
            log.error("{} 删除指纹异常: userId={}, error={}", LOG_PREFIX, userId, e.getMessage(), e);
            return AccessGen2OperationResult.failure("删除指纹异常: " + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 验证登录句柄是否有效
     */
    private boolean validateHandle(long loginHandle) {
        if (loginHandle <= 0) {
            log.warn("{} 无效的登录句柄: {}", LOG_PREFIX, loginHandle);
            return false;
        }
        return true;
    }

    /**
     * 设置最后错误信息
     */
    private void setLastError(int errorCode, String errorMessage) {
        lastErrorCode.set(errorCode);
        lastErrorMessage.set(errorMessage);
    }

    /**
     * 获取最后错误码
     */
    public int getLastErrorCode() {
        return lastErrorCode.get();
    }

    /**
     * 获取最后错误信息
     */
    public String getLastErrorMessage() {
        return lastErrorMessage.get();
    }

    /**
     * 填充字节数组
     */
    private void fillBytes(byte[] dest, String src) {
        if (src == null || dest == null) {
            return;
        }
        byte[] srcBytes = src.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int len = Math.min(srcBytes.length, dest.length - 1);
        System.arraycopy(srcBytes, 0, dest, 0, len);
        dest[len] = 0;
    }

    private static String safeString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        String s = new String(bytes, java.nio.charset.StandardCharsets.UTF_8).trim();
        if (!StringUtils.hasText(s)) {
            // 文档示例大量使用 GBK，兜底再尝试一次
            try {
                s = new String(bytes, "GBK").trim();
            } catch (Exception ignored) {
            }
        }
        return s;
    }

    private int[] normalizeDoorIndexes(int[] doors) {
        if (doors == null || doors.length == 0) {
            return new int[0];
        }
        int min = Integer.MAX_VALUE;
        for (int d : doors) {
            min = Math.min(min, d);
        }
        int[] out = new int[doors.length];
        for (int i = 0; i < doors.length; i++) {
            // 若传入最小值 >= 1，视为 1-based，转换为 0-based
            int v = (min >= 1) ? (doors[i] - 1) : doors[i];
            out[i] = Math.max(0, v);
        }
        return out;
    }

    private void fillValidTimeIfPresent(NET_ACCESS_USER_INFO user, String begin, String end) {
        if (user == null) {
            return;
        }
        java.time.LocalDateTime b = parseLocalDateTime(begin);
        java.time.LocalDateTime e = parseLocalDateTime(end);
        if (b != null) {
            user.stuValidBeginTime.dwYear = b.getYear();
            user.stuValidBeginTime.dwMonth = b.getMonthValue();
            user.stuValidBeginTime.dwDay = b.getDayOfMonth();
            user.stuValidBeginTime.dwHour = b.getHour();
            user.stuValidBeginTime.dwMinute = b.getMinute();
            user.stuValidBeginTime.dwSecond = b.getSecond();
        }
        if (e != null) {
            user.stuValidEndTime.dwYear = e.getYear();
            user.stuValidEndTime.dwMonth = e.getMonthValue();
            user.stuValidEndTime.dwDay = e.getDayOfMonth();
            user.stuValidEndTime.dwHour = e.getHour();
            user.stuValidEndTime.dwMinute = e.getMinute();
            user.stuValidEndTime.dwSecond = e.getSecond();
        }
    }

    private static java.time.LocalDateTime parseLocalDateTime(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        String v = s.trim().replace('T', ' ');
        try {
            if (v.length() == 19) { // yyyy-MM-dd HH:mm:ss
                return java.time.LocalDateTime.parse(v.replace(" ", "T"));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<AccessGen2UserInfo> queryUsersByIds(long loginHandle, List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        int n = userIds.size();
        NET_ACCESS_USER_INFO[] users = new NET_ACCESS_USER_INFO[n];
        FAIL_CODE[] failCodes = new FAIL_CODE[n];
        for (int i = 0; i < n; i++) {
            users[i] = new NET_ACCESS_USER_INFO();
            // 使用扩展字段，避免截断
            users[i].bUserIDEx = 1;
            fillBytes(users[i].szUserIDEx, userIds.get(i));
            failCodes[i] = new FAIL_CODE();
        }

        NET_IN_ACCESS_USER_SERVICE_GET stIn = new NET_IN_ACCESS_USER_SERVICE_GET();
        stIn.nUserNum = n;
        for (int i = 0; i < n; i++) {
            fillBytes(stIn.szUserIDs[i].szUserID, userIds.get(i));
        }

        NET_OUT_ACCESS_USER_SERVICE_GET stOut = new NET_OUT_ACCESS_USER_SERVICE_GET();
        stOut.nMaxRetNum = n;
        stOut.pUserInfo = new Memory(users[0].size() * n);
        stOut.pUserInfo.clear(users[0].size() * n);
        stOut.pFailCode = new Memory(failCodes[0].size() * n);
        stOut.pFailCode.clear(failCodes[0].size() * n);
        ToolKits.SetStructArrToPointerData(users, stOut.pUserInfo);
        ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

        stIn.write();
        stOut.write();

        int emType = NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_GET;
        boolean ok = getNetSdk().CLIENT_OperateAccessUserService(new LLong(loginHandle), emType,
                stIn.getPointer(), stOut.getPointer(), TIMEOUT_MS);
        if (!ok) {
            log.warn("{} 查询用户失败: {}", LOG_PREFIX, ToolKits.getErrorCodePrint());
            return Collections.emptyList();
        }
        ToolKits.GetPointerDataToStructArr(stOut.pUserInfo, users);
        ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);

        List<AccessGen2UserInfo> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (failCodes[i].nFailCode != 0) {
                continue;
            }
            result.add(convertSdkUser(users[i]));
        }
        return result;
    }

    private List<AccessGen2UserInfo> queryUsersByFind(long loginHandle, int limit) {
        NET_IN_USERINFO_START_FIND stInFind = new NET_IN_USERINFO_START_FIND();
        NET_OUT_USERINFO_START_FIND stOutFind = new NET_OUT_USERINFO_START_FIND();
        stInFind.write();
        stOutFind.write();
        LLong findHandle = getNetSdk().CLIENT_StartFindUserInfo(new LLong(loginHandle), stInFind, stOutFind, TIMEOUT_MS);
        if (findHandle == null || findHandle.longValue() == 0) {
            log.warn("{} StartFindUserInfo 失败: {}", LOG_PREFIX, ToolKits.getErrorCodePrint());
            return Collections.emptyList();
        }
        int total = stOutFind.nTotalCount;
        int cap = stOutFind.nCapNum;
        int batch = cap == 0 ? 20 : cap;
        int startNo = 0;
        int max = Math.min(limit, total);

        List<AccessGen2UserInfo> result = new ArrayList<>();
        try {
            while (startNo < max) {
                int need = Math.min(batch, max - startNo);
                NET_ACCESS_USER_INFO[] users = new NET_ACCESS_USER_INFO[need];
                for (int i = 0; i < need; i++) {
                    users[i] = new NET_ACCESS_USER_INFO();
                }

                NET_IN_USERINFO_DO_FIND stIn = new NET_IN_USERINFO_DO_FIND();
                stIn.nStartNo = startNo;
                stIn.nCount = need;
                NET_OUT_USERINFO_DO_FIND stOut = new NET_OUT_USERINFO_DO_FIND();
                stOut.nMaxNum = need;
                stOut.pstuInfo = new Memory(users[0].size() * need);
                stOut.pstuInfo.clear(users[0].size() * need);
                ToolKits.SetStructArrToPointerData(users, stOut.pstuInfo);

                stIn.write();
                stOut.write();
                boolean ok = getNetSdk().CLIENT_DoFindUserInfo(findHandle, stIn, stOut, TIMEOUT_MS);
                if (!ok || stOut.nRetNum <= 0) {
                    break;
                }
                ToolKits.GetPointerDataToStructArr(stOut.pstuInfo, users);
                for (int i = 0; i < stOut.nRetNum; i++) {
                    result.add(convertSdkUser(users[i]));
                }
                if (stOut.nRetNum < need) {
                    break;
                }
                startNo += need;
            }
        } finally {
            getNetSdk().CLIENT_StopFindUserInfo(findHandle);
        }
        return result;
    }

    private AccessGen2UserInfo convertSdkUser(NET_ACCESS_USER_INFO sdkUser) {
        String uid = safeString(sdkUser.szUserID);
        String name = safeString(sdkUser.szName);
        String pwd = safeString(sdkUser.szPsw);
        int doorNum = sdkUser.nDoorNum;
        int len = Math.max(0, Math.min(doorNum, sdkUser.nDoors.length));
        int[] doors = new int[len];
        for (int i = 0; i < len; i++) {
            doors[i] = sdkUser.nDoors[i] + 1; // 对外 1-based
        }
        return AccessGen2UserInfo.builder()
                .userId(uid)
                .userName(name)
                .password(pwd)
                .doors(doors)
                .build();
    }

    private static int mapDoorState(int emState) {
        // SDK emState 枚举在不同设备有差异，按最常见约定映射：0-关 1-开 其他-未知
        return emState == 0 ? 0 : (emState == 1 ? 1 : 2);
    }

    // ==================== 门通道查询操作 ====================

    /**
     * 查询门通道信息
     * 
     * <p>从设备获取已配置的门通道信息，包括门编号、门名称、门状态和生物识别能力。</p>
     * <p>门禁二代设备通常有1-4个门，使用默认配置返回门信息。</p>
     * 
     * @param loginHandle 登录句柄
     * @param supportsFace 设备是否支持人脸
     * @param supportsFingerprint 设备是否支持指纹
     * @return 操作结果，成功时 data 中包含 doorList
     */
    public AccessGen2OperationResult queryDoorChannels(long loginHandle, boolean supportsFace, boolean supportsFingerprint) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 查询门通道信息: handle={}", LOG_PREFIX, loginHandle);
            // 通过 CFG_CMD_ACCESS_EVENT 实时读取门禁通道配置（真实设备数据）
            // 注意：不要使用 ToolKits.GetDevConfig（该方法在 SDK 内部会直接 println “Get AccessControl Config Failed”，会污染日志）
            // 这里改为 CLIENT_GetNewDevConfig 取 JSON，再做“尽力解析”，避免 0x80000000|21 的刷屏问题。
            // 说明：文档示例中 nChn 作为通道号；这里以 0..3 扫描作为兜底范围（避免依赖外部通道数）
            int doorCount = 4;
            List<AccessGen2DoorInfo> doorList = new ArrayList<>();
            boolean anyOk = false;
            boolean anyValidateErr = false;
            for (int i = 0; i < doorCount; i++) {
                Optional<AccessEventChannelInfo> chOpt = getAccessEventChannelInfoViaNewDevConfig(loginHandle, i);
                if (chOpt.isEmpty()) {
                    // error code: (0x80000000|21) - 对返回数据的校验出错：多数情况下是通道不存在/设备不支持该通道号读取
                    int err = getNetSdk().CLIENT_GetLastError();
                    if ((err & 0x7FFFFFFF) == 21) {
                        anyValidateErr = true;
                    }
                    continue;
                }
                anyOk = true;
                String name = chOpt.get().channelName;
                doorList.add(AccessGen2DoorInfo.builder()
                        .doorNo(i + 1) // 对外从 1 开始
                        .doorName(StringUtils.hasText(name) ? name : ("门" + (i + 1)))
                        .doorStatus(mapDoorState(chOpt.get().state))
                        .cardSupported(true)
                        .faceSupported(supportsFace)
                        .fingerprintSupported(supportsFingerprint)
                        .build());
            }
            // 若完全读取失败，给一个更明确的提示（并避免每个通道都刷错误日志）
            if (!anyOk) {
                if (anyValidateErr) {
                    log.warn("{} 读取门通道配置失败：设备返回 0x80000000|21（对返回数据的校验出错）。通常是通道号不存在或设备不支持 CFG_CMD_ACCESS_EVENT。", LOG_PREFIX);
                } else {
                    log.warn("{} 读取门通道配置失败：{}", LOG_PREFIX, formatLastSdkError("CLIENT_GetNewDevConfig(CFG_CMD_ACCESS_EVENT)"));
                }
            }
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("doorList", doorList);
            resultData.put("doorCount", doorList.size());
            return AccessGen2OperationResult.success("查询成功", resultData);
            
        } catch (Exception e) {
            log.error("{} 查询门通道异常: handle={}, error={}", LOG_PREFIX, loginHandle, e.getMessage(), e);
            return AccessGen2OperationResult.failure("查询门通道异常: " + e.getMessage());
        }
    }

    /**
     * 使用 CLIENT_GetNewDevConfig 获取门禁通道配置（JSON），避免 ToolKits.GetDevConfig 的 stdout 输出。
     */
    private Optional<AccessEventChannelInfo> getAccessEventChannelInfoViaNewDevConfig(long loginHandle, int channelNo) {
        try {
            byte[] buf = new byte[256 * 1024]; // 256KB 足够门通道配置
            IntByReference errRef = new IntByReference(0);
            boolean ok = getNetSdk().CLIENT_GetNewDevConfig(
                    new LLong(loginHandle),
                    NetSDKLib.CFG_CMD_ACCESS_EVENT,
                    channelNo,
                    buf,
                    buf.length,
                    errRef,
                    TIMEOUT_MS,
                    null
            );
            if (!ok) {
                // 不在这里打 warn，避免循环刷屏；由上层汇总输出一次
                return Optional.empty();
            }

            int len = 0;
            for (int i = 0; i < buf.length; i++) {
                if (buf[i] == 0) { len = i; break; }
            }
            int useLen = (len > 0 ? len : buf.length);
            String json = new String(buf, 0, useLen, StandardCharsets.UTF_8).trim();
            if (!StringUtils.hasText(json)) {
                return Optional.of(new AccessEventChannelInfo(null, 0));
            }

            JsonNode root = JSON_MAPPER.readTree(json);
            String name = findFirstTextIgnoreCase(root, Set.of("channelname", "szchannelname", "name"));
            Integer state = findFirstIntIgnoreCase(root, Set.of("emstate", "state", "status"));
            return Optional.of(new AccessEventChannelInfo(name, state != null ? state : 0));
        } catch (Exception e) {
            // 解析异常不影响整体流程，按“读取失败”处理
            log.debug("{} 解析门通道配置 JSON 失败: channel={}, err={}", LOG_PREFIX, channelNo, e.getMessage());
            return Optional.empty();
        }
    }

    private static String findFirstTextIgnoreCase(JsonNode node, Set<String> keysLower) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String k = e.getKey() != null ? e.getKey().toLowerCase(Locale.ROOT) : "";
                JsonNode v = e.getValue();
                if (keysLower.contains(k) && v != null && v.isTextual()) {
                    String s = v.asText(null);
                    if (StringUtils.hasText(s)) {
                        return s.trim();
                    }
                }
                String nested = findFirstTextIgnoreCase(v, keysLower);
                if (StringUtils.hasText(nested)) {
                    return nested;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode v : node) {
                String nested = findFirstTextIgnoreCase(v, keysLower);
                if (StringUtils.hasText(nested)) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static Integer findFirstIntIgnoreCase(JsonNode node, Set<String> keysLower) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String k = e.getKey() != null ? e.getKey().toLowerCase(Locale.ROOT) : "";
                JsonNode v = e.getValue();
                if (keysLower.contains(k) && v != null && v.isNumber()) {
                    return v.asInt();
                }
                Integer nested = findFirstIntIgnoreCase(v, keysLower);
                if (nested != null) {
                    return nested;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode v : node) {
                Integer nested = findFirstIntIgnoreCase(v, keysLower);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private String formatLastSdkError(String action) {
        int err = 0;
        try {
            err = getNetSdk().CLIENT_GetLastError();
        } catch (Exception ignored) {}
        return action + " 失败，lastError=" + err;
    }

    private static final class AccessEventChannelInfo {
        private final String channelName;
        private final int state;

        private AccessEventChannelInfo(String channelName, int state) {
            this.channelName = channelName;
            this.state = state;
        }
    }

    // ==================== 门控制扩展操作 ====================

    /**
     * 设置门常开
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（从 0 开始）
     * @return 操作结果
     */
    public AccessGen2OperationResult setDoorAlwaysOpen(long loginHandle, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 设置门常开: handle={}, channel={}", LOG_PREFIX, loginHandle, channelNo);
            // 按《智能楼宇分册》：常开/常闭通过 CFG_CMD_ACCESS_EVENT 的 nOpenAlwaysTimeIndex/nCloseAlwaysTimeIndex 配置
            CFG_ACCESS_EVENT_INFO cfg = new CFG_ACCESS_EVENT_INFO();
            if (!ToolKits.GetDevConfig(new LLong(loginHandle), channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen2OperationResult.failure("读取门配置失败: " + ToolKits.getErrorCodePrint());
            }
            // 默认用 255 表示全天有效（时间段 index 需先通过 CFG_CMD_ACCESSTIMESCHEDULE 配置）
            cfg.nOpenAlwaysTimeIndex = 255;
            cfg.nCloseAlwaysTimeIndex = 0;
            if (!ToolKits.SetDevConfig(new LLong(loginHandle), channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen2OperationResult.failure("设置门常开失败: " + ToolKits.getErrorCodePrint());
            }
            // 回读做校验点
            CFG_ACCESS_EVENT_INFO verify = new CFG_ACCESS_EVENT_INFO();
            ToolKits.GetDevConfig(new LLong(loginHandle), channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, verify);
            return AccessGen2OperationResult.success("设置门常开成功", Map.of(
                    "nOpenAlwaysTimeIndex", verify.nOpenAlwaysTimeIndex,
                    "nCloseAlwaysTimeIndex", verify.nCloseAlwaysTimeIndex
            ));
            
        } catch (Exception e) {
            log.error("{} 设置门常开异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return AccessGen2OperationResult.failure("设置常开异常: " + e.getMessage());
        }
    }

    /**
     * 设置门常闭
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（从 0 开始）
     * @return 操作结果
     */
    public AccessGen2OperationResult setDoorAlwaysClosed(long loginHandle, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 设置门常闭: handle={}, channel={}", LOG_PREFIX, loginHandle, channelNo);
            CFG_ACCESS_EVENT_INFO cfg = new CFG_ACCESS_EVENT_INFO();
            if (!ToolKits.GetDevConfig(new LLong(loginHandle), channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen2OperationResult.failure("读取门配置失败: " + ToolKits.getErrorCodePrint());
            }
            cfg.nOpenAlwaysTimeIndex = 0;
            cfg.nCloseAlwaysTimeIndex = 255;
            if (!ToolKits.SetDevConfig(new LLong(loginHandle), channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen2OperationResult.failure("设置门常闭失败: " + ToolKits.getErrorCodePrint());
            }
            CFG_ACCESS_EVENT_INFO verify = new CFG_ACCESS_EVENT_INFO();
            ToolKits.GetDevConfig(new LLong(loginHandle), channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, verify);
            return AccessGen2OperationResult.success("设置门常闭成功", Map.of(
                    "nOpenAlwaysTimeIndex", verify.nOpenAlwaysTimeIndex,
                    "nCloseAlwaysTimeIndex", verify.nCloseAlwaysTimeIndex
            ));
            
        } catch (Exception e) {
            log.error("{} 设置门常闭异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return AccessGen2OperationResult.failure("设置常闭异常: " + e.getMessage());
        }
    }

    /**
     * 取消门常开/常闭状态
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（从 0 开始）
     * @return 操作结果
     */
    public AccessGen2OperationResult cancelDoorAlways(long loginHandle, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen2OperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 取消门常开/常闭: handle={}, channel={}", LOG_PREFIX, loginHandle, channelNo);
            CFG_ACCESS_EVENT_INFO cfg = new CFG_ACCESS_EVENT_INFO();
            if (!ToolKits.GetDevConfig(new LLong(loginHandle), channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen2OperationResult.failure("读取门配置失败: " + ToolKits.getErrorCodePrint());
            }
            cfg.nOpenAlwaysTimeIndex = 0;
            cfg.nCloseAlwaysTimeIndex = 0;
            if (!ToolKits.SetDevConfig(new LLong(loginHandle), channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen2OperationResult.failure("取消门常开/常闭失败: " + ToolKits.getErrorCodePrint());
            }
            CFG_ACCESS_EVENT_INFO verify = new CFG_ACCESS_EVENT_INFO();
            ToolKits.GetDevConfig(new LLong(loginHandle), channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, verify);
            return AccessGen2OperationResult.success("取消门常开/常闭成功", Map.of(
                    "nOpenAlwaysTimeIndex", verify.nOpenAlwaysTimeIndex,
                    "nCloseAlwaysTimeIndex", verify.nCloseAlwaysTimeIndex
            ));
            
        } catch (Exception e) {
            log.error("{} 取消门常开/常闭异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return AccessGen2OperationResult.failure("取消常开/常闭异常: " + e.getMessage());
        }
    }

    // ==================== 事件订阅（按《智能楼宇分册》：EVENT_IVS_ACCESS_CTL） ====================

    /**
     * 订阅门禁事件（带图智能事件回调）
     *
     * @param loginHandle 登录句柄
     * @param deviceId    设备ID（用于回调 dwUser 透传）
     * @param callback    智能事件回调
     * @return 订阅句柄（analyzerHandle），失败返回 null
     */
    public Long subscribeAccessCtlEvent(long loginHandle, long deviceId, NetSDKLib.fAnalyzerDataCallBack callback) {
        if (!validateHandle(loginHandle) || callback == null) {
            return null;
        }
        try {
            // nChannelID：门禁类事件通常使用 0（具体门号在回调 msg.nChannelID 中给出）
            LLong handle = getNetSdk().CLIENT_RealLoadPictureEx(
                    new LLong(loginHandle),
                    0,
                    NetSDKLib.EVENT_IVS_ACCESS_CTL,
                    1,
                    callback,
                    Pointer.createConstant(deviceId),
                    null
            );
            if (handle == null || handle.longValue() == 0) {
                log.warn("{} 订阅门禁事件失败: {}", LOG_PREFIX, ToolKits.getErrorCodePrint());
                return null;
            }
            log.info("{} ✅ 订阅门禁事件成功: loginHandle={}, analyzerHandle={}, deviceId={}",
                    LOG_PREFIX, loginHandle, handle.longValue(), deviceId);
            return handle.longValue();
        } catch (Exception e) {
            log.error("{} 订阅门禁事件异常: deviceId={}, error={}", LOG_PREFIX, deviceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 取消订阅门禁事件
     *
     * @param analyzerHandle 订阅句柄
     * @return 是否成功
     */
    public boolean unsubscribeAccessEvent(long analyzerHandle) {
        if (analyzerHandle <= 0) {
            return true;
        }
        try {
            boolean ok = getNetSdk().CLIENT_StopLoadPic(new LLong(analyzerHandle));
            if (!ok) {
                log.warn("{} 取消订阅失败: analyzerHandle={}, {}", LOG_PREFIX, analyzerHandle, ToolKits.getErrorCodePrint());
            } else {
                log.info("{} ✅ 取消订阅成功: analyzerHandle={}", LOG_PREFIX, analyzerHandle);
            }
            return ok;
        } catch (Exception e) {
            log.error("{} 取消订阅异常: analyzerHandle={}, error={}", LOG_PREFIX, analyzerHandle, e.getMessage(), e);
            return false;
        }
    }
}
