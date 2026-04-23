package cn.iocoder.yudao.module.iot.newgateway.core.handler;

import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceInfo;
import cn.iocoder.yudao.module.iot.newgateway.core.model.CommandResult;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceCommand;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceStatus;

/**
 * 设备处理器接口（基础接口）
 * <p>
 * 所有设备处理器必须实现此接口。定义了设备处理器的核心方法，
 * 包括设备类型识别、命令执行和状态查询。
 * </p>
 *
 * <p>设计原则：</p>
 * <ul>
 *     <li>每个设备插件独立实现此接口，不继承其他插件</li>
 *     <li>通过 {@link #supports(DeviceInfo)} 方法判断是否支持特定设备</li>
 *     <li>通过 {@link #executeCommand(Long, DeviceCommand)} 执行设备命令</li>
 * </ul>
 *
 * <p>实现示例：</p>
 * <pre>
 * {@code
 * @DevicePlugin(id = "alarm", name = "报警主机", deviceType = "ALARM")
 * public class AlarmPlugin implements DeviceHandler {
 *
 *     @Override
 *     public String getDeviceType() {
 *         return "ALARM";
 *     }
 *
 *     @Override
 *     public String getVendor() {
 *         return "Generic";
 *     }
 *
 *     @Override
 *     public boolean supports(DeviceInfo deviceInfo) {
 *         return "ALARM".equalsIgnoreCase(deviceInfo.getDeviceType());
 *     }
 *
 *     @Override
 *     public CommandResult executeCommand(Long deviceId, DeviceCommand command) {
 *         // 实现命令执行逻辑
 *     }
 *
 *     @Override
 *     public DeviceStatus queryStatus(Long deviceId) {
 *         // 实现状态查询逻辑
 *     }
 * }
 * }
 * </pre>
 *
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.core.annotation.DevicePlugin
 */
public interface DeviceHandler {

    /**
     * 获取设备类型
     * <p>
     * 返回此处理器支持的设备类型标识，如 "ALARM"、"CHANGHUI"、"ACCESS_GEN1" 等。
     * 设备类型用于插件注册和路由。
     * </p>
     *
     * @return 设备类型标识
     */
    String getDeviceType();

    /**
     * 获取厂商
     * <p>
     * 返回此处理器支持的设备厂商，如 "Dahua"、"Changhui"、"Generic" 等。
     * 可用于更精细的设备路由。
     * </p>
     *
     * @return 厂商标识
     */
    String getVendor();

    /**
     * 判断是否支持该设备
     * <p>
     * 根据设备信息判断此处理器是否能够处理该设备。
     * 通常基于设备类型和厂商进行判断。
     * </p>
     *
     * @param deviceInfo 设备信息
     * @return 如果支持该设备返回 true，否则返回 false
     */
    boolean supports(DeviceInfo deviceInfo);

    /**
     * 执行设备命令
     * <p>
     * 向指定设备发送命令并返回执行结果。
     * 不同设备类型支持不同的命令类型。
     * </p>
     *
     * <p>常见命令类型：</p>
     * <ul>
     *     <li>报警主机：ARM_ALL, ARM_STAY, DISARM, QUERY_STATUS, BYPASS_ZONE</li>
     *     <li>长辉设备：UPGRADE_TRIGGER, UPGRADE_URL</li>
     *     <li>门禁设备：OPEN_DOOR, CLOSE_DOOR, DISPATCH_AUTH, REVOKE_AUTH</li>
     *     <li>NVR设备：START_RECORDING, STOP_RECORDING, PTZ_CONTROL, QUERY_CHANNELS</li>
     * </ul>
     *
     * @param deviceId 设备ID
     * @param command  设备命令
     * @return 命令执行结果
     */
    CommandResult executeCommand(Long deviceId, DeviceCommand command);

    /**
     * 查询设备状态
     * <p>
     * 查询指定设备的当前状态，包括在线状态、最后活动时间等。
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    DeviceStatus queryStatus(Long deviceId);

    /**
     * 获取插件ID
     * <p>
     * 返回此处理器的唯一标识符，通常与 @DevicePlugin 注解中的 id 一致。
     * </p>
     *
     * @return 插件ID
     */
    default String getPluginId() {
        return getDeviceType().toLowerCase();
    }
}
