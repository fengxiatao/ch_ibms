package cn.iocoder.yudao.module.iot.dal.mysql.videoview;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.videoview.VideoViewPaneDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 视频监控 - 实时预览视图窗格 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface VideoViewPaneMapper extends BaseMapperX<VideoViewPaneDO> {

    default List<VideoViewPaneDO> selectListByViewId(Long viewId) {
        return selectList(new LambdaQueryWrapperX<VideoViewPaneDO>()
                .eq(VideoViewPaneDO::getViewId, viewId)
                .orderByAsc(VideoViewPaneDO::getPaneIndex));
    }

    default void deleteByViewId(Long viewId) {
        delete(new LambdaQueryWrapperX<VideoViewPaneDO>()
                .eq(VideoViewPaneDO::getViewId, viewId));
    }

}
