package cn.iocoder.yudao.module.iot.dal.dataobject.job;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务执行记录 DO
 * 
 * @author IBMS Team
 */
@TableName("iot_job_execution_record")
@Data
@EqualsAndHashCode(callSuper = true)
public class JobExecutionRecordDO extends TenantBaseDO {
    
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 实体类型: PRODUCT/DEVICE/SYSTEM/FACILITY
     */
    private String entityType;
    
    /**
     * 实体ID
     */
    private Long entityId;
    
    /**
     * 任务类型: offlineCheck/healthCheck/dataCollect
     */
    private String jobType;
    
    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;
    
    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;
    
    /**
     * 执行状态: 1-成功 2-失败
     */
    private Integer executeStatus;
    
    /**
     * 执行结果
     */
    private String executeResult;
    
    /**
     * 执行时长（毫秒）
     */
    private Integer executeDuration;
}






