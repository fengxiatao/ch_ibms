package cn.iocoder.yudao.module.iot.dal.mysql.camera;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraPresetDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 摄像头预置位 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotCameraPresetMapper extends BaseMapperX<IotCameraPresetDO> {

    /**
     * 根据摄像头ID查询所有预置位
     *
     * @param cameraId 摄像头ID
     * @return 预置位列表
     */
    default List<IotCameraPresetDO> selectListByCameraId(Long cameraId) {
        return selectList(new LambdaQueryWrapperX<IotCameraPresetDO>()
                .eq(IotCameraPresetDO::getCameraId, cameraId)
                .orderByAsc(IotCameraPresetDO::getPresetId));
    }

    /**
     * 根据摄像头ID和预置位编号查询
     *
     * @param cameraId 摄像头ID
     * @param presetId 预置位编号
     * @return 预置位
     */
    default IotCameraPresetDO selectByCameraIdAndPresetId(Long cameraId, Integer presetId) {
        return selectOne(new LambdaQueryWrapperX<IotCameraPresetDO>()
                .eq(IotCameraPresetDO::getCameraId, cameraId)
                .eq(IotCameraPresetDO::getPresetId, presetId));
    }

    /**
     * 删除摄像头的所有预置位
     *
     * @param cameraId 摄像头ID
     * @return 删除数量
     */
    default int deleteByCameraId(Long cameraId) {
        return delete(new LambdaQueryWrapperX<IotCameraPresetDO>()
                .eq(IotCameraPresetDO::getCameraId, cameraId));
    }

}

