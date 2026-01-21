package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.DoorGroupDeviceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DoorGroupDeviceMapper extends BaseMapperX<DoorGroupDeviceDO> {

    default List<DoorGroupDeviceDO> selectListByGroupId(Long groupId) {
        return selectList(DoorGroupDeviceDO::getGroupId, groupId);
    }

    default void deleteByGroupId(Long groupId) {
        delete(new LambdaQueryWrapperX<DoorGroupDeviceDO>()
                .eq(DoorGroupDeviceDO::getGroupId, groupId));
    }

}


























