package cn.iocoder.yudao.module.iot.dal.mysql.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPointPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPointDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 电子巡更 - 巡更点 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface EpatrolPointMapper extends BaseMapperX<EpatrolPointDO> {

    default PageResult<EpatrolPointDO> selectPage(EpatrolPointPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EpatrolPointDO>()
                .likeIfPresent(EpatrolPointDO::getPointNo, reqVO.getPointNo())
                .likeIfPresent(EpatrolPointDO::getPointName, reqVO.getPointName())
                .likeIfPresent(EpatrolPointDO::getPointLocation, reqVO.getPointLocation())
                .eqIfPresent(EpatrolPointDO::getStatus, reqVO.getStatus())
                .orderByDesc(EpatrolPointDO::getId));
    }

    default List<EpatrolPointDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<EpatrolPointDO>()
                .eqIfPresent(EpatrolPointDO::getStatus, status)
                .orderByDesc(EpatrolPointDO::getId));
    }

    default EpatrolPointDO selectByPointNo(String pointNo) {
        return selectOne(EpatrolPointDO::getPointNo, pointNo);
    }

}
