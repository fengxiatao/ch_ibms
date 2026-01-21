package cn.iocoder.yudao.module.iot.dal.mysql.video;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraCruiseDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 摄像头巡航路线 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CameraCruiseMapper extends BaseMapperX<CameraCruiseDO> {

    /**
     * 根据通道ID查询巡航路线列表
     */
    default List<CameraCruiseDO> selectListByChannelId(Long channelId) {
        return selectList(CameraCruiseDO::getChannelId, channelId);
    }

    /**
     * 根据通道ID和巡航路线名称查询
     */
    default CameraCruiseDO selectByChannelIdAndName(Long channelId, String cruiseName) {
        return selectOne(CameraCruiseDO::getChannelId, channelId,
                        CameraCruiseDO::getCruiseName, cruiseName);
    }

}
