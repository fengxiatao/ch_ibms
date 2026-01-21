package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.adapter;

import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.IpcLoginResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.IpcOperationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ONVIF 通用 IPC 适配器
 * 
 * <p>使用 ONVIF 协议实现通用 IPC 设备的接入，作为其他品牌的备选方案</p>
 * 
 * <p>ONVIF 是一个开放的行业标准，大多数 IPC 设备都支持</p>
 * 
 * @author IoT Gateway Team
 * @since 1.0
 */
@Slf4j
@Component
public class OnvifIpcAdapter extends AbstractIpcAdapter {

    private static final String LOG_PREFIX = "[OnvifIpcAdapter]";
    
    public static final String VENDOR_CODE = "ONVIF";
    public static final String VENDOR_NAME = "ONVIF通用";

    /** 模拟登录句柄生成器 */
    private final AtomicLong handleGenerator = new AtomicLong(200000);

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
            if (vendorStr.contains("ONVIF") || vendorStr.contains("通用") || vendorStr.contains("GENERIC")) {
                return true;
            }
        }
        
        // 通过 protocol 字段判断
        Object protocol = deviceInfo.get("protocol");
        if (protocol != null && "ONVIF".equalsIgnoreCase(protocol.toString())) {
            return true;
        }
        
        // ONVIF 作为兜底方案，始终返回 true（但优先级最低）
        return true;
    }

    @Override
    public int getPriority() {
        return 1000; // 最低优先级，作为兜底
    }

    // ==================== SDK 生命周期 ====================

    @Override
    public boolean initialize() {
        if (initialized) {
            log.info("{} 已初始化，跳过", LOG_PREFIX);
            return true;
        }
        
        try {
            log.info("{} 正在初始化 ONVIF 客户端...", LOG_PREFIX);
            
            // ONVIF 不需要初始化 SDK，直接通过 HTTP/SOAP 通信
            
            initialized = true;
            log.info("{} ✅ ONVIF 客户端初始化成功", LOG_PREFIX);
            return true;
            
        } catch (Exception e) {
            log.error("{} 初始化异常: {}", LOG_PREFIX, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void cleanup() {
        log.info("{} 正在清理 ONVIF 资源...", LOG_PREFIX);
        initialized = false;
        handleIpMap.clear();
        handlePortMap.clear();
    }

    // ==================== 设备登录/登出 ====================

    @Override
    public IpcLoginResult login(String ip, int port, String username, String password) {
        if (!initialized) {
            if (!initialize()) {
                return IpcLoginResult.failure("ONVIF 客户端未初始化");
            }
        }
        
        if (!StringUtils.hasText(ip)) {
            return IpcLoginResult.failure("IP 地址不能为空");
        }
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return IpcLoginResult.failure("用户名和密码不能为空");
        }
        
        try {
            log.info("{} 连接设备: ip={}, port={}", LOG_PREFIX, ip, port);
            
            // TODO: 实际使用时通过 ONVIF 协议连接设备
            // 1. 发现设备（GetServices）
            // 2. 获取设备信息（GetDeviceInformation）
            // 3. 获取媒体配置（GetProfiles）
            // 4. 获取流 URI（GetStreamUri）
            
            // 模拟登录成功
            long handle = handleGenerator.incrementAndGet();
            String serialNumber = "ONVIF_" + ip.replace(".", "") + "_" + System.currentTimeMillis();
            
            // 注册登录信息
            registerLoginInfo(handle, ip, port);
            
            log.info("{} ✅ 连接成功（模拟）: ip={}, handle={}", LOG_PREFIX, ip, handle);
            
            IpcLoginResult result = IpcLoginResult.success(handle, serialNumber, "ONVIF Device", "1.0", 1);
            result.setPtzSupported(true);  // ONVIF 大多支持 PTZ
            result.setAudioSupported(true);
            
            return result;
            
        } catch (Exception e) {
            log.error("{} 连接异常: ip={}, error={}", LOG_PREFIX, ip, e.getMessage(), e);
            return IpcLoginResult.failure("连接异常: " + e.getMessage());
        }
    }

    @Override
    public boolean logout(long loginHandle) {
        if (loginHandle == 0) {
            return true;
        }
        
        try {
            log.info("{} 断开连接: handle={}", LOG_PREFIX, loginHandle);
            
            // ONVIF 无状态，无需登出操作
            
            // 移除登录信息
            removeLoginInfo(loginHandle);
            
            log.info("{} ✅ 断开成功: handle={}", LOG_PREFIX, loginHandle);
            return true;
            
        } catch (Exception e) {
            log.error("{} 断开异常: handle={}, error={}", LOG_PREFIX, loginHandle, e.getMessage(), e);
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
            
            // TODO: 实际使用时通过 ONVIF PTZ 接口控制
            // 1. 获取 PTZ 配置（GetConfigurations）
            // 2. 执行 PTZ 控制（ContinuousMove / AbsoluteMove / RelativeMove）
            // 3. 停止 PTZ（Stop）
            
            String ip = getLoginIp(loginHandle);
            if (ip == null) {
                return IpcOperationResult.failure("设备未连接");
            }
            
            // 模拟 PTZ 控制
            log.info("{} ✅ PTZ控制成功（模拟）: cmd={}", LOG_PREFIX, ptzCommand);
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
        
        // ONVIF 通用 RTSP 格式（不同厂商可能不同，这里使用常见格式）
        // 尝试使用大华格式作为默认（兼容性较好）
        if (channelNo <= 0) {
            channelNo = 1;
        }
        
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
            
            // TODO: 实际使用时通过 ONVIF GetSnapshotUri 获取抓图地址
            // 然后通过 HTTP 请求获取图片
            
            String ip = getLoginIp(loginHandle);
            if (ip == null) {
                return IpcOperationResult.failure("设备未连接");
            }
            
            // 构建抓图 URL（ONVIF 标准格式）
            String snapshotUrl = String.format("http://%s/onvif-http/snapshot?channel=%d", ip, channelNo);
            
            return IpcOperationResult.success("抓图 URL 已生成", Map.of(
                    "channelNo", channelNo,
                    "snapshotUrl", snapshotUrl,
                    "message", "请通过 HTTP GET 请求获取图片"
            ));
            
        } catch (Exception e) {
            log.error("{} 抓图异常: channel={}, error={}", LOG_PREFIX, channelNo, e.getMessage(), e);
            return IpcOperationResult.failure("抓图异常: " + e.getMessage());
        }
    }
}
