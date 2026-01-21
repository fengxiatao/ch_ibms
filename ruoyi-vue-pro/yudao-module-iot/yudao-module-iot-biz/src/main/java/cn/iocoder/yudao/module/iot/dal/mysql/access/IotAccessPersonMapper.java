package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门禁人员 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessPersonMapper extends BaseMapperX<IotAccessPersonDO> {

    default PageResult<IotAccessPersonDO> selectPage(String personCode, String personName,
                                                      Integer personType, List<Long> deptIds, Integer status,
                                                      Integer pageNo, Integer pageSize) {
        cn.iocoder.yudao.framework.common.pojo.PageParam pageParam = new cn.iocoder.yudao.framework.common.pojo.PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam,
                new LambdaQueryWrapperX<IotAccessPersonDO>()
                .likeIfPresent(IotAccessPersonDO::getPersonCode, personCode)
                .likeIfPresent(IotAccessPersonDO::getPersonName, personName)
                .eqIfPresent(IotAccessPersonDO::getPersonType, personType)
                .inIfPresent(IotAccessPersonDO::getDeptId, deptIds)
                .eqIfPresent(IotAccessPersonDO::getStatus, status)
                .orderByDesc(IotAccessPersonDO::getId));
    }

    default IotAccessPersonDO selectByPersonCode(String personCode) {
        // 使用 selectFirstOne 避免重复 personCode 导致 TooManyResultsException
        return selectFirstOne(IotAccessPersonDO::getPersonCode, personCode);
    }

    default List<IotAccessPersonDO> selectListByDeptId(Long deptId) {
        return selectList(IotAccessPersonDO::getDeptId, deptId);
    }

    default Long selectCountByDeptId(Long deptId) {
        return selectCount(IotAccessPersonDO::getDeptId, deptId);
    }

    /**
     * 获取最大的人员编号
     * 用于生成新的流水号
     */
    default String selectMaxPersonCode() {
        IotAccessPersonDO person = selectOne(new LambdaQueryWrapperX<IotAccessPersonDO>()
                .likeRight(IotAccessPersonDO::getPersonCode, "YG")
                .orderByDesc(IotAccessPersonDO::getPersonCode)
                .last("LIMIT 1"));
        return person != null ? person.getPersonCode() : null;
    }

}
