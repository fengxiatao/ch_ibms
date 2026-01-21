package cn.iocoder.yudao.module.iot.simulator.protocol.changhui;

import cn.iocoder.yudao.module.iot.simulator.core.SimulatorConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 长辉设备模拟器配置
 * 
 * <p>支持心跳、升级等协议操作的配置</p>
 *
 * @author IoT Simulator Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChanghuiSimulatorConfig extends SimulatorConfig {

    /**
     * 协议类型标识
     */
    public static final String PROTOCOL_TYPE = "CHANGHUI";

    /**
     * 测站编码（20字符十六进制字符串）
     */
    private String stationCode;

    /**
     * 设备类型
     */
    private String deviceType = "INTEGRATED_GATE";

    /**
     * 升级进度上报间隔（毫秒）
     */
    private int progressReportInterval = 1000;

    /**
     * 模拟下载延迟（毫秒）
     */
    private int downloadDelay = 5000;

    /**
     * 升级成功后的新固件版本
     */
    private String newFirmwareVersion = "V2.0.0";

    /**
     * 当前固件版本
     */
    private String currentFirmwareVersion = "V1.0.0";

    /**
     * 失败帧号列表（用于FRAME_FAIL模式）
     */
    private int[] failFrameNumbers;

    /**
     * 是否模拟升级拒绝
     */
    private boolean rejectUpgrade = false;

    /**
     * 升级失败概率（0-100）
     */
    private int failureProbability = 0;

    public ChanghuiSimulatorConfig() {
        setProtocolType(PROTOCOL_TYPE);
        setServerPort(9700); // 默认长辉协议端口
    }

    /**
     * 从测站编码创建配置
     *
     * @param stationCode 测站编码
     * @return 配置实例
     */
    public static ChanghuiSimulatorConfig of(String stationCode) {
        ChanghuiSimulatorConfig config = new ChanghuiSimulatorConfig();
        config.setStationCode(stationCode);
        config.setDeviceId(stationCode);
        return config;
    }
}

