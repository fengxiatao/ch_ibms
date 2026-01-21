package cn.iocoder.yudao.module.iot.service.camera;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.camera.vo.IotCameraForGatewayRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.camera.vo.IotCameraPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.camera.vo.IotCameraSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * IoT 摄像头管理 Service接口
 *
 * @author 长辉信息
 */
public interface IotCameraService {

    /**
     * 创建摄像头配置
     *
     * @param createReqVO 创建信息
     * @return 摄像头ID
     */
    Long createCamera(@Valid IotCameraSaveReqVO createReqVO);

    /**
     * 更新摄像头配置
     *
     * @param updateReqVO 更新信息
     */
    void updateCamera(@Valid IotCameraSaveReqVO updateReqVO);

    /**
     * 删除摄像头配置
     *
     * @param id 摄像头ID
     */
    void deleteCamera(Long id);

    /**
     * 获得摄像头配置
     *
     * @param id 摄像头ID
     * @return 摄像头配置
     */
    IotCameraDO getCamera(Long id);

    /**
     * 根据设备ID获取摄像头配置
     *
     * @param deviceId 设备ID
     * @return 摄像头配置
     */
    IotCameraDO getCameraByDeviceId(Long deviceId);

    /**
     * 获得摄像头配置分页
     *
     * @param pageReqVO 分页查询
     * @return 摄像头配置分页
     */
    PageResult<IotCameraDO> getCameraPage(IotCameraPageReqVO pageReqVO);

    /**
     * 测试摄像头连接
     *
     * @param id 摄像头ID
     * @return 是否连接成功
     */
    Boolean testConnection(Long id);

    /**
     * 获取视频流地址
     *
     * @param id 摄像头ID
     * @param streamType 码流类型(main:主码流, sub:子码流)
     * @return 视频流地址
     */
    String getStreamUrl(Long id, String streamType);

    /**
     * 获取所有摄像头设备列表（供网关使用）
     * 
     * <p>网关启动时调用此接口批量拉取设备信息，用于初始化设备注册
     *
     * @return 摄像头设备列表
     */
    List<IotCameraForGatewayRespVO> listForGateway();

}

