package cn.iocoder.yudao.module.iot.service.message;

/**
 * 消息幂等性服务
 *
 * @author 长辉信息科技有限公司
 */
public interface MessageIdempotentService {
    
    /**
     * 尝试处理消息（幂等性检查）
     * 
     * @param messageId 消息ID
     * @param topic 消息主题
     * @return true=可以处理（首次），false=已处理过（重复）
     */
    boolean tryProcess(String messageId, String topic);
    
    /**
     * 标记消息处理成功
     * 
     * @param messageId 消息ID
     */
    void markSuccess(String messageId);
    
    /**
     * 标记消息处理失败
     * 
     * @param messageId 消息ID
     * @param errorMessage 错误信息
     */
    void markFailed(String messageId, String errorMessage);
}


