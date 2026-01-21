package cn.iocoder.yudao.module.iot.convert.video;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule.VideoPatrolScheduleRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.VideoPatrolScheduleDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 视频定时轮巡计划 Convert
 *
 * @author 芋道源码
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VideoPatrolScheduleConvert {

    VideoPatrolScheduleConvert INSTANCE = Mappers.getMapper(VideoPatrolScheduleConvert.class);

    VideoPatrolScheduleRespVO convert(VideoPatrolScheduleDO bean);

    PageResult<VideoPatrolScheduleRespVO> convertPage(PageResult<VideoPatrolScheduleDO> page);

}
