package cn.iocoder.yudao.module.iot.dal.mysql.video;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraPresetDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 摄像头预设点 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CameraPresetMapper extends BaseMapperX<CameraPresetDO> {

    /**
     * 根据通道ID查询预设点列表
     */
    default List<CameraPresetDO> selectListByChannelId(Long channelId) {
        return selectList(new LambdaQueryWrapperX<CameraPresetDO>()
                .eq(CameraPresetDO::getChannelId, channelId)
                .orderByAsc(CameraPresetDO::getPresetNo));
    }

    /**
     * 根据通道ID和预设点编号查询
     */
    default CameraPresetDO selectByChannelIdAndPresetNo(Long channelId, Integer presetNo) {
        return selectOne(new LambdaQueryWrapperX<CameraPresetDO>()
                .eq(CameraPresetDO::getChannelId, channelId)
                .eq(CameraPresetDO::getPresetNo, presetNo));
    }

    /**
     * 检查预设点编号是否已存在
     */
    default boolean existsByChannelIdAndPresetNo(Long channelId, Integer presetNo, Long excludeId) {
        return selectCount(new LambdaQueryWrapperX<CameraPresetDO>()
                .eq(CameraPresetDO::getChannelId, channelId)
                .eq(CameraPresetDO::getPresetNo, presetNo)
                .ne(excludeId != null, CameraPresetDO::getId, excludeId)) > 0;
    }

}
