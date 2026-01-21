package cn.iocoder.yudao.module.iot.service.access.event;

import cn.iocoder.yudao.module.iot.service.access.IotAccessAuthDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 门禁下发事件监听器
 * 
 * 在事务提交后异步处理下发任务，
 * 确保数据库操作和下发操作完全解耦。
 *
 * @author 芋道源码
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessDispatchEventListener {

    private final IotAccessAuthDispatchService authDispatchService;

    /**
     * 处理下发任务创建事件
     * 
     * 使用 @TransactionalEventListener 确保在事务提交后执行，
     * 使用 @Async 确保异步执行，不阻塞主线程。
     * 
     * fallbackExecution = true: 确保即使没有事务也会执行（应对 REQUIRES_NEW 场景）
     *
     * @param event 下发任务创建事件
     */
    @Async("accessDispatchExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDispatchTaskCreated(DispatchTaskCreatedEvent event) {
        log.info("[onDispatchTaskCreated] 收到任务事件: taskId={}, taskType={}, isRevoke={}, detailCount={}",
                event.getTaskId(), event.getTaskType(), event.isRevokeTask(),
                event.getDetailIds() != null ? event.getDetailIds().size() : 0);
        
        try {
            if (event.isRevokeTask()) {
                // 撤销任务
                authDispatchService.executeRevokeDispatchAsync(event.getTaskId());
            } else {
                // 下发任务
                authDispatchService.executeDispatchAsync(event.getTaskId());
            }
        } catch (Exception e) {
            log.error("[onDispatchTaskCreated] 执行任务失败: taskId={}, isRevoke={}", 
                    event.getTaskId(), event.isRevokeTask(), e);
            // 异常不抛出，避免影响其他事件处理
        }
    }
}
