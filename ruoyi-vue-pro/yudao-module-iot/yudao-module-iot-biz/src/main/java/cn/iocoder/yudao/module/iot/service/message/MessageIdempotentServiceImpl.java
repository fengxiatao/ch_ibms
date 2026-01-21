package cn.iocoder.yudao.module.iot.service.message;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.dal.dataobject.message.IotMessageIdempotentDO;
import cn.iocoder.yudao.module.iot.dal.mysql.message.IotMessageIdempotentMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消息幂等性服务实现
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class MessageIdempotentServiceImpl implements MessageIdempotentService {
    
    @Resource
    private IotMessageIdempotentMapper idempotentMapper;
    
    @Override
    @TenantIgnore // 忽略租户隔离：因为 RocketMQ 消费者调用时，未传递租户上下文
    public boolean tryProcess(String messageId, String topic) {
        try {
            // 尝试插入幂等记录（利用唯一索引）
            IotMessageIdempotentDO record = IotMessageIdempotentDO.builder()
                    .messageId(messageId)
                    .topic(topic)
                    .status("PROCESSING")
                    .build();
            
            idempotentMapper.insert(record);
            
            log.debug("[tryProcess][消息可以处理: {}]", messageId);
            return true; // 插入成功，可以处理
            
        } catch (DuplicateKeyException e) {
            // 唯一索引冲突，说明已经处理过
            log.warn("[tryProcess][消息已处理，跳过: {}]", messageId);
            return false;
            
        } catch (Exception e) {
            log.error("[tryProcess][幂等性检查失败: {}]", messageId, e);
            return false; // 异常情况，拒绝处理
        }
    }
    
    @Override
    @TenantIgnore // 忽略租户隔离：因为 RocketMQ 消费者调用时，未传递租户上下文
    public void markSuccess(String messageId) {
        try {
            IotMessageIdempotentDO record = idempotentMapper.selectByMessageId(messageId);
            if (record != null) {
                record.setStatus("SUCCESS");
                record.setProcessedTime(LocalDateTime.now());
                idempotentMapper.updateById(record);
                
                log.debug("[markSuccess][标记消息成功: {}]", messageId);
            }
        } catch (Exception e) {
            log.error("[markSuccess][标记失败]", e);
        }
    }
    
    @Override
    @TenantIgnore // 忽略租户隔离：因为 RocketMQ 消费者调用时，未传递租户上下文
    public void markFailed(String messageId, String errorMessage) {
        try {
            IotMessageIdempotentDO record = idempotentMapper.selectByMessageId(messageId);
            if (record != null) {
                record.setStatus("FAILED");
                record.setProcessedTime(LocalDateTime.now());
                record.setErrorMessage(errorMessage);
                idempotentMapper.updateById(record);
                
                log.debug("[markFailed][标记消息失败: {}]", messageId);
            }
        } catch (Exception e) {
            log.error("[markFailed][标记失败]", e);
        }
    }
}


