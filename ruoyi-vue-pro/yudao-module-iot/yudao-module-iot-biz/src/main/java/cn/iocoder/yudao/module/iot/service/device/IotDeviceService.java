package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.*;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import jakarta.validation.Valid;

import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * IoT 设备 Service 接口
 *
 * @author 长辉信息科技有限公司
 */
public interface IotDeviceService {

    /**
     * 【管理员】创建设备
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDevice(@Valid IotDeviceSaveReqVO createReqVO);

    /**
     * 更新设备
     *
     * @param updateReqVO 更新信息
     */
    void updateDevice(@Valid IotDeviceSaveReqVO updateReqVO);

    // TODO @长辉开发团队：先这么实现。未来看情况，要不要自己实现

    /**
     * 更新设备的所属网关
     *
     * @param id        编号
     * @param gatewayId 网关设备 ID
     */
    default void updateDeviceGateway(Long id, Long gatewayId) {
        IotDeviceSaveReqVO vo = new IotDeviceSaveReqVO();
        vo.setId(id);
        vo.setGatewayId(gatewayId);
        updateDevice(vo);
    }

    /**
     * 更新设备状态
     *
     * @param device 设备信息
     * @param state 状态
     */
    void updateDeviceState(IotDeviceDO device, Integer state);

    /**
     * 更新设备状态
     *
     * @param id    编号
     * @param state 状态
     */
    void updateDeviceState(Long id, Integer state);

    /**
     * 更新设备 config（iot_device.config）
     *
     * <p>用于写回设备能力快照等“业务侧需要的设备元信息”，并触发缓存清理。</p>
     *
     * @param deviceId 设备 ID
     * @param config   设备配置对象
     */
    void updateDeviceConfig(Long deviceId, DeviceConfig config);

    /**
     * 更新设备分组
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceGroup(@Valid IotDeviceUpdateGroupReqVO updateReqVO);

    /**
     * 删除单个设备
     *
     * @param id 编号
     */
    void deleteDevice(Long id);

    /**
     * 删除多个设备
     *
     * @param ids 编号数组
     */
    void deleteDeviceList(Collection<Long> ids);

    /**
     * 校验设备是否存在
     *
     * @param id 设备 ID
     * @return 设备对象
     */
    IotDeviceDO validateDeviceExists(Long id);

    /**
     * 【缓存】校验设备是否存在
     *
     * @param id 设备 ID
     * @return 设备对象
     */
    IotDeviceDO validateDeviceExistsFromCache(Long id);

    /**
     * 获得设备
     *
     * @param id 编号
     * @return IoT 设备
     */
    IotDeviceDO getDevice(Long id);

    /**
     * 【缓存】获得设备信息
     * <p>
     * 注意：该方法会忽略租户信息，所以调用时，需要确认会不会有跨租户访问的风险！！！
     *
     * @param id 编号
     * @return IoT 设备
     */
    IotDeviceDO getDeviceFromCache(Long id);

    /**
     * 【缓存】根据产品 key 和设备名称，获得设备信息
     * <p>
     * 注意：该方法会忽略租户信息，所以调用时，需要确认会不会有跨租户访问的风险！！！
     *
     * @param productKey 产品 key
     * @param deviceName 设备名称
     * @return 设备信息
     */
    IotDeviceDO getDeviceFromCache(String productKey, String deviceName);

    /**
     * 根据序列号获得设备信息
     *
     * @param serialNumber 序列号
     * @return 设备信息
     */
    IotDeviceDO getDeviceBySerialNumber(String serialNumber);

    /**
     * 【缓存】根据设备密钥获得设备信息
     * <p>
     * 注意：该方法会忽略租户信息，所以调用时，需要确认会不会有跨租户访问的风险！！！
     *
     * @param deviceKey 设备密钥
     * @return 设备信息
     */
    IotDeviceDO getDeviceFromCacheByDeviceKey(String deviceKey);

    /**
     * 获得设备分页
     *
     * @param pageReqVO 分页查询
     * @return IoT 设备分页
     */
    PageResult<IotDeviceDO> getDevicePage(IotDevicePageReqVO pageReqVO);

    /**
     * 填充设备实时在线状态（从Gateway查询）
     *
     * @param devices 设备列表
     */
    void fillDeviceRealTimeStatus(List<IotDeviceRespVO> devices);

    /**
     * 根据条件，获得设备列表
     *
     * @param deviceType 设备类型
     * @param productId 产品编号
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListByCondition(@Nullable Integer deviceType,
                                               @Nullable Long productId);

    /**
     * 获得状态，获得设备列表
     *
     * @param state 状态
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListByState(Integer state);

    /**
     * 根据产品编号，获取设备列表
     *
     * @param productId 产品编号
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListByProductId(Long productId);

    /**
     * 基于产品编号，获得设备数量
     *
     * @param productId 产品编号
     * @return 设备数量
     */
    Long getDeviceCountByProductId(Long productId);

    /**
     * 获得设备数量
     *
     * @param groupId 分组编号
     * @return 设备数量
     */
    Long getDeviceCountByGroupId(Long groupId);

    /**
     * 导入设备
     *
     * @param importDevices 导入设备列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    IotDeviceImportRespVO importDevice(List<IotDeviceImportExcelVO> importDevices, boolean updateSupport);

    /**
     * 获得设备数量
     *
     * @param createTime 创建时间，如果为空，则统计所有设备数量
     * @return 设备数量
     */
    Long getDeviceCount(@Nullable LocalDateTime createTime);

    /**
     * 获得设备认证信息
     *
     * @param id 设备编号
     * @return MQTT 连接参数
     */
    IotDeviceAuthInfoRespVO getDeviceAuthInfo(Long id);

