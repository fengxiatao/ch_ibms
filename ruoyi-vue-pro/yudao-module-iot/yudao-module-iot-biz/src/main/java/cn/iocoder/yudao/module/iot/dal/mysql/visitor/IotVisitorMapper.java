package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访客信息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotVisitorMapper extends BaseMapperX<IotVisitorDO> {

    default PageResult<IotVisitorDO> selectPage(String visitorName, String phone, Integer pageNo, Integer pageSize) {
        cn.iocoder.yudao.framework.common.pojo.PageParam pageParam = new cn.iocoder.yudao.framework.common.pojo.PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam,
                new LambdaQueryWrapperX<IotVisitorDO>()
                        .likeIfPresent(IotVisitorDO::getVisitorName, visitorName)
                        .likeIfPresent(IotVisitorDO::getPhone, phone)
                        .orderByDesc(IotVisitorDO::getId));
    }

    default IotVisitorDO selectByVisitorCode(String visitorCode) {
        return selectOne(IotVisitorDO::getVisitorCode, visitorCode);
    }

    default IotVisitorDO selectByPhone(String phone) {
        return selectOne(IotVisitorDO::getPhone, phone);
    }

    default IotVisitorDO selectByIdCard(String idCard) {
        return selectOne(IotVisitorDO::getIdCard, idCard);
    }

}
