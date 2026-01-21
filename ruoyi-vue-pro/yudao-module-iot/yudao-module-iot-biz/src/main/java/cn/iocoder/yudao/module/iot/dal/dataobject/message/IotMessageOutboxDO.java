package cn.iocoder.yudao.module.iot.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 消息发件箱 DO（本地消息表）
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_message_outbox")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotMessageOutboxDO extends BaseDO {
    
    /**
     * ID
     */
    @TableId
    private Long id;
    
    /**
     * 消息主题
     */
    private String topic;
    
    /**
     * 消息Key（用于去重）
     */
    private String messageKey;
    
    /**
     * 消息内容（JSON格式）
     */
    private String content;
    
    /**
     * 状态: PENDING=待发送 SENT=已发送 FAILED=失败
     */
    private String status;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetry;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 成功发送时间
     */
    private LocalDateTime sentTime;
}


