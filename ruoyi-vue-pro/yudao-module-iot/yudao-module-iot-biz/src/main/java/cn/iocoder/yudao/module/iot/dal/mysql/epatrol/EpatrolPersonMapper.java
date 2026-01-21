package cn.iocoder.yudao.module.iot.dal.mysql.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPersonPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPersonDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 电子巡更 - 巡更人员 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface EpatrolPersonMapper extends BaseMapperX<EpatrolPersonDO> {

    default PageResult<EpatrolPersonDO> selectPage(EpatrolPersonPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EpatrolPersonDO>()
                .likeIfPresent(EpatrolPersonDO::getName, reqVO.getName())
                .likeIfPresent(EpatrolPersonDO::getPhone, reqVO.getPhone())
                .likeIfPresent(EpatrolPersonDO::getPatrolStickNo, reqVO.getPatrolStickNo())
                .eqIfPresent(EpatrolPersonDO::getStatus, reqVO.getStatus())
                .orderByDesc(EpatrolPersonDO::getId));
    }

    default List<EpatrolPersonDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<EpatrolPersonDO>()
                .eqIfPresent(EpatrolPersonDO::getStatus, status)
                .orderByDesc(EpatrolPersonDO::getId));
    }

    default EpatrolPersonDO selectByPatrolStickNo(String patrolStickNo) {
        return selectOne(EpatrolPersonDO::getPatrolStickNo, patrolStickNo);
    }

    default EpatrolPersonDO selectByPersonCardNo(String personCardNo) {
        return selectOne(EpatrolPersonDO::getPersonCardNo, personCardNo);
    }

}
