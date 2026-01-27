package cn.iocoder.yudao.module.iot.controller.admin.channel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotDeviceChannelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotDeviceChannelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotDeviceChannelSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.NvrWithChannelsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotChannelAssignSpatialReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.channel.SyncResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 设备通道 Controller
 *
 * @author IBMS Team
 */
@Tag(name = "管理后台 - IoT 设备通道")
@RestController
@RequestMapping("/iot/channel")
@Validated
public class IotDeviceChannelController {

    @Resource
    private IotDeviceChannelService channelService;

    // ========== 基础 CRUD ==========

    @PostMapping("/create")
    @Operation(summary = "创建设备通道")
    @PreAuthorize("@ss.hasPermission('iot:channel:create')")
    public CommonResult<Long> createChannel(@Valid @RequestBody IotDeviceChannelSaveReqVO createReqVO) {
        return success(channelService.createChannel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备通道")
    @PreAuthorize("@ss.hasPermission('iot:channel:update')")
    public CommonResult<Boolean> updateChannel(@Valid @RequestBody IotDeviceChannelSaveReqVO updateReqVO) {
        channelService.updateChannel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备通道")
    @Parameter(name = "id", description = "通道ID", required = true, example = "1001")
    @PreAuthorize("@ss.hasPermission('iot:channel:delete')")
    public CommonResult<Boolean> deleteChannel(@RequestParam("id") Long id) {
        channelService.deleteChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备通道")
    @Parameter(name = "id", description = "通道ID", required = true, example = "1001")
    @PreAuthorize("@ss.hasPermission('iot:channel:query')")
    public CommonResult<IotDeviceChannelRespVO> getChannel(@RequestParam("id") Long id) {
        IotDeviceChannelDO channel = channelService.getChannel(id);
        return success(BeanUtils.toBean(channel, IotDeviceChannelRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备通道分页")
    @PreAuthorize("@ss.hasPermission('iot:channel:query')")
    public CommonResult<PageResult<IotDeviceChannelRespVO>> getChannelPage(@Valid IotDeviceChannelPageReqVO pageReqVO) {
        PageResult<IotDeviceChannelDO> pageResult = channelService.getChannelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceChannelRespVO.class));
    }

    // ========== 设备通道查询 ==========

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "获取设备的所有通道")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "100")
    @PreAuthorize("@ss.hasPermission('iot:channel:query')")
    public CommonResult<List<IotDeviceChannelRespVO>> getDeviceChannels(@PathVariable("deviceId") Long deviceId) {
        List<IotDeviceChannelDO> channels = channelService.getChannelsByDeviceId(deviceId);
        return success(BeanUtils.toBean(channels, IotDeviceChannelRespVO.class));
    }

    // ========== 视频通道专用 ==========

    @GetMapping("/video/list")
    @Operation(summary = "获取视频通道列表")
    @PreAuthorize("@ss.hasPermission('iot:channel:query')")
    public CommonResult<List<IotDeviceChannelRespVO>> getVideoChannels(
            @RequestParam(value = "deviceType", required = false) String deviceType,
            @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
            @RequestParam(value = "isPatrol", required = false) Boolean isPatrol,
            @RequestParam(value = "isMonitor", required = false) Boolean isMonitor) {
        List<IotDeviceChannelDO> channels = channelService.getVideoChannels(deviceType, onlineStatus, isPatrol, isMonitor);
        return success(BeanUtils.toBean(channels, IotDeviceChannelRespVO.class));
    }

    @GetMapping("/video/patrol")
    @Operation(summary = "获取巡更通道列表")
    @PreAuthorize("@ss.hasPermission('iot:channel:query')")
    public CommonResult<List<IotDeviceChannelRespVO>> getPatrolChannels() {
        List<IotDeviceChannelDO> channels = channelService.getPatrolChannels();
        return success(BeanUtils.toBean(channels, IotDeviceChannelRespVO.class));
    }

    @GetMapping("/video/monitor")
    @Operation(summary = "获取监控墙通道列表")
    @PreAuthorize("@ss.hasPermission('iot:channel:query')")
    public CommonResult<List<IotDeviceChannelRespVO>> getMonitorChannels() {
        List<IotDeviceChannelDO> channels = channelService.getMonitorChannels();
        return success(BeanUtils.toBean(channels, IotDeviceChannelRespVO.class));
    }

    // ========== 通道同步 ==========

    @PostMapping("/sync/{deviceId}")
    @Operation(summary = "同步设备通道")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "100")
    @PreAuthorize("@ss.hasPermission('iot:channel:sync')")
    public CommonResult<Integer> syncDeviceChannels(@PathVariable("deviceId") Long deviceId) {
        Integer syncCount = channelService.syncDeviceChannels(deviceId);
        return success(syncCount);
    }

    @PostMapping("/sync-all-nvr")
    @Operation(summary = "批量同步所有NVR通道", description = "向所有在线NVR设备发送通道查询命令，更新通道名称等信息")
    @PreAuthorize("@ss.hasPermission('iot:channel:sync')")
    public CommonResult<SyncResult> syncAllNvrChannels() {
        SyncResult result = channelService.batchSyncAllNvrChannels();
        return success(result);
    }

    // ========== 批量操作 ==========

    @PostMapping("/batch/enable")
    @Operation(summary = "批量启用通道")
    @PreAuthorize("@ss.hasPermission('iot:channel:update')")
    public CommonResult<Boolean> batchEnableChannels(@RequestBody List<Long> channelIds) {
        channelService.batchEnableChannels(channelIds);
        return success(true);
    }

    @PostMapping("/batch/disable")
    @Operation(summary = "批量禁用通道")
    @PreAuthorize("@ss.hasPermission('iot:channel:update')")
    public CommonResult<Boolean> batchDisableChannels(@RequestBody List<Long> channelIds) {
        channelService.batchDisableChannels(channelIds);
        return success(true);
    }

    @PostMapping("/batch/patrol")
    @Operation(summary = "批量设置巡更")
    @PreAuthorize("@ss.hasPermission('iot:channel:update')")
    public CommonResult<Boolean> batchSetPatrol(
            @RequestParam("channelIds") List<Long> channelIds,
            @RequestParam("isPatrol") Boolean isPatrol) {
        channelService.batchSetPatrol(channelIds, isPatrol);
        return success(true);
    }

    @PostMapping("/batch/monitor")
    @Operation(summary = "批量设置监控墙")
    @PreAuthorize("@ss.hasPermission('iot:channel:update')")
    public CommonResult<Boolean> batchSetMonitor(
            @RequestParam("channelIds") List<Long> channelIds,
            @RequestParam("isMonitor") Boolean isMonitor) {
        channelService.batchSetMonitor(channelIds, isMonitor);
        return success(true);
    }

    @PostMapping("/batch/assign-spatial")
    @Operation(summary = "批量指派空间（园区/建筑/楼层）")
    @PreAuthorize("@ss.hasPermission('iot:channel:update')")
    public CommonResult<Boolean> batchAssignSpatial(@Valid @RequestBody IotChannelAssignSpatialReqVO reqVO) {
        channelService.batchAssignSpatial(reqVO.getChannelIds(), reqVO.getCampusId(), reqVO.getBuildingId(), reqVO.getFloorId(), reqVO.getAreaId());
        return success(true);
    }

    // ========== 多屏预览专用 ==========

    @GetMapping("/nvr/{deviceId}/channels")
    @Operation(summary = "获取NVR通道列表（自动同步）", description = "优先从数据库获取，如果没有则通过SDK同步后返回")
    @PreAuthorize("@ss.hasPermission('iot:channel:query')")
    public CommonResult<List<IotDeviceChannelRespVO>> getNvrChannelsWithAutoSync(
            @Parameter(description = "NVR设备ID", required = true) @PathVariable("deviceId") Long deviceId,
            @Parameter(description = "是否强制同步", required = false) @RequestParam(value = "forceSync", defaultValue = "false") Boolean forceSync) {
        List<IotDeviceChannelDO> channels = channelService.getNvrChannelsWithAutoSync(deviceId, forceSync);
        return success(BeanUtils.toBean(channels, IotDeviceChannelRespVO.class));
    }

    @GetMapping("/nvr/all-with-channels")
    @Operation(summary = "获取所有NVR及其通道", description = "用于多屏预览，返回NVR设备及其通道列表")
    @PreAuthorize("@ss.hasPermission('iot:channel:query')")
    public CommonResult<List<NvrWithChannelsRespVO>> getAllNvrsWithChannels() {
        return success(channelService.getAllNvrsWithChannels());
    }
}
