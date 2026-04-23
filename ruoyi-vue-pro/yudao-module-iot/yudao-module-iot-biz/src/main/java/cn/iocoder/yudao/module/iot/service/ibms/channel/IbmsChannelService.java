package cn.iocoder.yudao.module.iot.service.ibms.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsDeviceTreeNodeRespVO;
import cn.iocoder.yudao.module.iot.service.channel.SyncResult;

import java.util.List;

/**
 * IBMS 通道管理 Service 接口
 */
public interface IbmsChannelService {

    Long createChannel(IbmsChannelSaveReqVO reqVO);

    void updateChannel(IbmsChannelSaveReqVO reqVO);

    void deleteChannel(Long id);

    IbmsChannelRespVO getChannel(Long id);

    PageResult<IbmsChannelRespVO> getChannelPage(IbmsChannelPageReqVO reqVO);

    /**
     * 按设备查询通道列表（不分页）。
     *
     * @param deviceId 设备 ID
     * @return 通道列表
     */
    List<IbmsChannelRespVO> listChannelsByDeviceId(Long deviceId);

    /**
     * 从设备（NVR）同步通道到 ibms_channel 表。
     * <p>
     * 流程：通过消息总线向 NVR 插件发送 {@code QUERY_CHANNELS}，消息中的 {@code deviceId} 与 {@code ibms_device.id}
     * 一致；等待结果后将通道 upsert 到 {@code ibms_channel}。接入参数以 {@code ibms_device.extra} 为准。
     * </p>
     *
     * @param deviceId IBMS 设备 ID（即网关侧 deviceId）
     * @return 同步后的通道列表
     */
    List<IbmsChannelRespVO> syncChannelsFromDevice(Long deviceId);

    /**
     * 按设备同步通道运行态（在线/离线/告警）。
     *
     * @param deviceId   设备 ID
     * @param online     是否在线
     * @param alarmCount 告警点位数量（可为空）
     */
    void syncRuntimeByDevice(Long deviceId, boolean online, Integer alarmCount);

    // ---------- 视频 / 巡更 / 监控墙（extra 承载 isPatrol、isMonitor、enableStatus、sort、monitorPosition）----------

    List<IbmsChannelRespVO> listVideoChannels(String deviceTypeCode, Integer onlineStatus, Boolean isPatrol, Boolean isMonitor);

    List<IbmsChannelRespVO> listPatrolChannels();

    List<IbmsChannelRespVO> listMonitorChannels();

    void batchEnableChannels(List<Long> channelIds);

    void batchDisableChannels(List<Long> channelIds);

    void batchSetPatrol(List<Long> channelIds, Boolean isPatrol);

    void batchSetMonitor(List<Long> channelIds, Boolean isMonitor);

    /** 委托历史 NVR 批量同步（仍写 ibms_channel / 兼容逻辑在 {@link cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService}） */
    SyncResult batchSyncAllNvrChannels();

    /**
     * 批量指派空间：写入 {@code ibms_channel.space} 位置文案、合并 {@code extra} 中 GIS 主键、并按 {@code ibms_space.extra.gis*} 映射设置 {@code space_id}（无映射则置空）。
     */
    void batchAssignSpatial(List<Long> channelIds, Long campusId, Long buildingId, Long floorId, Long areaId);

    /**
     * 设备（默认 system_code=VI）+ 下属 ibms_channel 树。
     */
    List<IbmsDeviceTreeNodeRespVO> getDeviceTree(String deviceTypeCode, String channelTypeCode, Integer onlineStatus, String keyword);
}

