package cn.iocoder.yudao.module.iot.core.biz;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;

import java.util.List;

/**
 * IoT 设备通用 API
 *
 * @author haohao
 */
public interface IotDeviceCommonApi {

    /**
     * 设备认证
     *
     * @param authReqDTO 认证请求
     * @return 认证结果
     */
    CommonResult<Boolean> authDevice(IotDeviceAuthReqDTO authReqDTO);

    /**
     * 获取设备信息
     *
     * @param infoReqDTO 设备信息请求
     * @return 设备信息
     */
    CommonResult<IotDeviceRespDTO> getDevice(IotDeviceGetReqDTO infoReqDTO);

    /**
     * 获取所有在线设备
     * 用于Gateway启动时自动连接
     *
     * @return 在线设备列表
     */
    CommonResult<List<IotDeviceRespDTO>> getOnlineDevices();

    /**
     * 获取所有门禁设备（不限状态）
     * 用于 Gateway 启动时激活设备
     * 
     * <p>与 {@link #getOnlineDevices()} 不同，此方法返回所有门禁设备，
     * 无论其当前状态是在线、离线还是未激活。这样 Gateway 可以尝试激活
     * 所有配置的门禁设备，而不仅仅是已经在线的设备。</p>
     * 
     * @return 门禁设备列表（包含所有状态的设备）
     */
    CommonResult<List<IotDeviceRespDTO>> getAccessDevices();

    /**
     * 获取所有设备（不限状态和类型）
     * 用于 Gateway 启动时初始化所有设备
     * 
     * @return 所有设备列表
     */
    CommonResult<List<IotDeviceRespDTO>> getAllDevices();

    /**
     * 获取指定租户的所有设备
     * <p>
     * 用于单租户本地化部署的 Gateway 启动时初始化设备。
     * 只返回指定租户的设备，实现租户数据隔离。
     * </p>
     * 
     * @param tenantId 租户ID
     * @return 该租户的设备列表
     */
    CommonResult<List<IotDeviceRespDTO>> getDevicesByTenantId(Long tenantId);

    /**
     * 批量更新设备状态（同步调用）
     * <p>
     * 用于 Gateway 重启时同步更新设备状态到数据库，
     * 解决依赖 RocketMQ 异步消息可能丢失的问题。
     * </p>
     * 
     * @param deviceIds 设备ID列表
     * @param state     目标状态值（0=未激活, 1=在线, 2=离线）
     * @param reason    状态变更原因
     * @return 成功更新的设备数量
     */
    CommonResult<Integer> batchUpdateDeviceState(List<Long> deviceIds, Integer state, String reason);

}
