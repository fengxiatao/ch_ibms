package cn.iocoder.yudao.module.iot.service.ibms.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceSimpleRespVO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * IBMS 设备管理 Service 接口。
 * <p>网关侧认证、列表、批量运行态写入等能力由 {@link IbmsDeviceGatewaySupportService} 提供。</p>
 */
public interface IbmsDeviceService {

    /**
     * 创建设备
     */
    Long createDevice(IbmsDeviceSaveReqVO reqVO);

    /**
     * 更新设备
     */
    void updateDevice(IbmsDeviceSaveReqVO reqVO);

    /**
     * 删除设备
     */
    void deleteDevice(Long id);

    /**
     * 获取设备详情
     */
    IbmsDeviceRespVO getDevice(Long id);

    /**
     * 分页查询设备
     */
    PageResult<IbmsDeviceRespVO> getDevicePage(IbmsDevicePageReqVO reqVO);

    /**
     * 同步设备对应点位的运行态。
     */
    void syncRuntime(Long id, boolean online, Integer pointsAlarm);

    /**
     * 将当前租户下全部 IBMS 设备的台账快照经 MQ 重推给 NewGateway（{@code DEVICE_PROFILE_CHANGED} UPSERT），
     * 用于网关心跳缓存预热或冷启动后对齐。
     *
     * @return 成功发布条数
     */
    int repushAllGatewayProfiles();

    /**
     * 按台账 {@code ip} 列判断设备是否已存在（忽略租户插件），用于发现等无租户上下文场景。
     * <p>双轨期间请与 {@link cn.iocoder.yudao.module.iot.service.device.IotDeviceService#isDeviceExistsByIp} 组合判断。</p>
     */
    boolean isDeviceExistsByIp(String ip);

    /**
     * 批量更新 IBMS 台账分组（{@code ibms_device.group_ids}），并尽力重推网关 Profile。
     */
    void updateDeviceGroup(Set<Long> deviceIds, Set<Long> groupIds);

    /**
     * IBMS 单台账设备数量：{@code ibms_device.ibms_product_id}。
     */
    long countDevicesByProduct(Long ibmsProductId);

    /**
     * IBMS 侧精简设备列表（下拉等），含运行态 state。
     */
    List<IbmsDeviceSimpleRespVO> listSimpleDevices(Integer deviceType, Long ibmsProductId);

    /**
     * 批量删除 IBMS 设备（含运行态、通道策略与 {@link #deleteDevice(Long)} 一致）。
     */
    void deleteDeviceList(Collection<Long> ids);
}

