package cn.iocoder.yudao.module.iot.newgateway.core.handler;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import cn.iocoder.yudao.module.iot.newgateway.core.model.LoginResult;

/**
 * 主动连接设备处理器接口
 * <p>
 * 用于平台主动连接设备的场景（如 SDK 登录）。
 * 继承 {@link DeviceHandler}，增加登录、登出和保活方法。
 * </p>
 *
 * <p>适用设备类型：</p>
 * <ul>
 *     <li>大华 NVR/IPC（通过 NetSDK 登录）</li>
 *     <li>大华门禁一代（通过 NetSDK 登录，使用 Recordset 操作）</li>
 *     <li>大华门禁二代（通过 NetSDK 登录，使用标准 API）</li>
 *     <li>其他需要 SDK 登录的设备</li>
 * </ul>
 *
 * <p>生命周期：</p>
 * <ol>
 *     <li>调用 {@link #login(DeviceConnectionInfo)} 登录设备</li>
 *     <li>定期调用 {@link #keepalive(Long)} 保持连接</li>
 *     <li>调用 {@link #logout(Long)} 登出设备</li>
 * </ol>
 *
 * <p>实现示例：</p>
 * <pre>
 * {@code
 * @DevicePlugin(id = "nvr", name = "NVR", deviceType = "NVR")
 * public class NvrPlugin implements ActiveDeviceHandler {
 *
 *     @Override
 *     public LoginResult login(DeviceConnectionInfo connectionInfo) {
 *         // 使用 SDK 登录设备
 *         long handle = sdk.login(connectionInfo.getIpAddress(),
 *                                 connectionInfo.getPort(),
 *                                 connectionInfo.getUsername(),
 *                                 connectionInfo.getPassword());
 *         if (handle > 0) {
 *             return LoginResult.success(handle);
 *         }
 *         return LoginResult.failure("登录失败", sdk.getLastError());
 *     }
 *
 *     @Override
 *     public void logout(Long deviceId) {
 *         Long handle = connectionManager.getConnection(deviceId);
 *         if (handle != null) {
 *             sdk.logout(handle);
 *             connectionManager.unregister(deviceId);
 *         }
 *     }
 *
 *     @Override
 *     public boolean keepalive(Long deviceId) {
 *         Long handle = connectionManager.getConnection(deviceId);
 *         return handle != null && sdk.isConnected(handle);
 *     }
 * }
 * }
 * </pre>
 *
 * @author IoT Gateway Team
 * @see DeviceHandler
 * @see PassiveDeviceHandler
 */
public interface ActiveDeviceHandler extends DeviceHandler {

    /**
     * 获取连接模式
     * <p>
     * 主动连接设备返回 {@link ConnectionMode#ACTIVE}。
     * </p>
     *
     * @return 连接模式（ACTIVE）
     */
    default ConnectionMode getConnectionMode() {
        return ConnectionMode.ACTIVE;
    }

    /**
     * 登录设备
     * <p>
     * 使用提供的连接信息登录设备。登录成功后应将连接信息
     * 注册到 ConnectionManager 中。
     * </p>
     *
     * @param connectionInfo 连接信息（包含 IP、端口、用户名、密码等）
     * @return 登录结果（包含登录句柄或错误信息）
     */
    LoginResult login(DeviceConnectionInfo connectionInfo);

    /**
     * 登出设备
     * <p>
     * 断开与设备的连接，释放相关资源。
     * 应从 ConnectionManager 中注销连接。
     * </p>
     *
     * @param deviceId 设备ID
     */
    void logout(Long deviceId);

    /**
     * 保活检测
     * <p>
     * 检测设备连接是否仍然有效。可用于定期检测连接状态，
     * 如果连接断开则触发重连。
     * </p>
     *
     * @param deviceId 设备ID
     * @return 如果连接有效返回 true，否则返回 false
     */
    boolean keepalive(Long deviceId);

    /**
     * 获取重连间隔（毫秒）
     * <p>
     * 当连接断开时，等待多长时间后尝试重连。
     * 默认 30 秒。
     * </p>
     *
     * @return 重连间隔（毫秒）
     */
    default long getReconnectInterval() {
        return 30000L;
    }

    /**
     * 获取保活检测间隔（毫秒）
     * <p>
     * 多长时间执行一次保活检测。
     * 默认 60 秒。
     * </p>
     *
     * @return 保活检测间隔（毫秒）
     */
    default long getKeepaliveInterval() {
        return 60000L;
    }
}
