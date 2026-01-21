package cn.iocoder.yudao.module.iot.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 消息幂等性检查 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_message_idempotent")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotMessageIdempotentDO extends BaseDO {
    
    /**
     * ID
     */
    @TableId
    private Long id;
    
    /**
     * 消息ID（业务生成）
     */
    private String messageId;
    
    /**
     * 消息主题
     */
    private String topic;
    
    /**
     * 状态: PROCESSING=处理中 SUCCESS=成功 FAILED=失败
     */
    private String status;
    
    /**
     * 处理完成时间
     */
    private LocalDateTime processedTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
}


