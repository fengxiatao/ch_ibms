package cn.iocoder.yudao.module.iot.websocket;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessAuthTaskDO;
import cn.iocoder.yudao.module.iot.websocket.message.AuthTaskProgressMessage;
import cn.iocoder.yudao.module.iot.websocket.message.IotMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 授权任务进度推送服务
 * 
 * 负责将授权任务的执行进度通过 WebSocket 实时推送到前端
 * 
 * Requirements: 6.2
 *
 * @author Kiro
 */
@Slf4j
@Service
public class AuthTaskProgressPushService {

    @Resource(name = "deviceStatusWebSocketHandler")
    private DeviceStatusWebSocketHandler webSocketHandler;

    /**
     * 推送任务进度更新
     *
     * @param task 任务信息
     * @param currentPersonName 当前处理的人员姓名
     * @param currentDeviceName 当前处理的设备名称
     */
    public void pushProgress(IotAccessAuthTaskDO task, String currentPersonName, String currentDeviceName) {
        if (task == null) {
            return;
        }

        AuthTaskProgressMessage message = buildProgressMessage(task);
        message.setCurrentPersonName(currentPersonName);
        message.setCurrentDeviceName(currentDeviceName);

        pushAuthTaskProgress(message);
    }

    /**
     * 推送任务进度更新（带错误信息）
     *
     * @param task 任务信息
     * @param currentPersonName 当前处理的人员姓名
     * @param currentDeviceName 当前处理的设备名称
     * @param latestError 最新错误信息
     */
    public void pushProgressWithError(IotAccessAuthTaskDO task, String currentPersonName, 
                                       String currentDeviceName, String latestError) {
        if (task == null) {
            return;
        }

        AuthTaskProgressMessage message = buildProgressMessage(task);
        message.setCurrentPersonName(currentPersonName);
        message.setCurrentDeviceName(currentDeviceName);
        message.setLatestError(latestError);

        pushAuthTaskProgress(message);
    }

    /**
     * 推送任务完成通知
     *
     * @param task 任务信息
     */
    public void pushCompleted(IotAccessAuthTaskDO task) {
        if (task == null) {
            return;
        }

        AuthTaskProgressMessage message = buildProgressMessage(task);
        pushAuthTaskCompleted(message);
    }

    /**
     * 推送单条明细完成通知
     *
     * @param taskId 任务ID
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param success 是否成功
     * @param errorMessage 错误信息（失败时）
     */
    public void pushDetailCompleted(Long taskId, Long personId, Long deviceId, 
                                     boolean success, String errorMessage) {
        AuthTaskProgressMessage message = AuthTaskProgressMessage.builder()
                .taskId(taskId)
                .currentPersonId(personId)
                .currentDeviceId(deviceId)
                .latestError(success ? null : errorMessage)
                .build();

        pushAuthTaskProgress(message);
    }

    /**
     * 推送任务开始通知
     *
     * @param task 任务信息
     */
    public void pushStarted(IotAccessAuthTaskDO task) {
        if (task == null) {
            return;
        }

        AuthTaskProgressMessage message = buildProgressMessage(task);
        message.setStatus(IotAccessAuthTaskDO.STATUS_RUNNING);
        message.setStatusDesc(AuthTaskProgressMessage.getStatusDescription(IotAccessAuthTaskDO.STATUS_RUNNING));

        pushAuthTaskProgress(message);
    }

    /**
     * 推送授权任务进度
     */
    private void pushAuthTaskProgress(AuthTaskProgressMessage message) {
        IotMessage iotMessage = new IotMessage("auth_task_progress", message, System.currentTimeMillis());
        webSocketHandler.broadcast(iotMessage);
        log.debug("[AuthTaskProgressPushService] 推送任务进度: taskId={}, progress={}", 
                message.getTaskId(), message.getProgress());
    }

    /**
     * 推送授权任务完成
     */
    private void pushAuthTaskCompleted(AuthTaskProgressMessage message) {
        IotMessage iotMessage = new IotMessage("auth_task_completed", message, System.currentTimeMillis());
        webSocketHandler.broadcast(iotMessage);
        log.info("[AuthTaskProgressPushService] 推送任务完成: taskId={}, status={}", 
                message.getTaskId(), message.getStatus());
    }

    /**
     * 构建进度消息
     */
    private AuthTaskProgressMessage buildProgressMessage(IotAccessAuthTaskDO task) {
        Integer progress = AuthTaskProgressMessage.calculateProgress(
                task.getTotalCount(), task.getSuccessCount(), task.getFailCount());

        return AuthTaskProgressMessage.builder()
                .taskId(task.getId())
                .taskType(task.getTaskType())
                .groupId(task.getGroupId())
                .totalCount(task.getTotalCount())
                .successCount(task.getSuccessCount())
                .failCount(task.getFailCount())
                .progress(progress)
                .status(task.getStatus())
                .statusDesc(AuthTaskProgressMessage.getStatusDescription(task.getStatus()))
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .build();
    }
}
