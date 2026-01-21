package cn.iocoder.yudao.module.iot.service.video;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.video.nvr.NvrQueryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * NVR 心跳检测服务
 * 
 * 职责：
 * - 定时检测 NVR 设备的在线状态
 * - 自动更新设备状态（在线/离线）
 * - 触发 WebSocket 推送
 * 
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "iot.nvr.heartbeat", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NvrHeartbeatService {

    @Resource
    private NvrQueryService nvrQueryService;
    
    @Resource
    private IotDeviceService deviceService;
    
    /**
     * 检测间隔（毫秒），默认 5 分钟
     */
    @Value("${iot.nvr.heartbeat.interval:300000}")
    private long checkInterval;
    
    /**
     * 超时时间（毫秒），默认 2 秒
     */
    @Value("${iot.nvr.heartbeat.timeout:2000}")
    private int timeout;
    
    /**
     * 检测端口，默认 37777（大华 NVR 默认端口）
     */
    @Value("${iot.nvr.heartbeat.port:37777}")
    private int port;
    
    /**
     * 定时检测 NVR 在线状态
     * 
     * 配置说明：
     * - fixedDelayString: 从配置文件读取间隔时间
     * - initialDelay: 应用启动后，延迟10秒首次执行
     * 
     * 推荐配置：
     * - 测试环境：30秒 (30000)
     * - 小型项目：1分钟 (60000)
     * - 中型项目：2-5分钟 (120000-300000) ✅ 推荐
     * - 大型项目：10分钟 (600000)
     */
    @Scheduled(fixedDelayString = "${iot.nvr.heartbeat.interval:300000}", initialDelay = 10000)
    public void checkNvrStatus() {
        try {
            // 在租户上下文中执行（默认租户ID = 1）
            // TODO: 未来如需支持多租户，需要遍历所有租户
            TenantUtils.execute(1L, () -> {
                try {
                    // 1. 获取所有 NVR 设备
                    List<IotDeviceDO> nvrList = nvrQueryService.getNvrList();
                    
                    if (nvrList.isEmpty()) {
                        log.debug("[NVR心跳] 没有 NVR 设备需要检测");
                        return;
                    }
                    
                    log.debug("[NVR心跳] 开始检测 {} 个 NVR 设备", nvrList.size());
                    
                    int onlineCount = 0;
                    int offlineCount = 0;
                    int unchangedCount = 0;
                    
                    // 2. 逐个检测 NVR 状态
                    for (IotDeviceDO nvr : nvrList) {
                        try {
                            // 2.1 检测在线状态
                            boolean isOnline = checkNvrOnline(nvr);
                            int newState = isOnline ? IotDeviceStateEnum.ONLINE.getState() : IotDeviceStateEnum.OFFLINE.getState();
                            
                            // 2.2 如果状态发生变化，更新数据库
                            if (!Objects.equals(nvr.getState(), newState)) {
                                deviceService.updateDeviceState(nvr.getId(), newState);
                                
                                String stateStr = isOnline ? "在线" : "离线";
                                String ip = extractIpFromConfig(nvr);
                                log.info("[NVR心跳] ✅ NVR 状态变化: id={}, name={}, ip={}, {} -> {}", 
                                        nvr.getId(), nvr.getDeviceName(), ip, 
                                        getStateStr(nvr.getState()), stateStr);
                                
                                if (isOnline) {
                                    onlineCount++;
                                } else {
                                    offlineCount++;
                                }
                            } else {
                                unchangedCount++;
                                log.trace("[NVR心跳] NVR 状态未变化: id={}, name={}, state={}", 
                                        nvr.getId(), nvr.getDeviceName(), getStateStr(newState));
                            }
                            
                        } catch (Exception e) {
                            log.error("[NVR心跳] ❌ 检测 NVR 失败: id={}, name={}, ip={}, error={}", 
                                    nvr.getId(), nvr.getDeviceName(), DeviceConfigHelper.getIpAddress(nvr), e.getMessage());
                        }
                    }
                    
                    // 3. 输出统计日志
                    if (onlineCount > 0 || offlineCount > 0) {
                        log.info("[NVR心跳] 检测完成: 总数={}, 上线={}, 下线={}, 未变化={}", 
                                nvrList.size(), onlineCount, offlineCount, unchangedCount);
                    }
                    
                } catch (Exception e) {
                    log.error("[NVR心跳] ❌ 检测任务执行失败（租户1）: {}", e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            log.error("[NVR心跳] ❌ 检测任务执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 检测 NVR 是否在线
     * 
     * 检测方式：
     * 1. 优先使用 TCP 端口检测（37777端口，大华默认端口）
     * 2. 如果 TCP 检测失败，使用 ICMP Ping 检测
     * 
     * @param nvr NVR 设备信息
     * @return true=在线，false=离线
     */
    private boolean checkNvrOnline(IotDeviceDO nvr) {
        // 从 config JSON 中提取 IP 和端口
        String ip = extractIpFromConfig(nvr);
        Integer tcpPort = extractTcpPortFromConfig(nvr);
        
        if (StrUtil.isBlank(ip)) {
            log.warn("[NVR心跳] NVR IP 为空，判定为离线: id={}, name={}, config={}", 
                    nvr.getId(), nvr.getDeviceName(), nvr.getConfig());
            return false;
        }
        
        // 使用从 config 中提取的端口，如果没有则使用默认端口
        int checkPort = tcpPort != null ? tcpPort : port;
        
        // 方式1：TCP 端口检测（更准确，检测服务是否可用）
        if (checkTcpPort(ip, checkPort, timeout)) {
            return true;
        }
        
        // 方式2：ICMP Ping 检测（备用方案）
        return pingHost(ip, timeout);
    }
    
    /**
     * 从设备信息中提取 IP 地址
     * 
     * <p>使用 DeviceConfigHelper 从 config 中提取 IP 地址
     * 
     * @param nvr NVR 设备信息
     * @return IP 地址，如果提取失败返回 null
     */
    private String extractIpFromConfig(IotDeviceDO nvr) {
        // 使用 DeviceConfigHelper 从 config 中获取 IP 地址
        String ipAddress = DeviceConfigHelper.getIpAddress(nvr);
        if (StrUtil.isNotBlank(ipAddress)) {
            return ipAddress;
        }
        
        // 兼容：从 config.toMap() 中提取（旧数据格式）
        try {
            if (nvr.getConfig() == null) {
                return null;
            }
            
            Map<String, Object> configMap = nvr.getConfig().toMap();
            // 尝试 ipAddress 字段
            Object ipObj = configMap.get("ipAddress");
            if (ipObj != null && StrUtil.isNotBlank(ipObj.toString())) {
                return ipObj.toString();
            }
            // 兼容旧的 ip 字段
            ipObj = configMap.get("ip");
            return ipObj != null ? ipObj.toString() : null;
        } catch (Exception e) {
            log.error("[NVR心跳] 解析 config 失败: id={}, error={}", 
                    nvr.getId(), e.getMessage());
            return null;
        }
    }
    
    /**
     * 从 config JSON 中提取 TCP 端口
     * 
     * @param nvr NVR 设备信息
     * @return TCP 端口，如果提取失败返回 null
     */
    private Integer extractTcpPortFromConfig(IotDeviceDO nvr) {
        try {
            if (nvr.getConfig() == null) {
                return null;
            }
            
            Map<String, Object> configMap = nvr.getConfig().toMap();
            Object portObj = configMap.get("tcpPort");
            if (portObj == null) {
                return null;
            }
            return portObj instanceof Integer ? (Integer) portObj : Integer.parseInt(portObj.toString());
        } catch (Exception e) {
            log.trace("[NVR心跳] 解析 tcpPort 失败，使用默认端口: id={}", nvr.getId());
            return null;
        }
    }
    
    /**
     * 检测 TCP 端口是否可达
     * 
     * @param host 主机地址
     * @param port 端口号
     * @param timeout 超时时间（毫秒）
     * @return true=可达，false=不可达
     */
    private boolean checkTcpPort(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            log.trace("[NVR心跳] TCP 端口检测失败: {}:{}, error={}", host, port, e.getMessage());
            return false;
        }
    }
    
    /**
     * Ping 主机
     * 
     * @param host 主机地址
     * @param timeout 超时时间（毫秒）
     * @return true=可达，false=不可达
     */
    private boolean pingHost(String host, int timeout) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(timeout);
        } catch (IOException e) {
            log.trace("[NVR心跳] Ping 检测失败: {}, error={}", host, e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取状态字符串
     */
    private String getStateStr(Integer state) {
        if (state == null) {
            return "未知";
        }
        return state.equals(IotDeviceStateEnum.ONLINE.getState()) ? "在线" : "离线";
    }
}
