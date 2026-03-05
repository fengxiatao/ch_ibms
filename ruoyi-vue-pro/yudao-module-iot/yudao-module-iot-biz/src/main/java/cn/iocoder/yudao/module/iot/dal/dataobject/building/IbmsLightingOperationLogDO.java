package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 照明操作日志 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_lighting_operation_log")
@KeySequence("ibms_lighting_operation_log_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsLightingOperationLogDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作类型：1-手动控制 2-场景执行 3-定时任务 4-系统操作
     */
    private Integer operationType;

    /**
     * 操作对象类型：1-回路 2-场景 3-网关 4-控制器
     */
    private Integer targetType;

    /**
     * 操作对象ID
     */
    private Long targetId;

    /**
     * 操作对象名称
     */
    private String targetName;

    /**
     * 操作内容
     */
    private String operationContent;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作IP
     */
    private String operatorIp;

    /**
     * 结果：0-失败 1-成功
     */
    private Integer result;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}
