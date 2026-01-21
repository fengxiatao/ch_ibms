package cn.iocoder.yudao.module.iot.service.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotDeviceChannelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotDeviceChannelSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.NvrWithChannelsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * IoT 设备通道 Service 接口
 *
 * @author IBMS Team
 */
public interface IotDeviceChannelService {

    /**
     * 创建通道
     *
     * @param createReqVO 创建信息
     * @return 通道ID
     */
    Long createChannel(@Valid IotDeviceChannelSaveReqVO createReqVO);

    /**
     * 更新通道
     *
     * @param updateReqVO 更新信息
     */
    void updateChannel(@Valid IotDeviceChannelSaveReqVO updateReqVO);

    /**
     * 删除通道
     *
     * @param id 通道ID
     */
    void deleteChannel(Long id);

    /**
     * 获得通道
     *
     * @param id 通道ID
     * @return 通道
     */
    IotDeviceChannelDO getChannel(Long id);

    /**
     * 获得通道分页
     *
     * @param pageReqVO 分页查询
     * @return 通道分页
     */
    PageResult<IotDeviceChannelDO> getChannelPage(IotDeviceChannelPageReqVO pageReqVO);

    /**
     * 获取设备的所有通道
     *
     * @param deviceId 设备ID
     * @return 通道列表
     */
    List<IotDeviceChannelDO> getChannelsByDeviceId(Long deviceId);

    /**
     * 根据设备ID和通道号获取通道
     *
     * @param deviceId  设备ID
     * @param channelNo 通道号
     * @return 通道信息
     */
    IotDeviceChannelDO getChannelByDeviceIdAndChannelNo(Long deviceId, Integer channelNo);

    /**
     * 获取视频通道列表
     *
     * @param deviceType   设备类型
     * @param onlineStatus 在线状态
     * @param isPatrol     是否巡更
     * @param isMonitor    是否监控墙
     * @return 视频通道列表
     */
    List<IotDeviceChannelDO> getVideoChannels(String deviceType, Integer onlineStatus, Boolean isPatrol, Boolean isMonitor);

    /**
     * 获取巡更通道列表
     *
     * @return 巡更通道列表
     */
    List<IotDeviceChannelDO> getPatrolChannels();

    /**
     * 获取监控墙通道列表
     *
     * @return 监控墙通道列表
     */
    List<IotDeviceChannelDO> getMonitorChannels();

    /**
     * 同步设备通道
     *
     * @param deviceId 设备ID
     * @return 同步的通道数量
     */
    Integer syncDeviceChannels(Long deviceId);

    /**
     * 批量启用通道
     *
     * @param channelIds 通道ID列表
     */
    void batchEnableChannels(List<Long> channelIds);

    /**
     * 批量禁用通道
     *
     * @param channelIds 通道ID列表
     */
    void batchDisableChannels(List<Long> channelIds);

    /**
     * 批量设置巡更
     *
     * @param channelIds 通道ID列表
     * @param isPatrol   是否巡更
     */
    void batchSetPatrol(List<Long> channelIds, Boolean isPatrol);

    /**
     * 批量设置监控墙
     *
     * @param channelIds 通道ID列表
     * @param isMonitor  是否监控墙
     */
    void batchSetMonitor(List<Long> channelIds, Boolean isMonitor);

    /**
     * 批量指派空间（园区/建筑/楼层）
     * @param channelIds 通道ID列表
     * @param campusId 园区ID
     * @param buildingId 建筑ID
     * @param floorId 楼层ID
     */
    void batchAssignSpatial(List<Long> channelIds, Long campusId, Long buildingId, Long floorId, Long areaId);

    // ========== 多屏预览专用 ==========

    /**
     * 获取NVR通道列表（自动同步）
     * 优先从数据库获取，如果没有或需要强制同步，则通过SDK同步后返回
     *
     * @param deviceId  NVR设备ID
     * @param forceSync 是否强制同步
     * @return 通道列表
     */
    List<IotDeviceChannelDO> getNvrChannelsWithAutoSync(Long deviceId, Boolean forceSync);

