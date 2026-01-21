package cn.iocoder.yudao.module.iot.dal.dataobject.changhui;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 长辉设备控制日志 DO
 * 
 * <p>记录设备远程控制操作，包括模式切换、手动控制、自动控制等
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@TableName("changhui_control_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChanghuiControlLogDO extends TenantBaseDO {

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
     * 控制类型
     * 
     * <p>MODE_SWITCH-模式切换, MANUAL_CONTROL-手动控制, AUTO_CONTROL-自动控制
     * @see cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiControlTypeEnum
     */
    private String controlType;

    /**
     * 控制参数(JSON格式)
     * 
     * <p>例如：{"action":"rise"} 或 {"targetValue":1.5,"controlMode":"flow"}
     */
    private String controlParams;

    /**
     * 结果：0-失败,1-成功
     */
    private Integer result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 操作员
     */
    private String operator;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

}
