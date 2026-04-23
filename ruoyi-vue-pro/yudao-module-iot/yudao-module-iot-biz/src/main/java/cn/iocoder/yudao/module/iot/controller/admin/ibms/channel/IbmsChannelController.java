package cn.iocoder.yudao.module.iot.controller.admin.ibms.channel;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotChannelAssignSpatialReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsDeviceTreeNodeRespVO;
import cn.iocoder.yudao.module.iot.service.channel.SyncResult;
import cn.iocoder.yudao.module.iot.service.ibms.channel.IbmsChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IBMS 通道管理
 */
@Tag(name = "管理后台 - IBMS 通道管理")
@RestController
@RequestMapping("/iot/ibms/channel")
@Validated
public class IbmsChannelController {

    @Resource
    private IbmsChannelService ibmsChannelService;

    @PostMapping("/create")
    @Operation(summary = "创建通道")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:create')")
    public CommonResult<Long> createChannel(@Valid @RequestBody IbmsChannelSaveReqVO reqVO) {
        return success(ibmsChannelService.createChannel(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新通道")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:update')")
    public CommonResult<Boolean> updateChannel(@Valid @RequestBody IbmsChannelSaveReqVO reqVO) {
        ibmsChannelService.updateChannel(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除通道")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:delete')")
    public CommonResult<Boolean> deleteChannel(@RequestParam("id") Long id) {
        ibmsChannelService.deleteChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取通道详情")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:query')")
    public CommonResult<IbmsChannelRespVO> getChannel(@RequestParam("id") Long id) {
        return success(ibmsChannelService.getChannel(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询通道")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:query')")
    public CommonResult<PageResult<IbmsChannelRespVO>> getChannelPage(@Valid IbmsChannelPageReqVO reqVO) {
        return success(ibmsChannelService.getChannelPage(reqVO));
    }

    @GetMapping("/list-by-device")
    @Operation(summary = "按设备查询通道列表（不分页）")
    @Parameter(name = "deviceId", description = "IBMS 设备 ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:query')")
    public CommonResult<List<IbmsChannelRespVO>> listByDevice(@RequestParam("deviceId") Long deviceId) {
        return success(ibmsChannelService.listChannelsByDeviceId(deviceId));
    }

    @PostMapping("/sync-from-device")
    @Operation(summary = "从设备同步通道（NVR）")
    @Parameter(name = "deviceId", description = "IBMS 设备 ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:create')")
    public CommonResult<List<IbmsChannelRespVO>> syncFromDevice(@RequestParam("deviceId") Long deviceId) {
        return success(ibmsChannelService.syncChannelsFromDevice(deviceId));
    }

    // ========== 视频 / 巡更 / 监控墙 / 批量（与 /iot/channel 语义对齐，数据来自 ibms_channel） ==========

    @GetMapping("/video/list")
    @Operation(summary = "视频通道列表（IBMS）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:query')")
    public CommonResult<List<IbmsChannelRespVO>> getVideoChannels(
            @RequestParam(value = "deviceType", required = false) String deviceType,
            @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
            @RequestParam(value = "isPatrol", required = false) Boolean isPatrol,
            @RequestParam(value = "isMonitor", required = false) Boolean isMonitor) {
        return success(ibmsChannelService.listVideoChannels(deviceType, onlineStatus, isPatrol, isMonitor));
    }

    @GetMapping("/video/patrol")
    @Operation(summary = "巡更通道列表（IBMS）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:query')")
    public CommonResult<List<IbmsChannelRespVO>> getPatrolChannels() {
        return success(ibmsChannelService.listPatrolChannels());
    }

    @GetMapping("/video/monitor")
    @Operation(summary = "监控墙通道列表（IBMS）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:query')")
    public CommonResult<List<IbmsChannelRespVO>> getMonitorChannels() {
        return success(ibmsChannelService.listMonitorChannels());
    }

    @GetMapping("/device-tree")
    @Operation(summary = "设备+通道树（默认 system_code=VI）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:query')")
    public CommonResult<List<IbmsDeviceTreeNodeRespVO>> getDeviceTree(
            @RequestParam(value = "deviceType", required = false) String deviceType,
            @RequestParam(value = "channelType", required = false) String channelType,
            @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return success(ibmsChannelService.getDeviceTree(deviceType, channelType, onlineStatus, keyword));
    }

    @PostMapping("/sync-all-nvr")
    @Operation(summary = "批量同步所有 NVR 通道（委托既有实现）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:create')")
    public CommonResult<SyncResult> syncAllNvrChannels() {
        return success(ibmsChannelService.batchSyncAllNvrChannels());
    }

    @PostMapping("/batch/enable")
    @Operation(summary = "批量启用通道（extra.enableStatus）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:update')")
    public CommonResult<Boolean> batchEnableChannels(@RequestBody List<Long> channelIds) {
        ibmsChannelService.batchEnableChannels(channelIds);
        return success(true);
    }

    @PostMapping("/batch/disable")
    @Operation(summary = "批量禁用通道（extra.enableStatus）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:update')")
    public CommonResult<Boolean> batchDisableChannels(@RequestBody List<Long> channelIds) {
        ibmsChannelService.batchDisableChannels(channelIds);
        return success(true);
    }

    @PostMapping("/batch/patrol")
    @Operation(summary = "批量设置巡更（extra.isPatrol）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:update')")
    public CommonResult<Boolean> batchSetPatrol(
            @RequestParam("channelIds") List<Long> channelIds,
            @RequestParam("isPatrol") Boolean isPatrol) {
        ibmsChannelService.batchSetPatrol(channelIds, isPatrol);
        return success(true);
    }

    @PostMapping("/batch/monitor")
    @Operation(summary = "批量设置监控墙（extra.isMonitor）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:update')")
    public CommonResult<Boolean> batchSetMonitor(
            @RequestParam("channelIds") List<Long> channelIds,
            @RequestParam("isMonitor") Boolean isMonitor) {
        ibmsChannelService.batchSetMonitor(channelIds, isMonitor);
        return success(true);
    }

    @PostMapping("/batch/assign-spatial")
    @Operation(summary = "批量指派空间（GIS 园区/楼栋/楼层/区域 → ibms_channel.space 与 extra.gis*，space_id 依赖 ibms_space.extra 映射）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:update')")
    public CommonResult<Boolean> batchAssignSpatial(@Valid @RequestBody IotChannelAssignSpatialReqVO reqVO) {
        ibmsChannelService.batchAssignSpatial(reqVO.getChannelIds(), reqVO.getCampusId(), reqVO.getBuildingId(),
                reqVO.getFloorId(), reqVO.getAreaId());
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出通道 Excel（预留）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-channel:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExcel() {
        throw new UnsupportedOperationException("IBMS 通道导出暂未实现");
    }
}

