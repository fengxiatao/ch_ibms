package cn.iocoder.yudao.module.iot.simulator;

import cn.iocoder.yudao.module.iot.simulator.core.DeviceSimulator;
import cn.iocoder.yudao.module.iot.simulator.core.SimulatorManager;
import cn.iocoder.yudao.module.iot.simulator.core.SimulatorMode;
import cn.iocoder.yudao.module.iot.simulator.protocol.changhui.ChanghuiDeviceSimulator;
import cn.iocoder.yudao.module.iot.simulator.protocol.changhui.ChanghuiSimulatorConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

/**
 * IoT 设备模拟器应用程序
 * 
 * 支持命令行参数启动：
 * --protocol changhui --station-code xxx --host localhost --port 9700 --mode success
 * 
 * @author Kiro
 */
@Slf4j
@SpringBootApplication
public class SimulatorApplication implements CommandLineRunner {

    @Autowired(required = false)
    private SimulatorManager simulatorManager;

    @Value("${demo.default-station-code:01020304050607080910}")
    private String defaultStationCode;

    @Value("${demo.default-mode:SUCCESS}")
    private String defaultMode;

    @Value("${demo.auto-start:true}")
    private boolean autoStart;

    @Value("${simulator.changhui.server-host:localhost}")
    private String serverHost;

    @Value("${simulator.changhui.server-port:9700}")
    private int serverPort;

    @Value("${simulator.changhui.heartbeat-interval:30}")
    private int heartbeatInterval;

    @Value("${simulator.changhui.progress-report-interval:1000}")
    private int progressReportInterval;

    @Value("${simulator.changhui.download-delay:5000}")
    private int downloadDelay;

    public static void main(String[] args) {
        SpringApplication.run(SimulatorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("  IoT Device Simulator Starting...");
        log.info("========================================");

        // 解析命令行参数
        if (containsHelp(args)) {
            printUsage();
            return;
        }

        // 解析参数（优先使用命令行参数，其次使用配置文件）
        String protocol = getArgValue(args, "--protocol", "changhui");
        String stationCode = getArgValue(args, "--station-code", defaultStationCode);
        String host = getArgValue(args, "--host", serverHost);
        int port = Integer.parseInt(getArgValue(args, "--port", String.valueOf(serverPort)));
        String modeStr = getArgValue(args, "--mode", defaultMode);
        SimulatorMode mode = SimulatorMode.valueOf(modeStr.toUpperCase());

        log.info("Protocol: {}", protocol);
        log.info("Station Code: {}", stationCode);
        log.info("Server: {}:{}", host, port);
        log.info("Mode: {}", mode);
        log.info("Heartbeat Interval: {}s", heartbeatInterval);

        // 根据协议类型启动模拟器
        if ("changhui".equalsIgnoreCase(protocol)) {
            startChanghuiSimulator(stationCode, host, port, mode);
        } else {
            log.error("不支持的协议类型: {}", protocol);
            printUsage();
            return;
        }

        // 保持运行
        log.info("========================================");
        log.info("  模拟器已启动，按 Ctrl+C 停止");
        log.info("========================================");
        
        // 使用 CountDownLatch 保持主线程运行
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("收到停止信号，正在关闭模拟器...");
            latch.countDown();
        }));
        latch.await();
    }

    /**
     * 启动长辉设备模拟器
     */
    private void startChanghuiSimulator(String stationCode, String host, int port, SimulatorMode mode) {
        ChanghuiSimulatorConfig config = new ChanghuiSimulatorConfig();
        config.setStationCode(stationCode);
        config.setDeviceId(stationCode);
        config.setServerHost(host);
        config.setServerPort(port);
        config.setMode(mode);
        config.setHeartbeatInterval(heartbeatInterval);
        config.setProgressReportInterval(progressReportInterval);
        config.setDownloadDelay(downloadDelay);

        // 根据模式设置特殊配置
        if (mode == SimulatorMode.REJECT) {
            config.setRejectUpgrade(true);
        } else if (mode == SimulatorMode.FRAME_FAIL) {
            config.setFailFrameNumbers(new int[]{30, 50, 70}); // 在30%、50%、70%时可能失败
        }

        ChanghuiDeviceSimulator simulator = new ChanghuiDeviceSimulator(config);
        
        // 启动模拟器
        simulator.start().thenRun(() -> {
            log.info("========================================");
            log.info("  长辉设备模拟器已启动");
            log.info("  测站编码: {}", stationCode);
            log.info("  连接服务器: {}:{}", host, port);
            log.info("  运行模式: {}", mode);
            log.info("========================================");
        }).exceptionally(e -> {
            log.error("模拟器启动失败", e);
            return null;
        });
    }

    /**
     * 检查是否包含帮助参数
     */
    private boolean containsHelp(String[] args) {
        for (String arg : args) {
            if ("--help".equals(arg) || "-h".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取参数值
     */
    private String getArgValue(String[] args, String key, String defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (key.equals(args[i])) {
                return args[i + 1];
            }
        }
        return defaultValue;
    }

    /**
     * 打印使用说明
     */
    private void printUsage() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("  IoT 设备模拟器 - 使用说明");
        System.out.println("========================================");
        System.out.println();
        System.out.println("java -jar yudao-module-iot-simulator.jar [options]");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  --protocol <type>        协议类型 (默认: changhui)");
        System.out.println("                           支持: changhui (长辉协议)");
        System.out.println("  --station-code <code>    测站编码，20字符十六进制 (默认: 01020304050607080910)");
        System.out.println("  --host <host>            Gateway服务器地址 (默认: localhost)");
        System.out.println("  --port <port>            Gateway服务器端口 (默认: 9700)");
        System.out.println("  --mode <mode>            运行模式:");
        System.out.println("                           SUCCESS    - 成功模式，正常完成升级");
        System.out.println("                           REJECT     - 拒绝模式，拒绝升级命令");
        System.out.println("                           FRAME_FAIL - 失败模式，升级过程中失败");
        System.out.println("                           TIMEOUT    - 超时模式，响应延迟");
        System.out.println("  --help, -h               显示此帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -jar yudao-module-iot-simulator.jar --protocol changhui --station-code 01020304050607080910");
        System.out.println("  java -jar yudao-module-iot-simulator.jar --host 192.168.1.100 --port 9700 --mode SUCCESS");
        System.out.println();
        System.out.println("使用配置文件启动:");
        System.out.println("  java -jar yudao-module-iot-simulator.jar --spring.profiles.active=changhui");
        System.out.println();
        System.out.println("功能说明:");
        System.out.println("  - 心跳: 自动发送心跳保持在线状态");
        System.out.println("  - 升级: 支持接收升级命令，上报升级进度");
        System.out.println("  - 数据: 支持上报水位等传感器数据");
        System.out.println();
    }
}
