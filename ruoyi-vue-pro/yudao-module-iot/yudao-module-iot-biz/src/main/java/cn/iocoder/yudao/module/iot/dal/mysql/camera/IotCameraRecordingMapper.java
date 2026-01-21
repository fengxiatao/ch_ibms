package cn.iocoder.yudao.module.iot.dal.mysql.camera;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraRecordingDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 摄像头录像记录 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotCameraRecordingMapper extends BaseMapperX<IotCameraRecordingDO> {

    /**
     * 分页查询录像记录
     *
     * @param pageReqVO 分页参数
     * @param cameraId 摄像头ID（可选）
     * @param recordingType 录像类型（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    default PageResult<IotCameraRecordingDO> selectPage(PageParam pageReqVO,
                                                         Long cameraId, Integer recordingType,
                                                         Integer status, LocalDateTime startTime,
                                                         LocalDateTime endTime) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotCameraRecordingDO>()
                .eqIfPresent(IotCameraRecordingDO::getCameraId, cameraId)
                .eqIfPresent(IotCameraRecordingDO::getRecordingType, recordingType)
                .eqIfPresent(IotCameraRecordingDO::getStatus, status)
                .geIfPresent(IotCameraRecordingDO::getStartTime, startTime)
                .leIfPresent(IotCameraRecordingDO::getEndTime, endTime)
                .orderByDesc(IotCameraRecordingDO::getStartTime));
    }

    /**
     * 查询摄像头正在录像的记录
     *
     * @param cameraId 摄像头ID
     * @return 录像记录列表
     */
    default List<IotCameraRecordingDO> selectRecordingByCameraId(Long cameraId) {
        return selectList(new LambdaQueryWrapperX<IotCameraRecordingDO>()
                .eq(IotCameraRecordingDO::getCameraId, cameraId)
                .eq(IotCameraRecordingDO::getStatus, 0)); // 0:录像中
    }

}

