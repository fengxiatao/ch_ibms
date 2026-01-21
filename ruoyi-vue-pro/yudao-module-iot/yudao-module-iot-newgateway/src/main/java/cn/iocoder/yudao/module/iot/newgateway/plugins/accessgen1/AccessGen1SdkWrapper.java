package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1;

import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1CardRecord;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1DoorInfo;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1LoginResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1OperationResult;
import cn.iocoder.yudao.module.iot.newgateway.util.NativeLibraryLoader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.NetSDKLib.*;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 门禁一代设备 SDK 封装
 * 
 * <p>封装大华 NetSDK 的登录/登出和 Recordset 操作，用于门禁一代设备的管理。</p>
 * 
 * <h2>主要功能</h2>
 * <ul>
 *     <li>SDK 初始化和清理</li>
 *     <li>设备登录/登出</li>
 *     <li>远程开门/关门</li>
 *     <li>Recordset 操作（查询、插入、删除门禁卡记录）</li>
 *     <li>通过 recNo 管理卡号/用户</li>
 * </ul>
 * 
 * <h2>Recordset 操作说明</h2>
 * <p>门禁一代设备使用 Recordset 方式管理卡号和用户：</p>
 * <ul>
 *     <li>插入: CLIENT_ControlDevice(CTRLTYPE_CTRL_RECORDSET_INSERT, recordType, recordData)</li>
 *     <li>更新: CLIENT_ControlDevice(CTRLTYPE_CTRL_RECORDSET_UPDATE, recordType, recordData)</li>
 *     <li>删除: CLIENT_ControlDevice(CTRLTYPE_CTRL_RECORDSET_REMOVE, recordType, recordNo)</li>
 *     <li>清空: CLIENT_ControlDevice(CTRLTYPE_CTRL_RECORDSET_CLEAR, recordType)</li>
 *     <li>查询: CLIENT_FindRecord / CLIENT_FindNextRecord</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see AccessGen1Plugin
 * @see AccessGen1ConnectionManager
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "access-gen1", havingValue = "true", matchIfMissing = true)
public class AccessGen1SdkWrapper implements AccessGen1SdkOperations {

    private static final String LOG_PREFIX = "[AccessGen1SdkWrapper]";

    /**
     * 仅用于解析 SDK 返回的 JSON 配置（CLIENT_GetNewDevConfig）
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    /** 
     * SDK 实例 - 使用懒加载模式避免类加载时触发 JNA 原生库加载
     * 这样可以在测试时 mock 此类而不会触发原生库加载
     */
    private static volatile NetSDKLib netsdk;
    
    /** SDK 操作超时时间（毫秒） */
    private static final int TIMEOUT_MS = 10000;
    
    /** 批量查询每批数量 */
    private static final int BATCH_QUERY_SIZE = 10;
    
    /** SDK 是否已初始化 */
    private volatile boolean sdkInitialized = false;
    
    /**
     * 获取 SDK 实例（懒加载）
     * 只有在实际需要使用 SDK 时才会加载原生库
     */
    private static NetSDKLib getNetSdk() {
        if (netsdk == null) {
            synchronized (AccessGen1SdkWrapper.class) {
                if (netsdk == null) {
                    netsdk = NetSDKLib.NETSDK_INSTANCE;
                }
            }
        }
        return netsdk;
    }
    
    /** 线程本地变量存储最后错误信息 */
    private final ThreadLocal<Integer> lastErrorCode = ThreadLocal.withInitial(() -> 0);
    private final ThreadLocal<String> lastErrorMessage = ThreadLocal.withInitial(() -> "");
    
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

