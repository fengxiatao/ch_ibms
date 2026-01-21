package cn.iocoder.yudao.module.iot.service.security;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.PlayUrlRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.SecurityOverviewCameraPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.SecurityOverviewCameraRespVO;

/**
 * IoT 安防概览 Service 接口
 *
 * @author 芋道源码
 */
public interface IotSecurityOverviewService {

    /**
     * 获取安防概览摄像头列表（包含实时抓图）
     *
     * @param reqVO 分页请求参数
     * @return 摄像头分页结果
     */
    PageResult<SecurityOverviewCameraRespVO> getSecurityOverviewCameras(SecurityOverviewCameraPageReqVO reqVO);

    /**
     * 获取单个设备的实时抓图
     *
     * @param deviceId 设备ID
     * @return Base64编码的图片数据
     */
    String getDeviceSnapshot(Long deviceId);

    /**
     * 获取设备播放地址
     * 
     * @param deviceId 设备ID
     * @return 播放地址信息（HLS/FLV/RTMP）
     */
    PlayUrlRespVO getPlayUrl(Long deviceId);
}

