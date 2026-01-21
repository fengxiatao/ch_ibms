package cn.iocoder.yudao.module.iot.dal.mysql.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolRoutePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolRouteDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 电子巡更 - 巡更路线 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface EpatrolRouteMapper extends BaseMapperX<EpatrolRouteDO> {

    default PageResult<EpatrolRouteDO> selectPage(EpatrolRoutePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EpatrolRouteDO>()
                .likeIfPresent(EpatrolRouteDO::getRouteName, reqVO.getRouteName())
                .eqIfPresent(EpatrolRouteDO::getStatus, reqVO.getStatus())
                .orderByDesc(EpatrolRouteDO::getId));
    }

    default List<EpatrolRouteDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<EpatrolRouteDO>()
                .eqIfPresent(EpatrolRouteDO::getStatus, status)
                .orderByDesc(EpatrolRouteDO::getId));
    }

}
