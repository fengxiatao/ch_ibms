package cn.iocoder.yudao.module.iot.service.device.status;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.status.DeviceStatusPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.status.DeviceStatusRespVO;

import java.util.List;

/**
 * 设备状态查询 Service 接口
 * 
 * <p>提供设备在线状态的查询功能</p>
 * 
 * <p>Requirements: 5.1, 5.2, 5.3</p>
 *
 * @author 长辉信息科技有限公司
 */
public interface DeviceStatusService {

    /**
     * 查询单个设备状态
     * 
     * <p>Requirements: 5.1 - 返回设备的当前状态和最后活跃时间</p>
     * <p>Requirements: 5.4 - 查询的设备不存在时返回 INACTIVE 状态</p>
     *
     * @param deviceId 设备编号
     * @return 设备状态信息
     */
    DeviceStatusRespVO getDeviceStatus(Long deviceId);

    /**
     * 批量查询设备状态
     * 
     * <p>Requirements: 5.2 - 返回所有请求设备的状态列表</p>
     * <p>注意：调用方需确保 deviceIds 数量不超过 100</p>
     *
     * @param deviceIds 设备编号列表
     * @return 设备状态列表
     */
    List<DeviceStatusRespVO> batchGetDeviceStatus(List<Long> deviceIds);

    /**
     * 分页查询设备状态
     * 
     * <p>Requirements: 5.3 - 支持按设备类型、状态、产品ID筛选</p>
     *
     * @param pageReqVO 分页查询条件
     * @return 设备状态分页结果
     */
    PageResult<DeviceStatusRespVO> getDeviceStatusPage(DeviceStatusPageReqVO pageReqVO);

}
