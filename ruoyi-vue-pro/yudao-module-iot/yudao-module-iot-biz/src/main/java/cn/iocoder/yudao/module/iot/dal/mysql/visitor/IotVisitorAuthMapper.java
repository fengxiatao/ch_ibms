package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorAuthDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 访客授权记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotVisitorAuthMapper extends BaseMapperX<IotVisitorAuthDO> {

    default IotVisitorAuthDO selectByApplyId(Long applyId) {
        return selectOne(IotVisitorAuthDO::getApplyId, applyId);
    }

    default List<IotVisitorAuthDO> selectListByVisitorId(Long visitorId) {
        return selectList(IotVisitorAuthDO::getVisitorId, visitorId);
    }

    default List<IotVisitorAuthDO> selectListByAuthStatus(Integer authStatus) {
        return selectList(IotVisitorAuthDO::getAuthStatus, authStatus);
    }

    default List<IotVisitorAuthDO> selectListByDispatchTaskId(Long dispatchTaskId) {
        return selectList(IotVisitorAuthDO::getDispatchTaskId, dispatchTaskId);
    }

    default List<IotVisitorAuthDO> selectExpiredAuths() {
        return selectList(new LambdaQueryWrapperX<IotVisitorAuthDO>()
                .eq(IotVisitorAuthDO::getAuthStatus, IotVisitorAuthDO.AUTH_STATUS_DISPATCHED)
                .lt(IotVisitorAuthDO::getAuthEndTime, java.time.LocalDateTime.now()));
    }

}
