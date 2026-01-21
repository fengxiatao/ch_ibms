package cn.iocoder.yudao.module.iot.dal.mysql.visitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorAuthDeviceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 访客授权设备关联 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotVisitorAuthDeviceMapper extends BaseMapperX<IotVisitorAuthDeviceDO> {

    default List<IotVisitorAuthDeviceDO> selectListByAuthId(Long authId) {
        return selectList(IotVisitorAuthDeviceDO::getAuthId, authId);
    }

    default List<IotVisitorAuthDeviceDO> selectListByApplyId(Long applyId) {
        return selectList(IotVisitorAuthDeviceDO::getApplyId, applyId);
    }

    default List<IotVisitorAuthDeviceDO> selectListByVisitorId(Long visitorId) {
        return selectList(IotVisitorAuthDeviceDO::getVisitorId, visitorId);
    }

    default void deleteByAuthId(Long authId) {
        delete(new LambdaQueryWrapperX<IotVisitorAuthDeviceDO>()
                .eq(IotVisitorAuthDeviceDO::getAuthId, authId));
    }

    default void deleteByApplyId(Long applyId) {
        delete(new LambdaQueryWrapperX<IotVisitorAuthDeviceDO>()
                .eq(IotVisitorAuthDeviceDO::getApplyId, applyId));
    }

}