    /**
     * 获得各个产品下的设备数量 Map
     *
     * @return key: 产品编号, value: 设备数量
     */
    Map<Long, Integer> getDeviceCountMapByProductId();

    /**
     * 获得各个状态下的设备数量 Map
     *
     * @return key: 设备状态枚举 {@link IotDeviceStateEnum}
     *         value: 设备数量
     */
    Map<Integer, Long> getDeviceCountMapByState();

    /**
     * 通过产品标识和设备名称列表获取设备列表
     *
     * @param productKey  产品标识
     * @param deviceNames 设备名称列表
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListByProductKeyAndNames(String productKey, List<String> deviceNames);

    /**
     * 认证设备
     *
     * @param authReqDTO 认证信息
     * @return 是否认证成功
     */
    boolean authDevice(@Valid IotDeviceAuthReqDTO authReqDTO);

    /**
     * 校验设备是否存在
     *
     * @param ids 设备编号数组
     */
    List<IotDeviceDO> validateDeviceListExists(Collection<Long> ids);

    /**
     * 获得设备列表
     *
     * @param ids 设备编号数组
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceList(Collection<Long> ids);

    /**
     * 获取所有设备列表（不限状态和类型）
     * 用于 Gateway 启动时初始化所有设备
     * <p>
     * 注意：此方法使用 @TenantIgnore，会返回所有租户的设备。
     * 如果需要按租户过滤，请使用 {@link #getDeviceListWithTenantFilter()}
     * </p>
     *
     * @return 所有设备列表
     */
    List<IotDeviceDO> getDeviceList();

    /**
     * 获取当前租户的设备列表（不限状态和类型）
     * <p>
     * 此方法会根据租户上下文自动过滤，只返回当前租户的设备。
     * 配合 {@code TenantUtils.execute(tenantId, ...)} 使用，实现按租户查询。
     * </p>
     * <p>
     * 用于单租户本地化部署的 Gateway 启动时初始化设备。
     * </p>
     *
     * @return 当前租户的设备列表
     */
    List<IotDeviceDO> getDeviceListWithTenantFilter();

    /**
     * 获得设备 Map
     *
     * @param ids 设备编号数组
     * @return 设备 Map
     */
    default Map<Long, IotDeviceDO> getDeviceMap(Collection<Long> ids) {
        return convertMap(getDeviceList(ids), IotDeviceDO::getId);
    }

    /**
     * 检查设备是否存在（根据IP）
     *
     * @param ip IP地址
     * @return 是否存在
     */
    boolean isDeviceExistsByIp(String ip);

    /**
     * 更新设备固件版本
     *
     * @param deviceId 设备编号
     * @param firmwareId 固件编号
     */
    void updateDeviceFirmware(Long deviceId, Long firmwareId);

    /**
     * 获取所有配置了 jobConfig 的设备
     * 
     * @return 设备列表（包含 id 和 jobConfig 字段）
     */
    List<IotDeviceDO> getDevicesWithJobConfig();

    /**
     * 更新设备的定时任务配置
     *
     * @param id 设备ID
     * @param jobConfig 任务配置JSON
     */
    void updateDeviceJobConfig(Long id, String jobConfig);

    /**
     * 获取设备的实际菜单配置（考虑继承产品配置）
     *
     * @param deviceId 设备ID
     * @return 菜单配置
     */
    IotDeviceMenuConfigVO getDeviceMenuConfig(Long deviceId);

    /**
     * 批量获取设备的实际菜单配置
     *
     * @param deviceIds 设备ID列表
     * @return 设备ID -> 菜单配置的映射
     */
    Map<Long, IotDeviceMenuConfigVO> getDeviceMenuConfigBatch(List<Long> deviceIds);

    /**
     * 获取所有在线设备列表（供Gateway使用）
     *
     * @return 在线设备列表
     */
    List<IotDeviceDO> getOnlineDeviceList();

    /**
     * 根据子系统代码查询设备列表
     * 
     * <p>用于查询特定子系统（如门禁、视频等）的所有设备，不限状态。</p>
     * 
     * @param subsystemCode 子系统代码（如 "access" 表示门禁设备）
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListBySubsystemCode(String subsystemCode);

    /**
     * 更新设备状态（带时间戳）
     * 
     * <p>根据状态类型更新对应的时间字段：</p>
     * <ul>
     *   <li>ONLINE: 更新 onlineTime，首次上线同时更新 activeTime</li>
     *   <li>OFFLINE: 更新 offlineTime</li>
     * </ul>
     * 
     * <p>Requirements: 1.1, 1.2, 1.3</p>
     *
     * @param deviceId  设备ID
     * @param newState  新状态
     * @param timestamp 状态变更时间戳（毫秒）
     */
    void updateDeviceStateWithTimestamp(Long deviceId, Integer newState, Long timestamp);

    /**
     * 更新设备视频能力配置
     * 
     * <p>在设备激活成功后，将视频能力信息保存到设备配置中。</p>
     * 
     * <p>Requirements: 1.2 - WHEN 设备支持视频功能 THEN THE Access_System SHALL 在设备配置中标记 supportVideo=true</p>
     *
     * @param deviceId          设备ID
     * @param supportVideo      是否支持视频预览
     * @param videoChannelCount 视频通道数量
     * @param httpPort          HTTP端口（用于RTSP over WebSocket）
     * @param rtspPort          RTSP端口
     */
    void updateDeviceVideoCapability(Long deviceId, Boolean supportVideo, Integer videoChannelCount, 
                                      Integer httpPort, Integer rtspPort);

}
