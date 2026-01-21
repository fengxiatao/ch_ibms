package cn.iocoder.yudao.module.iot.dal.mysql.videoview;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.videoview.VideoViewDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 视频监控 - 实时预览视图 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface VideoViewMapper extends BaseMapperX<VideoViewDO> {

    default List<VideoViewDO> selectListByCreator(String creator) {
        return selectList(new LambdaQueryWrapperX<VideoViewDO>()
                .eq(VideoViewDO::getCreator, creator)
                .orderByAsc(VideoViewDO::getSort)
                .orderByDesc(VideoViewDO::getId));
    }

    default VideoViewDO selectByIdAndCreator(Long id, String creator) {
        return selectOne(new LambdaQueryWrapperX<VideoViewDO>()
                .eq(VideoViewDO::getId, id)
                .eq(VideoViewDO::getCreator, creator));
    }

}
