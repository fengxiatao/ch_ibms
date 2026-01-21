package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门禁授权任务明细 DO
 * 
 * 记录单个人员到单个设备的下发结果
 * 
 * 状态说明：
 * - 0: 待执行
 * - 1: 成功
 * - 2: 失败
 *
 * @author 芋道源码
 */
@TableName("iot_access_auth_task_detail")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessAuthTaskDetailDO extends TenantBaseDO {

    /**
     * 明细状态：待执行
     */
    public static final int STATUS_PENDING = 0;
    /**
     * 明细状态：成功
     */
    public static final int STATUS_SUCCESS = 1;
    /**
     * 明细状态：失败
     */
    public static final int STATUS_FAILED = 2;

    /**
     * 明细ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员编号（冗余字段，便于查询展示）
     */
    private String personCode;

    /**
     * 人员姓名（冗余字段，便于查询展示）
     */
    private String personName;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称（冗余字段，便于查询展示）
     */
    private String deviceName;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 状态：0-待执行，1-成功，2-失败
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 下发的凭证类型，逗号分隔（如：FACE,CARD,PASSWORD）
     */
    private String credentialTypes;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最后错误信息（用于重试场景）
     */
    private String lastError;

}
