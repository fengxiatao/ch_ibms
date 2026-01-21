package cn.iocoder.yudao.module.iot.dal.dataobject.changhui;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 长辉设备升级任务 DO
 * 
 * <p>管理设备固件升级任务，支持TCP帧传输和HTTP URL下载两种升级模式
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@TableName("changhui_upgrade_task")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChanghuiUpgradeTaskDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 测站编码
     */
    private String stationCode;

    /**
     * 固件ID
     */
    private Long firmwareId;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 升级模式：0=TCP帧传输, 1=HTTP URL下载
     * 
     * @see cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiUpgradeModeEnum
     */
    private Integer upgradeMode;

    /**
     * 固件下载URL（HTTP模式使用）
     */
    private String firmwareUrl;

    /**
     * 状态：0-待执行,1-进行中,2-成功,3-失败,4-已取消,5-已拒绝
     * 
     * @see cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiUpgradeStatusEnum
     */
    private Integer status;

    /**
     * 进度(0-100)
     */
    private Integer progress;

    /**
     * 总帧数（TCP帧传输模式使用）
     */
    private Integer totalFrames;

    /**
     * 已发送帧数（TCP帧传输模式使用）
     */
    private Integer sentFrames;

    /**
     * 重试次数
     */
    private Integer retryCount;

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

}
