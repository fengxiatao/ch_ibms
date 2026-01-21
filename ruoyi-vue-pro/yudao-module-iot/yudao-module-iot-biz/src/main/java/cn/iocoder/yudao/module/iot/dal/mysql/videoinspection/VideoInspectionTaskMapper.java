package cn.iocoder.yudao.module.iot.dal.mysql.videoinspection;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.videoinspection.IotVideoInspectionTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频巡检任务 Mapper
 *
 * @author system
 */
@Mapper
public interface VideoInspectionTaskMapper extends BaseMapperX<IotVideoInspectionTaskDO> {

}