    /**
     * 获取所有NVR及其通道
     * 用于多屏预览，返回NVR设备及其通道列表
     *
     * @return NVR及通道列表
     */
    List<NvrWithChannelsRespVO> getAllNvrsWithChannels();

    /**
     * 批量同步所有NVR通道
     * 用于定时任务
     *
     * @return 同步结果统计
     */
    SyncResult batchSyncAllNvrChannels();

    // ========== 门禁通道同步 ==========

    /**
     * 同步门禁设备通道
     * <p>
     * 从 newgateway 返回的通道信息同步到数据库：
     * <ul>
     *     <li>新增：数据库中不存在的通道</li>
     *     <li>更新：数据库中已存在的通道</li>
     *     <li>软删除：数据库中存在但设备上不存在的通道</li>
     * </ul>
     *
     * @param deviceId    设备ID
     * @param channelList 通道信息列表（来自 newgateway）
     * @return 同步结果
     */
    AccessChannelSyncResult syncAccessChannels(Long deviceId, List<AccessChannelSyncInfo> channelList);

    // ========== NVR 通道同步（来自 newgateway） ==========

    /**
     * 同步 NVR 设备通道（来自 newgateway 的 QUERY_CHANNELS / SCAN_CHANNELS 结果）
     *
     * <p>用途：</p>
     * <ul>
     *     <li>newgateway 扫描/查询 NVR 通道后，通过 DEVICE_SERVICE_RESULT 回传</li>
     *     <li>biz 消费后将通道信息落库到 iot_device_channel</li>
     * </ul>
     *
     * @param deviceId    NVR 设备ID
     * @param channelList 通道信息列表（来自 newgateway）
     * @return 同步结果（复用 AccessChannelSyncResult 结构：inserted/updated/deleted）
     */
    AccessChannelSyncResult syncNvrChannels(Long deviceId, List<NvrChannelSyncInfo> channelList);

    /**
     * NVR 通道同步信息（用于接收 newgateway 返回的通道数据）
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class NvrChannelSyncInfo {
        /** 通道号（一般从 0 或 1 开始，取决于设备/SDK；biz 侧统一按 device 返回为准） */
        private Integer channelNo;
        /** 通道名称 */
        private String channelName;
        /** 是否在线 */
        private Boolean online;
        /** 是否正在录像（可选） */
        private Boolean recording;
        /** 原始能力/扩展字段 */
        private java.util.Map<String, Object> capabilities;
    }

    /**
     * 门禁通道同步信息
     * <p>
     * 用于接收 newgateway 返回的通道数据
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class AccessChannelSyncInfo {
        /** 通道号 */
        private Integer channelNo;
        /** 通道名称 */
        private String channelName;
        /** 通道类型 (ACCESS) */
        private String channelType;
        /** 状态: 0-离线/关闭, 1-在线/打开, 2-未知 */
        private Integer status;
        /** 能力信息 */
        private java.util.Map<String, Object> capabilities;
    }

    /**
     * 门禁通道同步结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class AccessChannelSyncResult {
        /** 是否成功 */
        private boolean success;
        /** 新增数量 */
        private int insertedCount;
        /** 更新数量 */
        private int updatedCount;
        /** 软删除数量 */
        private int deletedCount;
        /** 错误消息 */
        private String errorMessage;
        /** 同步时间 */
        private java.time.LocalDateTime syncTime;

        public static AccessChannelSyncResult success(int inserted, int updated, int deleted) {
            return AccessChannelSyncResult.builder()
                    .success(true)
                    .insertedCount(inserted)
                    .updatedCount(updated)
                    .deletedCount(deleted)
                    .syncTime(java.time.LocalDateTime.now())
                    .build();
        }

        public static AccessChannelSyncResult failure(String errorMessage) {
            return AccessChannelSyncResult.builder()
                    .success(false)
                    .errorMessage(errorMessage)
                    .syncTime(java.time.LocalDateTime.now())
                    .build();
        }
    }
}
