package cn.iocoder.yudao.module.iot.dal.mysql.camera;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 摄像头扩展信息 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface IotCameraMapper extends BaseMapperX<IotCameraDO> {

    /**
     * 根据设备ID查询摄像头配置
     *
     * @param deviceId 设备ID
     * @return 摄像头配置
     */
    default IotCameraDO selectByDeviceId(Long deviceId) {
        return selectOne(IotCameraDO::getDeviceId, deviceId);
    }

    /**
     * 分页查询摄像头列表
     *
     * @param pageReqVO 分页参数
     * @param deviceId 设备ID（可选）
     * @param manufacturer 厂商（可选）
     * @param ptzSupport PTZ支持（可选）
     * @return 分页结果
     */
    default PageResult<IotCameraDO> selectPage(PageParam pageReqVO,
                                               Long deviceId, String manufacturer, Boolean ptzSupport) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotCameraDO>()
                .eqIfPresent(IotCameraDO::getDeviceId, deviceId)
                .likeIfPresent(IotCameraDO::getManufacturer, manufacturer)
                .eqIfPresent(IotCameraDO::getPtzSupport, ptzSupport)
                .orderByDesc(IotCameraDO::getId));
    }

}

