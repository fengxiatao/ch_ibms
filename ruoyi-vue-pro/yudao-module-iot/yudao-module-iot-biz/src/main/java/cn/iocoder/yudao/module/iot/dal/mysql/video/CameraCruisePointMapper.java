package cn.iocoder.yudao.module.iot.dal.mysql.video;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraCruisePointDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 摄像头巡航点 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CameraCruisePointMapper extends BaseMapperX<CameraCruisePointDO> {

    /**
     * 根据巡航路线ID查询巡航点列表（按顺序排序）
     */
    default List<CameraCruisePointDO> selectListByCruiseId(Long cruiseId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CameraCruisePointDO>()
                .eq(CameraCruisePointDO::getCruiseId, cruiseId)
                .orderByAsc(CameraCruisePointDO::getSortOrder));
    }

    /**
     * 根据巡航路线ID删除所有巡航点
     */
    default void deleteByCruiseId(Long cruiseId) {
        delete(CameraCruisePointDO::getCruiseId, cruiseId);
    }

    /**
     * 根据预设点ID查询使用该预设点的巡航点列表
     */
    default List<CameraCruisePointDO> selectListByPresetId(Long presetId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CameraCruisePointDO>()
                .eq(CameraCruisePointDO::getPresetId, presetId));
    }

    /**
     * 检查预设点是否被巡航使用
     */
    default boolean existsByPresetId(Long presetId) {
        return selectCount(CameraCruisePointDO::getPresetId, presetId) > 0;
    }

}
