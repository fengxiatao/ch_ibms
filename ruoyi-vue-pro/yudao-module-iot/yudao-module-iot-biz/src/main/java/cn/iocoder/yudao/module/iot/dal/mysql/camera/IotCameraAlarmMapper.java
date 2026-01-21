package cn.iocoder.yudao.module.iot.dal.mysql.camera;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraAlarmDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * IoT 摄像头报警记录 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotCameraAlarmMapper extends BaseMapperX<IotCameraAlarmDO> {

    /**
     * 分页查询报警记录
     *
     * @param pageReqVO 分页参数
     * @param cameraId 摄像头ID（可选）
     * @param alarmType 报警类型（可选）
     * @param alarmLevel 报警级别（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    default PageResult<IotCameraAlarmDO> selectPage(PageParam pageReqVO,
                                                     Long cameraId, String alarmType,
                                                     Integer alarmLevel, Integer status,
                                                     LocalDateTime startTime, LocalDateTime endTime) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotCameraAlarmDO>()
                .eqIfPresent(IotCameraAlarmDO::getCameraId, cameraId)
                .eqIfPresent(IotCameraAlarmDO::getAlarmType, alarmType)
                .eqIfPresent(IotCameraAlarmDO::getAlarmLevel, alarmLevel)
                .eqIfPresent(IotCameraAlarmDO::getStatus, status)
                .geIfPresent(IotCameraAlarmDO::getAlarmTime, startTime)
                .leIfPresent(IotCameraAlarmDO::getAlarmTime, endTime)
                .orderByDesc(IotCameraAlarmDO::getAlarmTime));
    }

    /**
     * 统计未处理报警数量
     *
     * @param cameraId 摄像头ID（可选）
     * @return 未处理报警数量
     */
    default Long countUnhandled(Long cameraId) {
        return selectCount(new LambdaQueryWrapperX<IotCameraAlarmDO>()
                .eqIfPresent(IotCameraAlarmDO::getCameraId, cameraId)
                .eq(IotCameraAlarmDO::getStatus, 0)); // 0:未处理
    }

}

