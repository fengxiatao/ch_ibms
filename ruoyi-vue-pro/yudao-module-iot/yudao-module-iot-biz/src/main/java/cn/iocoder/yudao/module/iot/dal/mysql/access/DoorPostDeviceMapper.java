package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.DoorPostDeviceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DoorPostDeviceMapper extends BaseMapperX<DoorPostDeviceDO> {

    default List<DoorPostDeviceDO> selectListByPostId(Long postId) {
        return selectList(DoorPostDeviceDO::getPostId, postId);
    }

    default void deleteByPostId(Long postId) {
        delete(new LambdaQueryWrapperX<DoorPostDeviceDO>()
                .eq(DoorPostDeviceDO::getPostId, postId));
    }

}


























