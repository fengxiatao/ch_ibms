package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门禁授权任务 DO
 * 
 * 任务类型说明：
 * - group_dispatch: 权限组下发（批量将权限组中的人员下发到关联设备）
 * - person_dispatch: 单人下发（将单个人员下发到其关联的所有设备）
 * - revoke: 权限撤销（从设备删除人员权限）
 * 
 * 状态流转：
 * - 0(待执行) -> 1(执行中) -> 2(已完成) / 3(部分失败) / 4(全部失败)
 *
 * @author 芋道源码
 */
@TableName("iot_access_auth_task")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessAuthTaskDO extends TenantBaseDO {

    /**
     * 任务状态：待执行
     */
    public static final int STATUS_PENDING = 0;
    /**
     * 任务状态：执行中
     */
    public static final int STATUS_RUNNING = 1;
    /**
     * 任务状态：已完成
     */
    public static final int STATUS_COMPLETED = 2;
    /**
     * 任务状态：部分失败
     */
    public static final int STATUS_PARTIAL_FAILED = 3;
    /**
     * 任务状态：全部失败
     */
    public static final int STATUS_ALL_FAILED = 4;

    /**
     * 任务类型：权限组下发
     */
    public static final String TASK_TYPE_GROUP_DISPATCH = "group_dispatch";
    /**
     * 任务类型：单人下发
     */
    public static final String TASK_TYPE_PERSON_DISPATCH = "person_dispatch";
    /**
     * 任务类型：权限撤销
     */
    public static final String TASK_TYPE_REVOKE = "revoke";

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务类型：group_dispatch-权限组下发，person_dispatch-单人下发，revoke-权限撤销
     */
    private String taskType;

    /**
     * 权限组ID（权限组下发时必填）
     */
    private Long groupId;

    /**
     * 人员ID（单人下发/撤销时必填）
     */
    private Long personId;

    /**
     * 总数量（任务明细总数）
     */
    private Integer totalCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 状态：0-待执行，1-执行中，2-已完成，3-部分失败，4-全部失败
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 计算任务最终状态
     * 根据成功/失败计数确定最终状态
     */
    public int calculateFinalStatus() {
        if (failCount == null || failCount == 0) {
            return STATUS_COMPLETED;
        } else if (successCount == null || successCount == 0) {
            return STATUS_ALL_FAILED;
        } else {
            return STATUS_PARTIAL_FAILED;
        }
    }

}
