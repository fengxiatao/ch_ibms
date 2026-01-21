package cn.iocoder.yudao.module.iot.dal.mysql.message;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.message.IotMessageIdempotentDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 消息幂等性检查 Mapper
 * 
 * ⚠️ 忽略租户拦截：因为 RocketMQ 消费者调用时，未传递租户上下文
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface IotMessageIdempotentMapper extends BaseMapperX<IotMessageIdempotentDO> {
    
    /**
     * 根据消息ID查询（利用唯一索引）
     */
    default IotMessageIdempotentDO selectByMessageId(String messageId) {
        return selectOne(IotMessageIdempotentDO::getMessageId, messageId);
    }
}


