package cn.iocoder.yudao.module.iot.service.video;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceGatewaySupportService;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.ibms.device.support.IbmsDeviceLedgerRuntimeHelper;
import cn.iocoder.yudao.module.iot.service.video.IbmsDeviceVideoNetworkResolver;
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
 * <p>职责：</p>
 * <ul>
 *   <li>定时检测 NVR 设备的在线状态</li>
 *   <li>自动更新设备状态（在线/离线），写入 IBMS 台账 extra + 运行态表</li>
 * </ul>
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
    private IbmsDeviceGatewaySupportService ibmsDeviceGatewaySupportService;

    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

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
     */
    @Scheduled(fixedDelayString = "${iot.nvr.heartbeat.interval:300000}", initialDelay = 10000)
    public void checkNvrStatus() {
        try {
            TenantUtils.execute(1L, () -> {
                try {
                    List<IbmsDeviceDO> nvrList = nvrQueryService.getNvrList();

                    if (nvrList.isEmpty()) {
                        log.debug("[NVR心跳] 没有 NVR 设备需要检测");
                        return;
                    }

                    log.debug("[NVR心跳] 开始检测 {} 个 NVR 设备", nvrList.size());

                    int onlineCount = 0;
                    int offlineCount = 0;
                    int unchangedCount = 0;

                    for (IbmsDeviceDO nvr : nvrList) {
                        try {
                            boolean isOnline = checkNvrOnline(nvr);
                            int newState = isOnline ? IotDeviceStateEnum.ONLINE.getState() : IotDeviceStateEnum.OFFLINE.getState();

                            Integer oldState = IbmsDeviceLedgerRuntimeHelper.resolveDeviceState(nvr,
                                    ibmsDeviceRuntimeService.getByDeviceId(nvr.getId()));

                            if (!Objects.equals(oldState, newState)) {
                                ibmsDeviceGatewaySupportService.updateGatewayDeviceStateWithTimestamp(
                                        nvr.getId(), newState, System.currentTimeMillis());

                                String stateStr = isOnline ? "在线" : "离线";
                                String ip = extractIpFromIbms(nvr);
                                log.info("[NVR心跳] ✅ NVR 状态变化: id={}, name={}, ip={}, {} -> {}",
                                        nvr.getId(), nvr.getName(), ip,
                                        getStateStr(oldState), stateStr);

                                if (isOnline) {
                                    onlineCount++;
                                } else {
                                    offlineCount++;
                                }
                            } else {
                                unchangedCount++;
                                log.trace("[NVR心跳] NVR 状态未变化: id={}, name={}, state={}",
                                        nvr.getId(), nvr.getName(), getStateStr(newState));
                            }

                        } catch (Exception e) {
                            log.error("[NVR心跳] ❌ 检测 NVR 失败: id={}, name={}, ip={}, error={}",
                                    nvr.getId(), nvr.getName(), extractIpFromIbms(nvr), e.getMessage());
                        }
                    }

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

    private boolean checkNvrOnline(IbmsDeviceDO nvr) {
        String ip = extractIpFromIbms(nvr);
        Integer tcpPort = extractTcpPortFromIbms(nvr);

        if (StrUtil.isBlank(ip)) {
            log.warn("[NVR心跳] NVR IP 为空，判定为离线: id={}, name={}, extra={}",
                    nvr.getId(), nvr.getName(), nvr.getExtra());
            return false;
        }

        int checkPort = tcpPort != null ? tcpPort : port;

        if (checkTcpPort(ip, checkPort, timeout)) {
            return true;
        }

        return pingHost(ip, timeout);
    }

    private String extractIpFromIbms(IbmsDeviceDO nvr) {
        IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(nvr,
                ibmsDeviceRuntimeService.getByDeviceId(nvr.getId()));
        if (StrUtil.isNotBlank(net.ip)) {
            return net.ip;
        }
        try {
            if (StrUtil.isBlank(nvr.getExtra())) {
                return null;
            }
            JSONObject ex = JSONUtil.parseObj(nvr.getExtra());
            String ip = ex.getStr("ipAddress");
            if (StrUtil.isNotBlank(ip)) {
                return ip;
            }
            ip = ex.getStr("ip");
            if (StrUtil.isNotBlank(ip)) {
                return ip;
            }
            return ex.getStr("host");
        } catch (Exception e) {
            log.error("[NVR心跳] 解析 extra 失败: id={}, error={}",
                    nvr.getId(), e.getMessage());
            return null;
        }
    }

    private Integer extractTcpPortFromIbms(IbmsDeviceDO nvr) {
        var rt = ibmsDeviceRuntimeService.getByDeviceId(nvr.getId());
        DeviceConfig cfg = rt != null ? rt.getConfig() : null;
        try {
            if (cfg == null) {
                return null;
            }
            Map<String, Object> configMap = cfg.toMap();
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

    private boolean checkTcpPort(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            log.trace("[NVR心跳] TCP 端口检测失败: {}:{}, error={}", host, port, e.getMessage());
            return false;
        }
    }

    private boolean pingHost(String host, int timeout) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(timeout);
        } catch (IOException e) {
            log.trace("[NVR心跳] Ping 检测失败: {}, error={}", host, e.getMessage());
            return false;
        }
    }

    private String getStateStr(Integer state) {
        if (state == null) {
            return "未知";
        }
        return state.equals(IotDeviceStateEnum.ONLINE.getState()) ? "在线" : "离线";
    }
}
