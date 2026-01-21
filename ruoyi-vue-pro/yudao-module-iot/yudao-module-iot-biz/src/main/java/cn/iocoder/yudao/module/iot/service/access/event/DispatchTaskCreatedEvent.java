package cn.iocoder.yudao.module.iot.service.access.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 下发任务创建事件
 * 
 * 用于解耦数据库操作和下发操作，
 * 在事务提交后触发异步下发。
 *
 * @author 芋道源码
 */
@Getter
public class DispatchTaskCreatedEvent extends ApplicationEvent {

    /**
     * 任务ID
     */
    private final Long taskId;

    /**
     * 任务类型：1-权限组下发，2-人员下发
     */
    private final Integer taskType;

    /**
     * 权限组ID（权限组下发时使用）
     */
    private final Long groupId;

    /**
     * 人员ID（人员下发时使用）
     */
    private final Long personId;

    /**
     * 任务明细ID列表
     */
    private final List<Long> detailIds;

    public DispatchTaskCreatedEvent(Object source, Long taskId, Integer taskType, 
                                     Long groupId, Long personId, List<Long> detailIds) {
        super(source);
        this.taskId = taskId;
        this.taskType = taskType;
        this.groupId = groupId;
        this.personId = personId;
        this.detailIds = detailIds;
    }

    /**
     * 创建权限组下发事件
     */
    public static DispatchTaskCreatedEvent forPermissionGroup(Object source, Long taskId, 
                                                               Long groupId, List<Long> detailIds) {
        return new DispatchTaskCreatedEvent(source, taskId, 1, groupId, null, detailIds);
    }

    /**
     * 创建人员下发事件
     */
    public static DispatchTaskCreatedEvent forPerson(Object source, Long taskId, 
                                                      Long personId, List<Long> detailIds) {
        return new DispatchTaskCreatedEvent(source, taskId, 2, null, personId, detailIds);
    }

    /**
     * 创建权限组撤销事件
     */
    public static DispatchTaskCreatedEvent forPermissionGroupRevoke(Object source, Long taskId, 
                                                                     Long groupId, List<Long> detailIds) {
        return new DispatchTaskCreatedEvent(source, taskId, 3, groupId, null, detailIds);
    }

    /**
     * 创建人员撤销事件
     */
    public static DispatchTaskCreatedEvent forPersonRevoke(Object source, Long taskId, 
                                                            Long personId, List<Long> detailIds) {
        return new DispatchTaskCreatedEvent(source, taskId, 4, null, personId, detailIds);
    }

    /**
     * 判断是否为撤销任务
     */
    public boolean isRevokeTask() {
        return taskType != null && taskType >= 3;
    }
}
