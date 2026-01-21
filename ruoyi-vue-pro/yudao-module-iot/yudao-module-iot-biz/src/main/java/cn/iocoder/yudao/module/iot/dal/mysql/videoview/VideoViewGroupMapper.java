package cn.iocoder.yudao.module.iot.dal.mysql.videoview;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.videoview.VideoViewGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 视频监控 - 实时预览视图分组 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface VideoViewGroupMapper extends BaseMapperX<VideoViewGroupDO> {

    default List<VideoViewGroupDO> selectList() {
        return selectList(new LambdaQueryWrapperX<VideoViewGroupDO>()
                .orderByAsc(VideoViewGroupDO::getSort)
                .orderByDesc(VideoViewGroupDO::getId));
    }

}
