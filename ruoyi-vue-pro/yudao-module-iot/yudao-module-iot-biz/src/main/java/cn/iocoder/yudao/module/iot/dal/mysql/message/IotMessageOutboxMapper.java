package cn.iocoder.yudao.module.iot.dal.mysql.message;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.message.IotMessageOutboxDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * IoT 消息发件箱 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface IotMessageOutboxMapper extends BaseMapperX<IotMessageOutboxDO> {
    
    /**
     * 查询待发送的消息（状态为PENDING且未超过最大重试次数）
     */
    @Select("SELECT * FROM iot_message_outbox " +
            "WHERE status = 'PENDING' " +
            "AND retry_count < max_retry " +
            "AND deleted = 0 " +
            "ORDER BY create_time ASC " +
            "LIMIT 100")
    List<IotMessageOutboxDO> selectPendingMessages();
}


