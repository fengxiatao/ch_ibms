package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorReasonDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 访客来访事由 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotVisitorReasonMapper extends BaseMapperX<IotVisitorReasonDO> {

    /**
     * 获取启用的来访事由列表
     */
    default List<IotVisitorReasonDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<IotVisitorReasonDO>()
                .eqIfPresent(IotVisitorReasonDO::getStatus, status)
                .orderByAsc(IotVisitorReasonDO::getSort));
    }

    /**
     * 根据事由名称查询
     */
    default IotVisitorReasonDO selectByReasonName(String reasonName) {
        return selectOne(IotVisitorReasonDO::getReasonName, reasonName);
    }

}
