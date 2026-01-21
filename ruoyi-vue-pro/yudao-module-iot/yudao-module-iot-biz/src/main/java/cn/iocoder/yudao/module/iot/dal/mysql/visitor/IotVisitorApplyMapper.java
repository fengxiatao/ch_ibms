package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorApplyDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客申请/预约 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotVisitorApplyMapper extends BaseMapperX<IotVisitorApplyDO> {

    default PageResult<IotVisitorApplyDO> selectPage(String visitorName, String visiteeName, String visitReason,
                                                      Integer visitStatus, Integer approveStatus,
                                                      LocalDateTime visitTimeStart, LocalDateTime visitTimeEnd,
                                                      Integer pageNo, Integer pageSize) {
        cn.iocoder.yudao.framework.common.pojo.PageParam pageParam = new cn.iocoder.yudao.framework.common.pojo.PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam,
                new LambdaQueryWrapperX<IotVisitorApplyDO>()
                        .likeIfPresent(IotVisitorApplyDO::getVisitorName, visitorName)
                        .likeIfPresent(IotVisitorApplyDO::getVisiteeName, visiteeName)
                        .likeIfPresent(IotVisitorApplyDO::getVisitReason, visitReason)
                        .eqIfPresent(IotVisitorApplyDO::getVisitStatus, visitStatus)
                        .eqIfPresent(IotVisitorApplyDO::getApproveStatus, approveStatus)
                        .geIfPresent(IotVisitorApplyDO::getPlanVisitTime, visitTimeStart)
                        .leIfPresent(IotVisitorApplyDO::getPlanVisitTime, visitTimeEnd)
                        .orderByDesc(IotVisitorApplyDO::getId));
    }

    default IotVisitorApplyDO selectByApplyCode(String applyCode) {
        return selectOne(IotVisitorApplyDO::getApplyCode, applyCode);
    }

    default List<IotVisitorApplyDO> selectListByVisitorId(Long visitorId) {
        return selectList(IotVisitorApplyDO::getVisitorId, visitorId);
    }

    default List<IotVisitorApplyDO> selectListByVisitStatus(Integer visitStatus) {
        return selectList(IotVisitorApplyDO::getVisitStatus, visitStatus);
    }

    default List<IotVisitorApplyDO> selectListByApproveStatus(Integer approveStatus) {
        return selectList(IotVisitorApplyDO::getApproveStatus, approveStatus);
    }

    default Long countByVisitStatus(Integer visitStatus) {
        return selectCount(IotVisitorApplyDO::getVisitStatus, visitStatus);
    }

    default Long countByVisitTimeRange(LocalDateTime start, LocalDateTime end) {
        return selectCount(new LambdaQueryWrapperX<IotVisitorApplyDO>()
                .ge(IotVisitorApplyDO::getActualVisitTime, start)
                .le(IotVisitorApplyDO::getActualVisitTime, end));
    }

}
