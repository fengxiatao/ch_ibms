package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessChannelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDoorControlReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.service.access.IotAccessChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁通道管理 Controller
 */
@Tag(name = "管理后台 - 门禁通道管理")
@RestController
@RequestMapping("/iot/access/channel")
@Validated
public class IotAccessChannelController {

    @Resource
    private IotAccessChannelService channelService;

    @GetMapping("/list-by-device")
    @Operation(summary = "获取设备通道列表")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-channel:query')")
    public CommonResult<List<IotAccessChannelRespVO>> getChannelsByDevice(@RequestParam("deviceId") Long deviceId) {
        List<IotDeviceChannelDO> channels = channelService.getChannelsByDeviceId(deviceId);
        return success(convertToVOList(channels));
    }

    @PostMapping("/discover")
    @Operation(summary = "发现通道")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-channel:discover')")
    public CommonResult<Boolean> discoverChannels(@RequestParam("deviceId") Long deviceId) {
        channelService.discoverChannels(deviceId);
        return success(true);
    }

    @PostMapping("/open-door")
    @Operation(summary = "远程开门")
    @PreAuthorize("@ss.hasPermission('iot:access-channel:control')")
    public CommonResult<Boolean> openDoor(@Valid @RequestBody IotAccessDoorControlReqVO reqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        String operatorName = SecurityFrameworkUtils.getLoginUserNickname();
        channelService.openDoor(reqVO.getChannelId(), operatorId, operatorName);
        return success(true);
    }

    @PostMapping("/close-door")
    @Operation(summary = "远程关门")
    @PreAuthorize("@ss.hasPermission('iot:access-channel:control')")
    public CommonResult<Boolean> closeDoor(@Valid @RequestBody IotAccessDoorControlReqVO reqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        String operatorName = SecurityFrameworkUtils.getLoginUserNickname();
        channelService.closeDoor(reqVO.getChannelId(), operatorId, operatorName);
        return success(true);
    }

    @PostMapping("/always-open")
    @Operation(summary = "设置常开")
    @PreAuthorize("@ss.hasPermission('iot:access-channel:control')")
    public CommonResult<Boolean> setAlwaysOpen(@Valid @RequestBody IotAccessDoorControlReqVO reqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        String operatorName = SecurityFrameworkUtils.getLoginUserNickname();
        channelService.setAlwaysOpen(reqVO.getChannelId(), operatorId, operatorName);
        return success(true);
    }

    @PostMapping("/always-closed")
    @Operation(summary = "设置常闭")
    @PreAuthorize("@ss.hasPermission('iot:access-channel:control')")
    public CommonResult<Boolean> setAlwaysClosed(@Valid @RequestBody IotAccessDoorControlReqVO reqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        String operatorName = SecurityFrameworkUtils.getLoginUserNickname();
        channelService.setAlwaysClosed(reqVO.getChannelId(), operatorId, operatorName);
        return success(true);
    }

    @PostMapping("/cancel-always")
    @Operation(summary = "取消常开/常闭")
    @PreAuthorize("@ss.hasPermission('iot:access-channel:control')")
    public CommonResult<Boolean> cancelAlwaysState(@Valid @RequestBody IotAccessDoorControlReqVO reqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        String operatorName = SecurityFrameworkUtils.getLoginUserNickname();
        channelService.cancelAlwaysState(reqVO.getChannelId(), operatorId, operatorName);
        return success(true);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取通道详细信息")
    @Parameter(name = "id", description = "通道ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-channel:query')")
    public CommonResult<cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessChannelDetailRespVO> getChannelDetail(@PathVariable("id") Long id) {
        return success(channelService.getChannelDetail(id));
    }

    // ========== 转换方法 ==========

    private IotAccessChannelRespVO convertToVO(IotDeviceChannelDO channel) {
        if (channel == null) {
            return null;
        }
        IotAccessChannelRespVO vo = new IotAccessChannelRespVO();
        vo.setId(channel.getId());
        vo.setDeviceId(channel.getDeviceId());
        vo.setChannelNo(channel.getChannelNo());
        vo.setChannelName(channel.getChannelName());
        vo.setOnlineStatus(channel.getOnlineStatus());
        vo.setCreateTime(channel.getCreateTime());
        // 从config中解析门状态等信息
        // TODO: 解析config JSON字段
        return vo;
    }

    private List<IotAccessChannelRespVO> convertToVOList(List<IotDeviceChannelDO> channels) {
        List<IotAccessChannelRespVO> result = new ArrayList<>();
        if (channels != null) {
            for (IotDeviceChannelDO channel : channels) {
                result.add(convertToVO(channel));
            }
        }
        return result;
    }

}