    /** 报警消息回调 */
    private final fMessCallBack alarmMessageCallback = new fMessCallBack() {
        @Override
        public boolean invoke(int lCommand, LLong lLoginID, Pointer pStuEvent, int dwBufLen, String strDeviceIP, NativeLong nDevicePort, Pointer dwUser) {
            try {
                long loginHandle = lLoginID != null ? lLoginID.longValue() : 0L;
                int devicePort = nDevicePort != null ? nDevicePort.intValue() : 0;
                log.info("{} 收到报警消息: command={}, loginHandle={}, ip={}", LOG_PREFIX, lCommand, loginHandle, strDeviceIP);
                
                // 根据登录句柄查找设备ID（需要在连接管理器中实现）
                // 这里简单处理，发送给所有监听器
                int alarmType = lCommand;
                int channelNo = 0;
                long alarmTime = System.currentTimeMillis();
                
                Map<String, Object> alarmData = new HashMap<>();
                alarmData.put("command", lCommand);
                alarmData.put("deviceIp", strDeviceIP);
                alarmData.put("devicePort", devicePort);
                
                // 通知所有报警监听器
                for (AlarmEventListener listener : alarmListeners) {
                    try {
                        listener.onAlarm(loginHandle, alarmType, channelNo, alarmTime, alarmData);
                    } catch (Exception e) {
                        log.error("{} 报警监听器处理异常: {}", LOG_PREFIX, e.getMessage(), e);
                    }
                }
                
                return true;
            } catch (Exception e) {
                log.error("{} 报警消息回调处理异常: {}", LOG_PREFIX, e.getMessage(), e);
                return false;
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
            
            // 设置报警消息回调
            getNetSdk().CLIENT_SetDVRMessCallBack(alarmMessageCallback, null);
            log.info("{} 报警消息回调已设置", LOG_PREFIX);
            
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
    public AccessGen1LoginResult login(String ip, int port, String username, String password) {
        if (!sdkInitialized) {
            return AccessGen1LoginResult.failure("SDK 未初始化");
        }
        
        if (!StringUtils.hasText(ip)) {
            return AccessGen1LoginResult.failure("IP 地址不能为空");
        }
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return AccessGen1LoginResult.failure("用户名和密码不能为空");
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
            LLong loginHandle = getNetSdk().CLIENT_LoginWithHighLevelSecurity(loginIn, loginOut);
            
            if (loginHandle.longValue() == 0) {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 登录失败: ip={}, error={}", LOG_PREFIX, ip, errorMsg);
                return AccessGen1LoginResult.failure("登录失败: " + errorMsg);
            }
            
            NET_DEVICEINFO_Ex deviceInfo = loginOut.stuDeviceInfo;
            String serialNumber = new String(deviceInfo.sSerialNumber).trim();
            int channelCount = deviceInfo.byChanNum & 0xFF;
            
            log.info("{} ✅ 登录成功: ip={}, handle={}, sn={}, channels={}", 
                    LOG_PREFIX, ip, loginHandle.longValue(), serialNumber, channelCount);
            
            return AccessGen1LoginResult.success(
                    loginHandle.longValue(),
                    serialNumber,
                    channelCount,
                    deviceInfo.byDVRType
            );
            
        } catch (Exception e) {
            log.error("{} 登录异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return AccessGen1LoginResult.failure("登录异常: " + e.getMessage());
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
            
            boolean result = getNetSdk().CLIENT_Logout(new LLong(loginHandle));
            
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

    // ==================== 门控制操作 ====================

    /**
     * 远程开门
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（从 1 开始，如：1=第一个门，2=第二个门）
     * @return 操作结果
     */
    public AccessGen1OperationResult openDoor(long loginHandle, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen1OperationResult.failure("无效的登录句柄");
        }
        
        try {
            // 根据测试结果和官方文档：
            // - nChannelID 直接使用业务层的 channelNo（从 1 开始）
            // - 官方示例中 nChannelID=0 表示"开全部门"（特殊值），实际门编号从 1 开始
            // - 如果某个门开门失败，需要检查设备的门通道配置
            log.info("{} 远程开门: handle={}, channelNo={}", LOG_PREFIX, loginHandle, channelNo);
            
            NET_CTRL_ACCESS_OPEN openInfo = new NET_CTRL_ACCESS_OPEN();
            openInfo.nChannelID = channelNo;
            openInfo.emOpenDoorType = EM_OPEN_DOOR_TYPE.EM_OPEN_DOOR_TYPE_REMOTE;
            // 使用标准方式：write() + getPointer()
            openInfo.write();
            
            boolean result = getNetSdk().CLIENT_ControlDeviceEx(
                    new LLong(loginHandle),
                    CtrlType.CTRLTYPE_CTRL_ACCESS_OPEN,
                    openInfo.getPointer(),
                    null,
                    TIMEOUT_MS
            );
            openInfo.read();
            
            if (result) {
                log.info("{} ✅ 开门成功: channelNo={}", LOG_PREFIX, channelNo);
                return AccessGen1OperationResult.success("开门成功");
            } else {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 开门失败: channelNo={}, error={}", LOG_PREFIX, channelNo, errorMsg);
                return AccessGen1OperationResult.failure("开门失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("{} 开门异常: channelNo={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return AccessGen1OperationResult.failure("开门异常: " + e.getMessage());
        }
    }

    /**
     * 远程关门
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（从 1 开始，如：1=第一个门，2=第二个门）
     * @return 操作结果
     */
    public AccessGen1OperationResult closeDoor(long loginHandle, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen1OperationResult.failure("无效的登录句柄");
        }
        
        try {
            // 注意：门禁一代设备的 nChannelID 从 1 开始（与业务层的 channelNo 一致）
            log.info("{} 远程关门: handle={}, channelNo={}", LOG_PREFIX, loginHandle, channelNo);
            
            NET_CTRL_ACCESS_CLOSE closeInfo = new NET_CTRL_ACCESS_CLOSE();
            closeInfo.nChannelID = channelNo;
            closeInfo.write();
            
            boolean result = getNetSdk().CLIENT_ControlDeviceEx(
                    new LLong(loginHandle),
                    CtrlType.CTRLTYPE_CTRL_ACCESS_CLOSE,
                    closeInfo.getPointer(),
                    null,
                    TIMEOUT_MS
            );
            closeInfo.read();
            
            if (result) {
                log.info("{} ✅ 关门成功: channelNo={}", LOG_PREFIX, channelNo);
                return AccessGen1OperationResult.success("关门成功");
            } else {
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 关门失败: channelNo={}, error={}", LOG_PREFIX, channelNo, errorMsg);
                return AccessGen1OperationResult.failure("关门失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("{} 关门异常: channelNo={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return AccessGen1OperationResult.failure("关门异常: " + e.getMessage());
        }
    }


    // ==================== Recordset 操作 - 门禁卡记录 ====================

    /**
     * 插入门禁卡记录
     * 
     * @param loginHandle 登录句柄
     * @param record      门禁卡记录
     * @return 操作结果（成功时包含 recNo）
     */
    public AccessGen1OperationResult insertCard(long loginHandle, AccessGen1CardRecord record) {
        if (!validateHandle(loginHandle)) {
            return AccessGen1OperationResult.failure("无效的登录句柄");
        }
        
        if (record == null || !StringUtils.hasText(record.getCardNo())) {
            return AccessGen1OperationResult.failure("卡号不能为空");
        }
        
        try {
            log.info("{} 插入门禁卡记录: handle={}, cardNo={}, userId={}", 
                    LOG_PREFIX, loginHandle, record.getCardNo(), record.getUserId());
            
            // 构建门禁卡记录集信息
            NET_RECORDSET_ACCESS_CTL_CARD accessCardInfo = new NET_RECORDSET_ACCESS_CTL_CARD();
            fillRecordsetCardInfo(accessCardInfo, record);
            
            // 记录集操作 - 添加
            NET_CTRL_RECORDSET_INSERT_PARAM insertParam = new NET_CTRL_RECORDSET_INSERT_PARAM();
            insertParam.stuCtrlRecordSetInfo.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
            insertParam.stuCtrlRecordSetInfo.pBuf = accessCardInfo.getPointer();
            
            accessCardInfo.write();
            insertParam.write();
            
            boolean result = getNetSdk().CLIENT_ControlDevice(
                    new LLong(loginHandle),
                    CtrlType.CTRLTYPE_CTRL_RECORDSET_INSERT,
                    insertParam.getPointer(),
                    TIMEOUT_MS
            );
            
            insertParam.read();
            accessCardInfo.read();
            
            if (result) {
                int recNo = insertParam.stuCtrlRecordSetResult.nRecNo;
                log.info("{} ✅ 插入门禁卡记录成功: cardNo={}, recNo={}", LOG_PREFIX, record.getCardNo(), recNo);
                return AccessGen1OperationResult.success("插入成功", Map.of("recNo", recNo));
            } else {
                int errorCode = getNetSdk().CLIENT_GetLastError();
                // 提取实际错误码（去除 0x80000000 标志位）
                int actualErrorCode = errorCode & 0x7FFFFFFF;
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 插入门禁卡记录失败: cardNo={}, errorCode={}, actualErrorCode={}, msg={}", 
                        LOG_PREFIX, record.getCardNo(), errorCode, actualErrorCode, errorMsg);
                setLastError(actualErrorCode, "插入门禁卡记录失败: " + errorMsg);
                return AccessGen1OperationResult.failure("插入失败: " + errorMsg, actualErrorCode);
            }
            
        } catch (Exception e) {
            log.error("{} 插入门禁卡记录异常: cardNo={}, error={}", LOG_PREFIX, record.getCardNo(), e.getMessage(), e);
            setLastError(-1, "插入门禁卡记录异常: " + e.getMessage());
            return AccessGen1OperationResult.failure("插入异常: " + e.getMessage());
        }
    }

    /**
     * 更新门禁卡记录
     * 
     * @param loginHandle 登录句柄
     * @param record      门禁卡记录（必须包含 recNo）
     * @return 操作结果
     */
    public AccessGen1OperationResult updateCard(long loginHandle, AccessGen1CardRecord record) {
        if (!validateHandle(loginHandle)) {
            return AccessGen1OperationResult.failure("无效的登录句柄");
        }
        
        if (record == null || record.getRecNo() == null || record.getRecNo() < 0) {
            return AccessGen1OperationResult.failure("记录编号不能为空");
        }
        
        try {
            log.info("{} 更新门禁卡记录: handle={}, recNo={}, cardNo={}", 
                    LOG_PREFIX, loginHandle, record.getRecNo(), record.getCardNo());
            
            // 构建门禁卡记录集信息
            NET_RECORDSET_ACCESS_CTL_CARD accessCardInfo = new NET_RECORDSET_ACCESS_CTL_CARD();
            accessCardInfo.nRecNo = record.getRecNo();
            fillRecordsetCardInfo(accessCardInfo, record);
            
            // 记录集操作 - 更新
            NET_CTRL_RECORDSET_PARAM updateParam = new NET_CTRL_RECORDSET_PARAM();
            updateParam.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
            updateParam.pBuf = accessCardInfo.getPointer();
            
            accessCardInfo.write();
            updateParam.write();
            
            boolean result = getNetSdk().CLIENT_ControlDevice(
                    new LLong(loginHandle),
                    CtrlType.CTRLTYPE_CTRL_RECORDSET_UPDATE,
                    updateParam.getPointer(),
                    TIMEOUT_MS
            );
            
            updateParam.read();
            accessCardInfo.read();
            
            if (result) {
                log.info("{} ✅ 更新门禁卡记录成功: recNo={}", LOG_PREFIX, record.getRecNo());
                return AccessGen1OperationResult.success("更新成功");
            } else {
                int errorCode = getNetSdk().CLIENT_GetLastError();
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 更新门禁卡记录失败: recNo={}, errorCode={}, msg={}", 
                        LOG_PREFIX, record.getRecNo(), errorCode, errorMsg);
                setLastError(errorCode, "更新门禁卡记录失败: " + errorMsg);
                return AccessGen1OperationResult.failure("更新失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("{} 更新门禁卡记录异常: recNo={}, error={}", LOG_PREFIX, record.getRecNo(), e.getMessage(), e);
            setLastError(-1, "更新门禁卡记录异常: " + e.getMessage());
            return AccessGen1OperationResult.failure("更新异常: " + e.getMessage());
        }
    }

    /**
     * 删除门禁卡记录（通过 recNo）
     * 
     * @param loginHandle 登录句柄
     * @param recNo       记录编号
     * @return 操作结果
     */
    public AccessGen1OperationResult deleteCardByRecNo(long loginHandle, int recNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen1OperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 删除门禁卡记录: handle={}, recNo={}", LOG_PREFIX, loginHandle, recNo);
            
            // 删除记录集
            NET_CTRL_RECORDSET_PARAM deleteParam = new NET_CTRL_RECORDSET_PARAM();
            deleteParam.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
            deleteParam.pBuf = new IntByReference(recNo).getPointer();
            
            deleteParam.write();
            boolean result = getNetSdk().CLIENT_ControlDevice(
                    new LLong(loginHandle),
                    CtrlType.CTRLTYPE_CTRL_RECORDSET_REMOVE,
                    deleteParam.getPointer(),
                    TIMEOUT_MS
            );
            deleteParam.read();
            
            if (result) {
                log.info("{} ✅ 删除门禁卡记录成功: recNo={}", LOG_PREFIX, recNo);
                return AccessGen1OperationResult.success("删除成功");
            } else {
                int errorCode = getNetSdk().CLIENT_GetLastError();
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 删除门禁卡记录失败: recNo={}, errorCode={}, msg={}", 
                        LOG_PREFIX, recNo, errorCode, errorMsg);
                setLastError(errorCode, "删除门禁卡记录失败: " + errorMsg);
                return AccessGen1OperationResult.failure("删除失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("{} 删除门禁卡记录异常: recNo={}, error={}", LOG_PREFIX, recNo, e.getMessage(), e);
            setLastError(-1, "删除门禁卡记录异常: " + e.getMessage());
            return AccessGen1OperationResult.failure("删除异常: " + e.getMessage());
        }
    }

    /**
     * 删除门禁卡记录（通过卡号）
     * 
     * @param loginHandle 登录句柄
     * @param cardNo      卡号
     * @return 操作结果
     */
    public AccessGen1OperationResult deleteCardByCardNo(long loginHandle, String cardNo) {
        if (!StringUtils.hasText(cardNo)) {
            return AccessGen1OperationResult.failure("卡号不能为空");
        }
        
        // 先查询卡片记录
        AccessGen1CardRecord record = queryCardByCardNo(loginHandle, cardNo);
        if (record == null || record.getRecNo() == null) {
            log.warn("{} 卡片不存在，视为删除成功: cardNo={}", LOG_PREFIX, cardNo);
            return AccessGen1OperationResult.success("卡片不存在");
        }
        
        return deleteCardByRecNo(loginHandle, record.getRecNo());
    }

    /**
     * 清空所有门禁卡记录
     * 
     * @param loginHandle 登录句柄
     * @return 操作结果
     */
    public AccessGen1OperationResult clearAllCards(long loginHandle) {
        if (!validateHandle(loginHandle)) {
            return AccessGen1OperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 清空所有门禁卡记录: handle={}", LOG_PREFIX, loginHandle);
            
            NET_CTRL_RECORDSET_PARAM clearParam = new NET_CTRL_RECORDSET_PARAM();
            clearParam.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
            
            clearParam.write();
            boolean result = getNetSdk().CLIENT_ControlDevice(
                    new LLong(loginHandle),
                    CtrlType.CTRLTYPE_CTRL_RECORDSET_CLEAR,
                    clearParam.getPointer(),
                    TIMEOUT_MS
            );
            clearParam.read();
            
            if (result) {
                log.info("{} ✅ 清空所有门禁卡记录成功", LOG_PREFIX);
                return AccessGen1OperationResult.success("清空成功");
            } else {
                int errorCode = getNetSdk().CLIENT_GetLastError();
                String errorMsg = ToolKits.getErrorCodePrint();
                log.error("{} 清空门禁卡记录失败: errorCode={}, msg={}", LOG_PREFIX, errorCode, errorMsg);
                setLastError(errorCode, "清空门禁卡记录失败: " + errorMsg);
                return AccessGen1OperationResult.failure("清空失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("{} 清空门禁卡记录异常: error={}", LOG_PREFIX, e.getMessage(), e);
            setLastError(-1, "清空门禁卡记录异常: " + e.getMessage());
            return AccessGen1OperationResult.failure("清空异常: " + e.getMessage());
        }
    }


    // ==================== Recordset 查询操作 ====================

    /**
     * 查询所有门禁卡记录
     * 
     * @param loginHandle 登录句柄
     * @return 门禁卡记录列表
     */
    public List<AccessGen1CardRecord> queryAllCards(long loginHandle) {
        List<AccessGen1CardRecord> result = new ArrayList<>();
        
        if (!validateHandle(loginHandle)) {
            return result;
        }
        
        LLong findHandle = new LLong(0);
        
        try {
            log.info("{} 查询所有门禁卡记录: handle={}", LOG_PREFIX, loginHandle);
            
            // 查询入参
            NET_IN_FIND_RECORD_PARAM inParam = new NET_IN_FIND_RECORD_PARAM();
            inParam.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
            
            // 查询出参
            NET_OUT_FIND_RECORD_PARAM outParam = new NET_OUT_FIND_RECORD_PARAM();
            
            if (!getNetSdk().CLIENT_FindRecord(new LLong(loginHandle), inParam, outParam, TIMEOUT_MS)) {
                log.warn("{} 查询门禁卡记录失败: {}", LOG_PREFIX, ToolKits.getErrorCodePrint());
                return result;
            }
            
            findHandle = outParam.lFindeHandle;
            
            // 查询所有记录
            while (true) {
                List<AccessGen1CardRecord> batch = findNextCardBatch(findHandle, BATCH_QUERY_SIZE);
                if (batch.isEmpty()) {
                    break;
                }
                result.addAll(batch);
            }
            
            log.info("{} ✅ 查询门禁卡记录成功: 共{}条", LOG_PREFIX, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("{} 查询门禁卡记录异常: error={}", LOG_PREFIX, e.getMessage(), e);
            setLastError(-1, "查询门禁卡记录异常: " + e.getMessage());
            return result;
        } finally {
            if (findHandle.longValue() != 0) {
                getNetSdk().CLIENT_FindRecordClose(findHandle);
            }
        }
    }

    /**
     * 根据卡号查询门禁卡记录
     * 
     * @param loginHandle 登录句柄
     * @param cardNo      卡号
     * @return 门禁卡记录，不存在返回 null
     */
    public AccessGen1CardRecord queryCardByCardNo(long loginHandle, String cardNo) {
        if (!validateHandle(loginHandle) || !StringUtils.hasText(cardNo)) {
            return null;
        }
        
        LLong findHandle = new LLong(0);
        
        try {
            // 查询条件
            FIND_RECORD_ACCESSCTLCARD_CONDITION findCondition = new FIND_RECORD_ACCESSCTLCARD_CONDITION();
            findCondition.abCardNo = 1;
            fillBytes(findCondition.szCardNo, cardNo);
            
            // 查询入参
            NET_IN_FIND_RECORD_PARAM inParam = new NET_IN_FIND_RECORD_PARAM();
            inParam.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
            inParam.pQueryCondition = findCondition.getPointer();
            
            // 查询出参
            NET_OUT_FIND_RECORD_PARAM outParam = new NET_OUT_FIND_RECORD_PARAM();
            
            findCondition.write();
            if (!getNetSdk().CLIENT_FindRecord(new LLong(loginHandle), inParam, outParam, TIMEOUT_MS)) {
                return null;
            }
            findCondition.read();
            
            findHandle = outParam.lFindeHandle;
            if (findHandle.longValue() == 0) {
                return null;
            }
            
            List<AccessGen1CardRecord> batch = findNextCardBatch(findHandle, 1);
            return batch.isEmpty() ? null : batch.get(0);
            
        } catch (Exception e) {
            log.error("{} 查询门禁卡记录异常: cardNo={}, error={}", LOG_PREFIX, cardNo, e.getMessage(), e);
            return null;
        } finally {
            if (findHandle.longValue() != 0) {
                getNetSdk().CLIENT_FindRecordClose(findHandle);
            }
        }
    }

    /**
     * 根据用户 ID 查询门禁卡记录
     * 
     * @param loginHandle 登录句柄
     * @param userId      用户 ID
     * @return 门禁卡记录列表
     */
    public List<AccessGen1CardRecord> queryCardsByUserId(long loginHandle, String userId) {
        List<AccessGen1CardRecord> result = new ArrayList<>();
        
        if (!validateHandle(loginHandle) || !StringUtils.hasText(userId)) {
            return result;
        }
        
        LLong findHandle = new LLong(0);
        
        try {
            // 查询条件
            FIND_RECORD_ACCESSCTLCARD_CONDITION findCondition = new FIND_RECORD_ACCESSCTLCARD_CONDITION();
            findCondition.abUserID = 1;
            fillBytes(findCondition.szUserID, userId);
            
            // 查询入参
            NET_IN_FIND_RECORD_PARAM inParam = new NET_IN_FIND_RECORD_PARAM();
            inParam.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
            inParam.pQueryCondition = findCondition.getPointer();
            
            // 查询出参
            NET_OUT_FIND_RECORD_PARAM outParam = new NET_OUT_FIND_RECORD_PARAM();
            
            findCondition.write();
            if (!getNetSdk().CLIENT_FindRecord(new LLong(loginHandle), inParam, outParam, TIMEOUT_MS)) {
                return result;
            }
            findCondition.read();
            
            findHandle = outParam.lFindeHandle;
            
            // 查询所有记录
            while (true) {
                List<AccessGen1CardRecord> batch = findNextCardBatch(findHandle, BATCH_QUERY_SIZE);
                if (batch.isEmpty()) {
                    break;
                }
                result.addAll(batch);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("{} 查询门禁卡记录异常: userId={}, error={}", LOG_PREFIX, userId, e.getMessage(), e);
            return result;
        } finally {
            if (findHandle.longValue() != 0) {
                getNetSdk().CLIENT_FindRecordClose(findHandle);
            }
        }
    }

    /**
     * 获取门禁卡记录数量
     * 
     * @param loginHandle 登录句柄
     * @return 记录数量
     */
    public int getCardCount(long loginHandle) {
        List<AccessGen1CardRecord> cards = queryAllCards(loginHandle);
        return cards.size();
    }

    // ==================== 错误信息 ====================

    /**
     * 获取最后一次操作的错误码
     */
    public int getLastErrorCode() {
        return lastErrorCode.get();
    }

    /**
     * 获取最后一次操作的错误信息
     */
    public String getLastErrorMessage() {
        return lastErrorMessage.get();
    }

    // ==================== 私有辅助方法 ====================

    private boolean validateHandle(long loginHandle) {
        if (!sdkInitialized) {
            setLastError(-1, "SDK 未初始化");
            return false;
        }
        
        if (loginHandle == 0) {
            setLastError(-1, "无效的登录句柄");
            return false;
        }
        
        return true;
    }

    private void setLastError(int errorCode, String errorMessage) {
        lastErrorCode.set(errorCode);
        lastErrorMessage.set(errorMessage);
    }

    private List<AccessGen1CardRecord> findNextCardBatch(LLong findHandle, int count) {
        List<AccessGen1CardRecord> result = new ArrayList<>();
        
        try {
            NET_RECORDSET_ACCESS_CTL_CARD[] cards = new NET_RECORDSET_ACCESS_CTL_CARD[count];
            for (int i = 0; i < count; i++) {
                cards[i] = new NET_RECORDSET_ACCESS_CTL_CARD();
            }
            
            NET_IN_FIND_NEXT_RECORD_PARAM nextIn = new NET_IN_FIND_NEXT_RECORD_PARAM();
            nextIn.lFindeHandle = findHandle;
            nextIn.nFileCount = count;
            
            NET_OUT_FIND_NEXT_RECORD_PARAM nextOut = new NET_OUT_FIND_NEXT_RECORD_PARAM();
            nextOut.nMaxRecordNum = count;
            nextOut.pRecordList = new Memory(cards[0].dwSize * count);
            nextOut.pRecordList.clear(cards[0].dwSize * count);
            ToolKits.SetStructArrToPointerData(cards, nextOut.pRecordList);
            
            if (!getNetSdk().CLIENT_FindNextRecord(nextIn, nextOut, TIMEOUT_MS)) {
                return result;
            }
            
            if (nextOut.nRetRecordNum == 0) {
                return result;
            }
            
            ToolKits.GetPointerDataToStructArr(nextOut.pRecordList, cards);
            
            for (int i = 0; i < nextOut.nRetRecordNum; i++) {
                AccessGen1CardRecord record = convertToCardRecord(cards[i]);
                if (record != null) {
                    result.add(record);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("{} 查询下一批记录异常: error={}", LOG_PREFIX, e.getMessage(), e);
            return result;
        }
    }

    private void fillRecordsetCardInfo(NET_RECORDSET_ACCESS_CTL_CARD accessCardInfo, AccessGen1CardRecord record) {
        // 卡号
        fillBytes(accessCardInfo.szCardNo, record.getCardNo());
        
        // 用户 ID
        if (StringUtils.hasText(record.getUserId())) {
            fillBytes(accessCardInfo.szUserID, record.getUserId());
        }
        
        // 卡名（用户名）
        if (StringUtils.hasText(record.getCardName())) {
            try {
                byte[] nameBytes = record.getCardName().getBytes("GBK");
                System.arraycopy(nameBytes, 0, accessCardInfo.szCardName, 0, 
                        Math.min(nameBytes.length, accessCardInfo.szCardName.length - 1));
            } catch (Exception e) {
                fillBytes(accessCardInfo.szCardName, record.getCardName());
            }
        }
        
        // 密码
        if (StringUtils.hasText(record.getPassword())) {
            fillBytes(accessCardInfo.szPsw, record.getPassword());
        }
        
        // 设置开门权限
        if (record.getDoors() != null && record.getDoors().length > 0) {
            accessCardInfo.nDoorNum = Math.min(record.getDoors().length, accessCardInfo.sznDoors.length);
            for (int i = 0; i < accessCardInfo.nDoorNum; i++) {
                accessCardInfo.sznDoors[i] = record.getDoors()[i];
            }
        } else {
            accessCardInfo.nDoorNum = 1;
            accessCardInfo.sznDoors[0] = 0;
        }
        
        // 时间段编号
        if (record.getTimeSections() != null && record.getTimeSections().length > 0) {
            accessCardInfo.nTimeSectionNum = Math.min(record.getTimeSections().length, accessCardInfo.sznTimeSectionNo.length);
            for (int i = 0; i < accessCardInfo.nTimeSectionNum; i++) {
                accessCardInfo.sznTimeSectionNo[i] = record.getTimeSections()[i];
            }
        } else {
            accessCardInfo.nTimeSectionNum = 1;
            accessCardInfo.sznTimeSectionNo[0] = 255;
        }
        
        // 卡状态
        accessCardInfo.emStatus = record.getCardStatus() != null ? record.getCardStatus() : 0;
        
        // 卡类型
        accessCardInfo.emType = record.getCardType() != null ? record.getCardType() : 0;
        
        // 使用次数
        accessCardInfo.nUserTime = record.getUseTimes() != null ? record.getUseTimes() : 0;
        
        // 是否首卡
        accessCardInfo.bFirstEnter = record.getFirstEnter() != null ? record.getFirstEnter() : 0;
        
        // 是否有效
        accessCardInfo.bIsValid = record.getIsValid() != null ? record.getIsValid() : 1;
        
        // 有效期
        if (StringUtils.hasText(record.getValidStartTime())) {
            fillSDKTime(accessCardInfo.stuValidStartTime, record.getValidStartTime());
        }
        if (StringUtils.hasText(record.getValidEndTime())) {
            fillSDKTime(accessCardInfo.stuValidEndTime, record.getValidEndTime());
        }
    }

    private AccessGen1CardRecord convertToCardRecord(NET_RECORDSET_ACCESS_CTL_CARD sdkCard) {
        try {
            return AccessGen1CardRecord.builder()
                    .recNo(sdkCard.nRecNo)
                    .cardNo(new String(sdkCard.szCardNo).trim())
                    .userId(new String(sdkCard.szUserID).trim())
                    .cardName(new String(sdkCard.szCardName, "GBK").trim())
                    .cardStatus(sdkCard.emStatus)
                    .cardType(sdkCard.emType)
                    .useTimes(sdkCard.nUserTime)
                    .firstEnter(sdkCard.bFirstEnter)
                    .isValid(sdkCard.bIsValid)
                    .validStartTime(convertSDKTimeToString(sdkCard.stuValidStartTime))
                    .validEndTime(convertSDKTimeToString(sdkCard.stuValidEndTime))
                    .doorNum(sdkCard.nDoorNum)
                    .doors(Arrays.copyOf(sdkCard.sznDoors, sdkCard.nDoorNum))
                    .timeSections(Arrays.copyOf(sdkCard.sznTimeSectionNo, sdkCard.nTimeSectionNum))
                    .build();
        } catch (Exception e) {
            log.error("{} 转换记录异常: error={}", LOG_PREFIX, e.getMessage());
            return null;
        }
    }

    private void fillSDKTime(NET_TIME sdkTime, String dateTimeStr) {
        if (!StringUtils.hasText(dateTimeStr)) {
            return;
        }
        try {
            String[] parts = dateTimeStr.split(" ");
            String[] dateParts = parts[0].split("-");
            String[] timeParts = parts.length > 1 ? parts[1].split(":") : new String[]{"0", "0", "0"};
            
            sdkTime.dwYear = Integer.parseInt(dateParts[0]);
            sdkTime.dwMonth = Integer.parseInt(dateParts[1]);
            sdkTime.dwDay = Integer.parseInt(dateParts[2]);
            sdkTime.dwHour = Integer.parseInt(timeParts[0]);
            sdkTime.dwMinute = Integer.parseInt(timeParts[1]);
            sdkTime.dwSecond = Integer.parseInt(timeParts[2]);
        } catch (Exception e) {
            log.warn("{} 解析时间失败: {} - {}", LOG_PREFIX, dateTimeStr, e.getMessage());
        }
    }

    private String convertSDKTimeToString(NET_TIME sdkTime) {
        if (sdkTime.dwYear == 0) {
            return null;
        }
        return String.format("%04d-%02d-%02d %02d:%02d:%02d",
                sdkTime.dwYear, sdkTime.dwMonth, sdkTime.dwDay,
                sdkTime.dwHour, sdkTime.dwMinute, sdkTime.dwSecond);
    }

    private static void fillBytes(byte[] dest, String src) {
        Arrays.fill(dest, (byte) 0);
        if (src == null) {
            return;
        }
        byte[] bytes = src.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(dest.length - 1, bytes.length);
        System.arraycopy(bytes, 0, dest, 0, len);
        dest[len] = 0;
    }

    // ==================== 门通道查询操作 ====================

    /**
     * 查询门通道信息
     * 
     * <p>从设备获取已配置的门通道信息，包括门编号、门名称、门状态等。</p>
     * <p>门禁一代设备通常有1-4个门，使用默认配置返回门信息。</p>
     * 
     * @param loginHandle 登录句柄
     * @return 操作结果，成功时 data 中包含 doorList
     */
    public AccessGen1OperationResult queryDoorChannels(long loginHandle) {
        if (!validateHandle(loginHandle)) {
            return AccessGen1OperationResult.failure("无效的登录句柄");
        }
        
        try {
            log.info("{} 查询门通道信息: handle={}", LOG_PREFIX, loginHandle);
            
            List<AccessGen1DoorInfo> doorList = new ArrayList<>();

            // 通过 CFG_CMD_ACCESS_EVENT 实时读取门禁通道配置（真实设备数据，不做内存模拟）
            // 注意：不要使用 ToolKits.GetDevConfig（该方法在 SDK 内部会直接 println “Get AccessControl Config Failed”，会污染日志）
            // 这里改为 CLIENT_GetNewDevConfig 取 JSON，再做“尽力解析”，避免 0x80000000|21 的刷屏问题。
            // 说明：文档示例中 nChn 作为通道号；这里以 0..3 扫描作为兜底范围
            int doorCount = 4;
            boolean anyOk = false;
            boolean anyValidateErr = false;
            for (int i = 0; i < doorCount; i++) {
                Optional<AccessEventChannelInfo> chOpt = getAccessEventChannelInfoViaNewDevConfig(loginHandle, i);
                if (chOpt.isEmpty()) {
                    int err = getNetSdk().CLIENT_GetLastError();
                    if ((err & 0x7FFFFFFF) == 21) {
                        anyValidateErr = true;
                    }
                    log.debug("{} 配置索引 {} 无数据或不存在", LOG_PREFIX, i);
                    continue;
                }
                anyOk = true;
                String name = chOpt.get().channelName;
                // 关键：doorNo 使用 i + 1，与控制接口的 nChannelID 保持一致
                // 根据大华 SDK 文档，nChannelID 从 1 开始（0 是特殊值"全部门"）
                int doorNo = i + 1;
                log.info("{} 发现门通道: 配置索引={}, doorNo={}, name={}", LOG_PREFIX, i, doorNo, name);
                doorList.add(AccessGen1DoorInfo.builder()
                        .doorNo(doorNo)
                        .doorName(StringUtils.hasText(name) ? name : ("门" + doorNo))
                        .doorStatus(mapDoorState(chOpt.get().state))
                        .cardSupported(true)
                        .build());
            }
            if (!anyOk) {
                if (anyValidateErr) {
                    log.warn("{} 读取门通道配置失败：设备返回 0x80000000|21（对返回数据的校验出错）。通常是通道号不存在或设备不支持 CFG_CMD_ACCESS_EVENT。", LOG_PREFIX);
                } else {
                    log.warn("{} 读取门通道配置失败：{}", LOG_PREFIX, formatLastSdkError("CLIENT_GetNewDevConfig(CFG_CMD_ACCESS_EVENT)"));
                }
            }
            
            log.info("{} ✅ 查询门通道成功: 共{}个门", LOG_PREFIX, doorList.size());
            
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("doorList", doorList);
            resultData.put("doorCount", doorList.size());
            
            return AccessGen1OperationResult.success("查询成功", resultData);
            
        } catch (Exception e) {
            log.error("{} 查询门通道异常: handle={}, error={}", LOG_PREFIX, loginHandle, e.getMessage(), e);
            return AccessGen1OperationResult.failure("查询门通道异常: " + e.getMessage());
        }
    }

    private Optional<AccessEventChannelInfo> getAccessEventChannelInfoViaNewDevConfig(long loginHandle, int channelNo) {
        try {
            byte[] buf = new byte[256 * 1024];
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
     * @param channelNo   通道号（从 1 开始，如：1=第一个门，2=第二个门）
     * @return 操作结果
     */
    public AccessGen1OperationResult setDoorAlwaysOpen(long loginHandle, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen1OperationResult.failure("无效的登录句柄");
        }
        
        try {
            // 注意：配置接口（CFG_CMD_ACCESS_EVENT）的通道号从 0 开始，需要 -1 转换
            // （与控制接口 CTRLTYPE_CTRL_ACCESS_OPEN 不同，后者从 1 开始）
            int sdkChannelId = channelNo - 1;
            if (sdkChannelId < 0) {
                sdkChannelId = 0;
            }
            log.info("{} 设置门常开: handle={}, channelNo={}, sdkChannelId={}", LOG_PREFIX, loginHandle, channelNo, sdkChannelId);

            // 按《智能楼宇分册》：常开/常闭通过 CFG_CMD_ACCESS_EVENT 的 nOpenAlwaysTimeIndex/nCloseAlwaysTimeIndex 配置
            CFG_ACCESS_EVENT_INFO cfg = new CFG_ACCESS_EVENT_INFO();
            if (!ToolKits.GetDevConfig(new LLong(loginHandle), sdkChannelId, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen1OperationResult.failure("读取门配置失败: " + ToolKits.getErrorCodePrint());
            }
            // 默认用 255 表示全天有效（时间段 index 需先通过 CFG_CMD_ACCESSTIMESCHEDULE 配置）
            cfg.nOpenAlwaysTimeIndex = 255;
            cfg.nCloseAlwaysTimeIndex = 0;
            if (!ToolKits.SetDevConfig(new LLong(loginHandle), sdkChannelId, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen1OperationResult.failure("设置常开失败: " + ToolKits.getErrorCodePrint());
            }
            CFG_ACCESS_EVENT_INFO verify = new CFG_ACCESS_EVENT_INFO();
            ToolKits.GetDevConfig(new LLong(loginHandle), sdkChannelId, NetSDKLib.CFG_CMD_ACCESS_EVENT, verify);
            return AccessGen1OperationResult.success("设置常开成功", Map.of(
                    "nOpenAlwaysTimeIndex", verify.nOpenAlwaysTimeIndex,
                    "nCloseAlwaysTimeIndex", verify.nCloseAlwaysTimeIndex
            ));
            
        } catch (Exception e) {
            log.error("{} 设置门常开异常: channelNo={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return AccessGen1OperationResult.failure("设置常开异常: " + e.getMessage());
        }
    }

    /**
     * 设置门常闭
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（从 1 开始，如：1=第一个门，2=第二个门）
     * @return 操作结果
     */
    public AccessGen1OperationResult setDoorAlwaysClosed(long loginHandle, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen1OperationResult.failure("无效的登录句柄");
        }
        
        try {
            // 注意：配置接口（CFG_CMD_ACCESS_EVENT）的通道号从 0 开始，需要 -1 转换
            int sdkChannelId = channelNo - 1;
            if (sdkChannelId < 0) {
                sdkChannelId = 0;
            }
            log.info("{} 设置门常闭: handle={}, channelNo={}, sdkChannelId={}", LOG_PREFIX, loginHandle, channelNo, sdkChannelId);

            CFG_ACCESS_EVENT_INFO cfg = new CFG_ACCESS_EVENT_INFO();
            if (!ToolKits.GetDevConfig(new LLong(loginHandle), sdkChannelId, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen1OperationResult.failure("读取门配置失败: " + ToolKits.getErrorCodePrint());
            }
            cfg.nOpenAlwaysTimeIndex = 0;
            cfg.nCloseAlwaysTimeIndex = 255;
            if (!ToolKits.SetDevConfig(new LLong(loginHandle), sdkChannelId, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen1OperationResult.failure("设置常闭失败: " + ToolKits.getErrorCodePrint());
            }
            CFG_ACCESS_EVENT_INFO verify = new CFG_ACCESS_EVENT_INFO();
            ToolKits.GetDevConfig(new LLong(loginHandle), sdkChannelId, NetSDKLib.CFG_CMD_ACCESS_EVENT, verify);
            return AccessGen1OperationResult.success("设置常闭成功", Map.of(
                    "nOpenAlwaysTimeIndex", verify.nOpenAlwaysTimeIndex,
                    "nCloseAlwaysTimeIndex", verify.nCloseAlwaysTimeIndex
            ));
            
        } catch (Exception e) {
            log.error("{} 设置门常闭异常: channelNo={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return AccessGen1OperationResult.failure("设置常闭异常: " + e.getMessage());
        }
    }

    /**
     * 取消门常开/常闭状态
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号（从 1 开始，如：1=第一个门，2=第二个门）
     * @return 操作结果
     */
    public AccessGen1OperationResult cancelDoorAlways(long loginHandle, int channelNo) {
        if (!validateHandle(loginHandle)) {
            return AccessGen1OperationResult.failure("无效的登录句柄");
        }
        
        try {
            // 注意：配置接口（CFG_CMD_ACCESS_EVENT）的通道号从 0 开始，需要 -1 转换
            int sdkChannelId = channelNo - 1;
            if (sdkChannelId < 0) {
                sdkChannelId = 0;
            }
            log.info("{} 取消门常开/常闭: handle={}, channelNo={}, sdkChannelId={}", LOG_PREFIX, loginHandle, channelNo, sdkChannelId);

            CFG_ACCESS_EVENT_INFO cfg = new CFG_ACCESS_EVENT_INFO();
            if (!ToolKits.GetDevConfig(new LLong(loginHandle), sdkChannelId, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen1OperationResult.failure("读取门配置失败: " + ToolKits.getErrorCodePrint());
            }
            cfg.nOpenAlwaysTimeIndex = 0;
            cfg.nCloseAlwaysTimeIndex = 0;
            if (!ToolKits.SetDevConfig(new LLong(loginHandle), sdkChannelId, NetSDKLib.CFG_CMD_ACCESS_EVENT, cfg)) {
                return AccessGen1OperationResult.failure("取消常开/常闭失败: " + ToolKits.getErrorCodePrint());
            }
            CFG_ACCESS_EVENT_INFO verify = new CFG_ACCESS_EVENT_INFO();
            ToolKits.GetDevConfig(new LLong(loginHandle), sdkChannelId, NetSDKLib.CFG_CMD_ACCESS_EVENT, verify);
            return AccessGen1OperationResult.success("取消常开/常闭成功", Map.of(
                    "nOpenAlwaysTimeIndex", verify.nOpenAlwaysTimeIndex,
                    "nCloseAlwaysTimeIndex", verify.nCloseAlwaysTimeIndex
            ));
            
        } catch (Exception e) {
            log.error("{} 取消门常开/常闭异常: channelNo={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return AccessGen1OperationResult.failure("取消常开/常闭异常: " + e.getMessage());
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

    // ==================== 报警事件订阅 ====================

    /**
     * 报警事件监听器接口
     */
    public interface AlarmEventListener {
        /**
         * 收到报警事件回调
         *
         * @param deviceId   设备ID
         * @param alarmType  报警类型
         * @param channelNo  通道号
         * @param alarmTime  报警时间
         * @param alarmData  报警数据
         */
        void onAlarm(long deviceId, int alarmType, int channelNo, long alarmTime, Map<String, Object> alarmData);
    }

    /** 报警监听器列表 */
    private final List<AlarmEventListener> alarmListeners = new CopyOnWriteArrayList<>();

    /**
     * 注册报警事件监听器
     */
    public void addAlarmListener(AlarmEventListener listener) {
        if (listener != null && !alarmListeners.contains(listener)) {
            alarmListeners.add(listener);
            log.info("{} 注册报警监听器: {}", LOG_PREFIX, listener.getClass().getSimpleName());
        }
    }

    /**
     * 移除报警事件监听器
     */
    public void removeAlarmListener(AlarmEventListener listener) {
        if (listener != null) {
            alarmListeners.remove(listener);
        }
    }

    /**
     * 订阅报警事件（通过 CLIENT_StartListenEx）
     *
     * @param loginHandle 登录句柄
     * @param deviceId    设备ID（用于回调透传）
     * @return 是否订阅成功
     */
    public boolean subscribeAlarmEvent(long loginHandle, long deviceId) {
        if (!validateHandle(loginHandle)) {
            return false;
        }
        try {
            // 启动监听报警
            boolean ok = getNetSdk().CLIENT_StartListenEx(new LLong(loginHandle));
            if (!ok) {
                log.warn("{} 订阅报警事件失败: loginHandle={}, {}", LOG_PREFIX, loginHandle, ToolKits.getErrorCodePrint());
                return false;
            }
            log.info("{} ✅ 订阅报警事件成功: loginHandle={}, deviceId={}", LOG_PREFIX, loginHandle, deviceId);
            return true;
        } catch (Exception e) {
            log.error("{} 订阅报警事件异常: deviceId={}, error={}", LOG_PREFIX, deviceId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 取消订阅报警事件
     *
     * @param loginHandle 登录句柄
     * @return 是否成功
     */
    public boolean unsubscribeAlarmEvent(long loginHandle) {
        if (loginHandle <= 0) {
            return true;
        }
        try {
            boolean ok = getNetSdk().CLIENT_StopListen(new LLong(loginHandle));
            if (!ok) {
                log.warn("{} 取消报警订阅失败: loginHandle={}, {}", LOG_PREFIX, loginHandle, ToolKits.getErrorCodePrint());
            } else {
                log.info("{} ✅ 取消报警订阅成功: loginHandle={}", LOG_PREFIX, loginHandle);
            }
            return ok;
        } catch (Exception e) {
            log.error("{} 取消报警订阅异常: loginHandle={}, error={}", LOG_PREFIX, loginHandle, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 通知所有报警监听器
     */
    public void notifyAlarmListeners(long deviceId, int alarmType, int channelNo, long alarmTime, Map<String, Object> alarmData) {
        for (AlarmEventListener listener : alarmListeners) {
            try {
                listener.onAlarm(deviceId, alarmType, channelNo, alarmTime, alarmData);
            } catch (Exception e) {
                log.error("{} 报警监听器处理异常: {}", LOG_PREFIX, e.getMessage(), e);
            }
        }
    }

    private static String safeString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        String s = new String(bytes, java.nio.charset.StandardCharsets.UTF_8).trim();
        if (!StringUtils.hasText(s)) {
            try {
                s = new String(bytes, "GBK").trim();
            } catch (Exception ignored) {
            }
        }
        return s;
    }

    private static int mapDoorState(int emState) {
        return emState == 0 ? 0 : (emState == 1 ? 1 : 2);
    }
}
