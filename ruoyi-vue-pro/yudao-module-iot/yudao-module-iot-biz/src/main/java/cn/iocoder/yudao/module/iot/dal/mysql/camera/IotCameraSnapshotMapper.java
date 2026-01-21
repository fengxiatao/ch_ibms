package cn.iocoder.yudao.module.iot.dal.mysql.camera;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraSnapshotDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 摄像头抓图记录 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotCameraSnapshotMapper extends BaseMapperX<IotCameraSnapshotDO> {

    /**
     * 分页查询抓图记录
     *
     * @param pageReqVO 分页参数
     * @param deviceId 设备ID（可选）
     * @param channelId 通道ID（可选）
     * @param snapshotType 抓图类型（可选）
     * @param eventType 事件类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param isProcessed 是否已处理（可选）
     * @return 分页结果
     */
    default PageResult<IotCameraSnapshotDO> selectPage(PageParam pageReqVO,
                                                        Long deviceId, Long channelId, java.util.List<Long> channelIds,
                                                        Integer snapshotType, String eventType, 
                                                        LocalDateTime startTime, LocalDateTime endTime, 
                                                        Boolean isProcessed) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotCameraSnapshotDO>()
                .eqIfPresent(IotCameraSnapshotDO::getDeviceId, deviceId)
                .eqIfPresent(IotCameraSnapshotDO::getChannelId, channelId)
                .inIfPresent(IotCameraSnapshotDO::getChannelId, channelIds)
                .eqIfPresent(IotCameraSnapshotDO::getSnapshotType, snapshotType)
                .eqIfPresent(IotCameraSnapshotDO::getEventType, eventType)
                .geIfPresent(IotCameraSnapshotDO::getCaptureTime, startTime)
                .leIfPresent(IotCameraSnapshotDO::getCaptureTime, endTime)
                .eqIfPresent(IotCameraSnapshotDO::getIsProcessed, isProcessed)
                .orderByDesc(IotCameraSnapshotDO::getCaptureTime));
    }

    /**
     * 查询设备的所有抓图记录
     *
     * @param deviceId 设备ID
     * @return 抓图记录列表
     */
    default List<IotCameraSnapshotDO> selectListByDeviceId(Long deviceId) {
        return selectList(new LambdaQueryWrapperX<IotCameraSnapshotDO>()
                .eq(IotCameraSnapshotDO::getDeviceId, deviceId)
                .orderByDesc(IotCameraSnapshotDO::getCaptureTime));
    }

    /**
     * 查询未处理的抓图记录数量
     *
     * @return 未处理数量
     */
    default Long selectUnprocessedCount() {
        return selectCount(new LambdaQueryWrapperX<IotCameraSnapshotDO>()
                .eq(IotCameraSnapshotDO::getIsProcessed, false));
    }

    /**
     * 查询通道最近一条抓图记录
     *
     * @param channelId 通道ID
     * @return 最近抓图
     */
    default IotCameraSnapshotDO selectLatestByChannelId(Long channelId) {
        return selectOne(new LambdaQueryWrapperX<IotCameraSnapshotDO>()
                .eq(IotCameraSnapshotDO::getChannelId, channelId)
                .orderByDesc(IotCameraSnapshotDO::getCaptureTime)
                .last("LIMIT 1"));
    }

}







