package cn.iocoder.yudao.module.iot.service.device.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.service.IotDeviceServiceInvokeReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.service.IotDeviceServiceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceServiceLogDO;

/**
 * IoT 设备服务调用 Service 接口
 *
 * @author 长辉信息科技有限公司
 */
public interface IotDeviceServiceInvokeService {

    /**
     * 调用设备服务
     *
     * @param reqVO 服务调用请求
     * @return 请求ID
     */
    String invokeDeviceService(IotDeviceServiceInvokeReqVO reqVO);

    /**
     * 获取服务调用日志分页
     *
     * @param pageReqVO 分页查询参数
     * @return 服务调用日志分页
     */
    PageResult<IotDeviceServiceLogDO> getServiceLogPage(IotDeviceServiceLogPageReqVO pageReqVO);

    /**
     * 获取服务调用日志
     *
     * @param id 日志ID
     * @return 服务调用日志
     */
    IotDeviceServiceLogDO getServiceLog(Long id);

    /**
     * 处理服务调用结果（由ServiceResultConsumer调用）
     *
     * @param requestId 请求ID
     * @param statusCode 状态码
     * @param responseMessage 响应消息
     * @param responseData 响应数据（JSON字符串）
     * @param responseTime 响应时间（毫秒时间戳）
     */
    void handleServiceResult(String requestId, Integer statusCode, String responseMessage, 
                            String responseData, Long responseTime);
}












