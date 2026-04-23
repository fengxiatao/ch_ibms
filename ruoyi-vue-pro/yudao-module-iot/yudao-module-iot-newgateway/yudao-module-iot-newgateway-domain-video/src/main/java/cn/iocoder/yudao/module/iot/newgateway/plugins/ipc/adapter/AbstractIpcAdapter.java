package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.adapter;

import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.IpcLoginResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.IpcOperationResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IPC 厂商适配器抽象基类
 * 
 * <p>提供通用的实现和工具方法，具体品牌适配器继承此类</p>
 * 
 * @author IoT Gateway Team
 * @since 1.0
 */
@Slf4j
public abstract class AbstractIpcAdapter implements IpcVendorAdapter {

    protected volatile boolean initialized = false;
    
    protected DisconnectCallback disconnectCallback;
    
    /** 登录句柄与设备IP的映射 */
    protected final Map<Long, String> handleIpMap = new ConcurrentHashMap<>();
    
    /** 登录句柄与设备端口的映射 */
    protected final Map<Long, Integer> handlePortMap = new ConcurrentHashMap<>();

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void setDisconnectCallback(DisconnectCallback callback) {
        this.disconnectCallback = callback;
    }

    /**
     * 通知断线
     */
    protected void notifyDisconnect(long loginHandle, String ip, int port) {
        if (disconnectCallback != null) {
            try {
                disconnectCallback.onDisconnect(loginHandle, ip, port);
            } catch (Exception e) {
                log.error("[{}] 断线回调异常: {}", getVendorCode(), e.getMessage(), e);
            }
        }
    }

    /**
     * 注册登录信息
     */
    protected void registerLoginInfo(long loginHandle, String ip, int port) {
        handleIpMap.put(loginHandle, ip);
        handlePortMap.put(loginHandle, port);
    }

    /**
     * 移除登录信息
     */
    protected void removeLoginInfo(long loginHandle) {
        handleIpMap.remove(loginHandle);
        handlePortMap.remove(loginHandle);
    }

    /**
     * 获取登录的IP
     */
    protected String getLoginIp(long loginHandle) {
        return handleIpMap.get(loginHandle);
    }

    /**
     * 获取登录的端口
     */
    protected Integer getLoginPort(long loginHandle) {
        return handlePortMap.get(loginHandle);
    }

    @Override
    public IpcOperationResult presetControl(long loginHandle, int channelNo, int presetIndex, String action) {
        // 默认实现，子类可覆盖
        return IpcOperationResult.failure("预置点控制功能尚未实现");
    }

    @Override
    public IpcOperationResult queryDeviceInfo(long loginHandle) {
        // 默认实现，子类可覆盖
        return IpcOperationResult.failure("设备信息查询功能尚未实现");
    }

    @Override
    public IpcOperationResult queryCapabilities(long loginHandle) {
        // 默认实现，子类可覆盖
        return IpcOperationResult.failure("设备能力查询功能尚未实现");
    }

    /**
     * 填充字节数组（用于 SDK 调用）
     */
    protected void fillBytes(byte[] dest, String src) {
        if (src == null || dest == null) {
            return;
        }
        byte[] srcBytes = src.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int len = Math.min(srcBytes.length, dest.length - 1);
        System.arraycopy(srcBytes, 0, dest, 0, len);
        dest[len] = 0;
    }

    /**
     * 从字节数组提取字符串
     */
    protected String extractString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        int len = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                len = i;
                break;
            }
            len = bytes.length;
        }
        return new String(bytes, 0, len, java.nio.charset.StandardCharsets.UTF_8).trim();
    }
}
