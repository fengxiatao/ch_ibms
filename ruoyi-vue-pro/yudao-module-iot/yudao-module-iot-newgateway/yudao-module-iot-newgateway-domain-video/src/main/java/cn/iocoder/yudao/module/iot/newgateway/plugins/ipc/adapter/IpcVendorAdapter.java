package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.adapter;

import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.IpcLoginResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.IpcOperationResult;

import java.util.Map;

/**
 * IPC 厂商适配器接口
 * 
 * <p>定义不同品牌 IPC 设备的统一操作接口，采用策略模式支持多品牌：</p>
 * <ul>
 *     <li>大华（Dahua）- 使用 NetSDK</li>
 *     <li>海康威视（Hikvision）- 使用 HCNetSDK</li>
 *     <li>宇视（Uniview）- 使用 NetSDK</li>
 *     <li>华为（Huawei）- 使用 SDK</li>
 *     <li>ONVIF - 通用协议，作为备选方案</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @since 1.0
 */
public interface IpcVendorAdapter {

    // ==================== 基础信息 ====================

    /**
     * 获取适配器支持的厂商标识
     * 
     * @return 厂商标识，如 "DAHUA", "HIKVISION", "UNIVIEW", "ONVIF"
     */
    String getVendorCode();

    /**
     * 获取适配器名称
     * 
     * @return 适配器名称
     */
    String getVendorName();

    /**
     * 判断是否支持该设备
     * 
     * @param deviceInfo 设备信息（包含 vendor、productKey 等）
     * @return 是否支持
     */
    boolean supports(Map<String, Object> deviceInfo);

    /**
     * 获取适配器优先级（数值越小优先级越高）
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }

    // ==================== SDK 生命周期 ====================

    /**
     * 初始化 SDK
     * 
     * @return 是否初始化成功
     */
    boolean initialize();

    /**
     * 清理 SDK 资源
     */
    void cleanup();

    /**
     * 检查 SDK 是否已初始化
     * 
     * @return 是否已初始化
     */
    boolean isInitialized();

    // ==================== 设备登录/登出 ====================

    /**
     * 登录设备
     * 
     * @param ip       设备IP
     * @param port     设备端口
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    IpcLoginResult login(String ip, int port, String username, String password);

    /**
     * 登出设备
     * 
     * @param loginHandle 登录句柄
     * @return 是否成功
     */
    boolean logout(long loginHandle);

    // ==================== PTZ 控制 ====================

    /**
     * PTZ 云台控制
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号
     * @param ptzCommand  PTZ 命令（UP, DOWN, LEFT, RIGHT, ZOOM_IN, ZOOM_OUT 等）
     * @param speed       速度（1-7）
     * @return 操作结果
     */
    IpcOperationResult ptzControl(long loginHandle, int channelNo, String ptzCommand, int speed);

    /**
     * 预置点操作
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号
     * @param presetIndex 预置点索引
     * @param action      动作（SET=设置, GOTO=转到, CLEAR=删除）
     * @return 操作结果
     */
    IpcOperationResult presetControl(long loginHandle, int channelNo, int presetIndex, String action);

    // ==================== 视频预览 ====================

    /**
     * 构建 RTSP 流地址
     * 
     * @param ip        设备IP
     * @param port      RTSP端口
     * @param username  用户名
     * @param password  密码
     * @param channelNo 通道号
     * @param subtype   码流类型（0=主码流, 1=子码流）
     * @return RTSP URL
     */
    String buildRtspUrl(String ip, int port, String username, String password, int channelNo, int subtype);

    // ==================== 抓图 ====================

    /**
     * 远程抓图
     * 
     * @param loginHandle 登录句柄
     * @param channelNo   通道号
     * @return 操作结果（包含图片数据或URL）
     */
    IpcOperationResult capturePicture(long loginHandle, int channelNo);

    // ==================== 设备信息 ====================

    /**
     * 查询设备信息
     * 
     * @param loginHandle 登录句柄
     * @return 操作结果（包含设备型号、固件版本等）
     */
    IpcOperationResult queryDeviceInfo(long loginHandle);

    /**
     * 查询设备能力
     * 
     * @param loginHandle 登录句柄
     * @return 操作结果（包含 PTZ、音频、AI 等能力）
     */
    IpcOperationResult queryCapabilities(long loginHandle);

    // ==================== 事件订阅 ====================

    /**
     * 订阅设备事件
     * 
     * @param loginHandle   登录句柄
     * @param eventTypes    事件类型列表
     * @param eventCallback 事件回调
     * @return 操作结果
     */
    default IpcOperationResult subscribeEvents(long loginHandle, String[] eventTypes, IpcEventCallback eventCallback) {
        return IpcOperationResult.failure("该适配器不支持事件订阅");
    }

    /**
     * 取消事件订阅
     * 
     * @param loginHandle 登录句柄
     * @return 操作结果
     */
    default IpcOperationResult unsubscribeEvents(long loginHandle) {
        return IpcOperationResult.failure("该适配器不支持事件订阅");
    }

    /**
     * 事件回调接口
     */
    @FunctionalInterface
    interface IpcEventCallback {
        /**
         * 事件回调
         * 
         * @param loginHandle 登录句柄
         * @param eventType   事件类型
         * @param eventData   事件数据
         */
        void onEvent(long loginHandle, String eventType, Map<String, Object> eventData);
    }

    // ==================== 断线回调 ====================

    /**
     * 设置断线回调
     * 
     * @param callback 断线回调
     */
    void setDisconnectCallback(DisconnectCallback callback);

    /**
     * 断线回调接口
     */
    @FunctionalInterface
    interface DisconnectCallback {
        /**
         * 断线回调
         * 
         * @param loginHandle 登录句柄
         * @param ip          设备IP
         * @param port        设备端口
         */
        void onDisconnect(long loginHandle, String ip, int port);
    }
}
