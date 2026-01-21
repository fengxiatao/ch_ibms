package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane.ParkingLanePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane.ParkingLaneRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane.ParkingLaneSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLaneDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingLaneService;
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

@Tag(name = "管理后台 - 车道管理")
@RestController
@RequestMapping("/iot/parking/lane")
@Validated
public class ParkingLaneController {

    @Resource
    private ParkingLaneService parkingLaneService;

    @PostMapping("/create")
    @Operation(summary = "创建车道")
    @PreAuthorize("@ss.hasPermission('iot:parking:lane:create')")
    public CommonResult<Long> createParkingLane(@Valid @RequestBody ParkingLaneSaveReqVO createReqVO) {
        return success(parkingLaneService.createParkingLane(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新车道")
    @PreAuthorize("@ss.hasPermission('iot:parking:lane:update')")
    public CommonResult<Boolean> updateParkingLane(@Valid @RequestBody ParkingLaneSaveReqVO updateReqVO) {
        parkingLaneService.updateParkingLane(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除车道")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:lane:delete')")
    public CommonResult<Boolean> deleteParkingLane(@RequestParam("id") Long id) {
        parkingLaneService.deleteParkingLane(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得车道")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:lane:query')")
    public CommonResult<ParkingLaneRespVO> getParkingLane(@RequestParam("id") Long id) {
        ParkingLaneDO parkingLane = parkingLaneService.getParkingLane(id);
        return success(BeanUtils.toBean(parkingLane, ParkingLaneRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得车道分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:lane:query')")
    public CommonResult<PageResult<ParkingLaneRespVO>> getParkingLanePage(@Valid ParkingLanePageReqVO pageReqVO) {
        PageResult<ParkingLaneDO> pageResult = parkingLaneService.getParkingLanePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingLaneRespVO.class));
    }

    @GetMapping("/list-by-lot")
    @Operation(summary = "获得车场的车道列表")
    @Parameter(name = "lotId", description = "车场ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:lane:query')")
    public CommonResult<List<ParkingLaneRespVO>> getParkingLaneListByLotId(@RequestParam("lotId") Long lotId) {
        List<ParkingLaneDO> list = parkingLaneService.getParkingLaneListByLotId(lotId);
        return success(BeanUtils.toBean(list, ParkingLaneRespVO.class));
    }
}
