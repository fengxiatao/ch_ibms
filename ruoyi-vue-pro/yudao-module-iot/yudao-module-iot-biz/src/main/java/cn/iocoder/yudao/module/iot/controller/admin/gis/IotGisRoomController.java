package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.DeviceVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.RoomVO;
import cn.iocoder.yudao.module.iot.service.gis.IotGisRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT GIS 房间 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IoT GIS 房间")
@RestController
@RequestMapping("/iot/gis/room")
@Validated
public class IotGisRoomController {

    @Autowired
    private IotGisRoomService roomService;

    @GetMapping("/{roomId}/detail")
    @Operation(summary = "获取房间详情", description = "包含设备列表")
    @Parameter(name = "roomId", description = "房间ID", required = true, example = "1001")
    public CommonResult<RoomVO> getRoomDetail(@PathVariable("roomId") Long roomId) {
        RoomVO room = roomService.getRoomDetail(roomId);
        return success(room);
    }

    @GetMapping("/{roomId}/devices")
    @Operation(summary = "获取房间的设备列表")
    @Parameter(name = "roomId", description = "房间ID", required = true, example = "1001")
    public CommonResult<List<DeviceVO>> getRoomDevices(@PathVariable("roomId") Long roomId) {
        List<DeviceVO> devices = roomService.getRoomDevices(roomId);
        return success(devices);
    }

}

