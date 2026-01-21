package cn.iocoder.yudao.module.iot.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT WebSocket 消息基类
 *
 * @author 芋道源码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotMessage {

    /**
     * 消息类型
     * - connected: 连接确认
     * - pong: 心跳响应
     * - device_status: 设备状态更新
     * - alarm_event: 告警事件
     * - device_stats: 设备统计数据
     * - snapshot_update: 快照更新
     * - service_failure: 服务失败
     * - recording_event: 录像事件
     */
    private String type;

    /**
     * 消息数据（根据 type 不同，data 类型也不同）
     */
    private Object data;

    /**
     * 消息时间戳
     */
    private Long timestamp;

    /**
     * 创建连接确认消息
     */
    public static IotMessage connected() {
        return new IotMessage("connected", null, System.currentTimeMillis());
    }

    /**
     * 创建心跳响应消息
     */
    public static IotMessage pong() {
        return new IotMessage("pong", null, System.currentTimeMillis());
    }

    /**
     * 创建设备状态更新消息
     */
    public static IotMessage deviceStatus(DeviceStatusMessage data) {
        return new IotMessage("device_status", data, System.currentTimeMillis());
    }

    /**
     * 创建告警事件消息
     */
    public static IotMessage alarmEvent(AlarmEventMessage data) {
        return new IotMessage("alarm_event", data, System.currentTimeMillis());
    }

    /**
     * 创建设备统计数据消息
     */
    public static IotMessage deviceStats(DeviceStatsMessage data) {
        return new IotMessage("device_stats", data, System.currentTimeMillis());
    }

    /**
     * 创建快照更新消息
     */
    public static IotMessage snapshotUpdate(Object data) {
        return new IotMessage("snapshot_update", data, System.currentTimeMillis());
    }

    /**
     * 创建服务失败消息
     */
    public static IotMessage serviceFailure(ServiceFailureMessage data) {
        return new IotMessage("service_failure", data, System.currentTimeMillis());
    }

    public static IotMessage recordingEvent(Object data) {
        return new IotMessage("recording_event", data, System.currentTimeMillis());
    }

    /**
     * 创建报警主机状态更新消息
     */
    public static IotMessage alarmHostStatus(AlarmHostStatusMessage data) {
        return new IotMessage("alarm_host_status", data, System.currentTimeMillis());
    }

    /**
     * 创建门禁设备状态更新消息
     */
    public static IotMessage accessDeviceStatus(AccessDeviceStatusMessage data) {
        return new IotMessage("access_device_status", data, System.currentTimeMillis());
    }

    /**
     * 创建门禁事件消息
     * 消息类型设置为"access_event"
     * Requirements: 4.3
     */
    public static IotMessage accessEvent(AccessEventMessage data) {
        return new IotMessage("access_event", data, System.currentTimeMillis());
    }

    /**
     * 创建门禁事件推送消息（从事件日志转换）
     * 消息类型设置为"access_event"
     * Requirements: 4.2, 4.3
     */
    public static IotMessage accessEventPush(AccessEventPushMessage data) {
        return new IotMessage("access_event", data, System.currentTimeMillis());
    }

    /**
     * 创建授权任务进度消息
     * Requirements: 6.2
     */
    public static IotMessage authTaskProgress(AuthTaskProgressMessage data) {
        return new IotMessage("auth_task_progress", data, System.currentTimeMillis());
    }

    /**
     * 创建授权任务完成消息
     */
    public static IotMessage authTaskCompleted(AuthTaskProgressMessage data) {
        return new IotMessage("auth_task_completed", data, System.currentTimeMillis());
    }

    /**
     * 创建设备状态变更推送消息
     * <p>用于推送设备在线/离线状态变更</p>
     * Requirements: 2.3
     */
    public static IotMessage deviceStatusChange(DeviceStatusPushMessage data) {
        return new IotMessage("device_status_change", data, System.currentTimeMillis());
    }
}

