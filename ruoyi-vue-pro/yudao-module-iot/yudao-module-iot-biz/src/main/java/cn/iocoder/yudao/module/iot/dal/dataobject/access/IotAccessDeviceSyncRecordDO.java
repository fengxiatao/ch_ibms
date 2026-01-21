package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 设备人员同步记录 DO
 * 
 * 记录每次设备人员对账的结果，用于：
 * 1. 审计追踪 - 记录每次对账操作的详情
 * 2. 差异分析 - 记录系统与设备之间的人员差异
 * 3. 同步历史 - 查看历史同步记录
 *
 * @author 长辉信息科技
 */
@TableName("iot_access_device_sync_record")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessDeviceSyncRecordDO extends TenantBaseDO {

    /**
     * 同步类型：对账检查
     */
    public static final int SYNC_TYPE_CHECK = 1;
    /**
     * 同步类型：清理多余用户
     */
    public static final int SYNC_TYPE_CLEAN = 2;
    /**
     * 同步类型：补发缺失用户
     */
    public static final int SYNC_TYPE_REPAIR = 3;
    /**
     * 同步类型：全量同步
     */
    public static final int SYNC_TYPE_FULL = 4;

    /**
     * 同步状态：进行中
     */
    public static final int SYNC_STATUS_RUNNING = 0;
    /**
     * 同步状态：成功
     */
    public static final int SYNC_STATUS_SUCCESS = 1;
    /**
     * 同步状态：部分成功
     */
    public static final int SYNC_STATUS_PARTIAL = 2;
    /**
     * 同步状态：失败
     */
    public static final int SYNC_STATUS_FAILED = 3;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称（冗余，便于查询）
     */
    private String deviceName;

    /**
     * 同步类型：1-对账检查，2-清理多余，3-补发缺失，4-全量同步
     */
    private Integer syncType;

    /**
     * 同步状态：0-进行中，1-成功，2-部分成功，3-失败
     */
    private Integer syncStatus;

    /**
     * 系统应有人员数（权限组人员）
     */
    private Integer systemUserCount;

    /**
     * 设备实际人员数
     */
    private Integer deviceUserCount;

    /**
     * 已同步人员数（两边都有）
     */
    private Integer syncedCount;

    /**
     * 系统多余人员数（系统有、设备无，需要下发）
     */
    private Integer systemOnlyCount;

    /**
     * 设备多余人员数（设备有、系统无，野生用户）
     */
    private Integer deviceOnlyCount;

    /**
     * 已清理人员数
     */
    private Integer cleanedCount;

    /**
     * 已补发人员数
     */
    private Integer repairedCount;

    /**
     * 同步开始时间
     */
    private LocalDateTime syncStartTime;

    /**
     * 同步结束时间
     */
    private LocalDateTime syncEndTime;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 系统多余用户ID列表（JSON数组）
     */
    private String systemOnlyUserIds;

    /**
     * 设备多余用户ID列表（JSON数组）
     */
    private String deviceOnlyUserIds;

}
