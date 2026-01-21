package cn.iocoder.yudao.module.iot.dal.mysql.camera;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotMonitorWallDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 监控墙配置 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotMonitorWallMapper extends BaseMapperX<IotMonitorWallDO> {

    /**
     * 查询所有监控墙
     *
     * @return 监控墙列表
     */
    default List<IotMonitorWallDO> selectAll() {
        return selectList(new LambdaQueryWrapperX<IotMonitorWallDO>()
                .orderByDesc(IotMonitorWallDO::getIsDefault)
                .orderByDesc(IotMonitorWallDO::getId));
    }

    /**
     * 查询默认监控墙
     *
     * @return 默认监控墙
     */
    default IotMonitorWallDO selectDefault() {
        return selectOne(new LambdaQueryWrapperX<IotMonitorWallDO>()
                .eq(IotMonitorWallDO::getIsDefault, true));
    }

    /**
     * 取消所有默认监控墙
     */
    default void clearDefault() {
        update(null, new LambdaUpdateWrapper<IotMonitorWallDO>()
                .eq(IotMonitorWallDO::getIsDefault, true)
                .set(IotMonitorWallDO::getIsDefault, false));
    }

}

