package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate.ParkingGatePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate.ParkingGateRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate.ParkingGateSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingGateDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingGateService;
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

@Tag(name = "管理后台 - 道闸设备管理")
@RestController
@RequestMapping("/iot/parking/gate")
@Validated
public class ParkingGateController {

    @Resource
    private ParkingGateService parkingGateService;

    @PostMapping("/create")
    @Operation(summary = "创建道闸设备")
    @PreAuthorize("@ss.hasPermission('iot:parking:gate:create')")
    public CommonResult<Long> createParkingGate(@Valid @RequestBody ParkingGateSaveReqVO createReqVO) {
        return success(parkingGateService.createParkingGate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新道闸设备")
    @PreAuthorize("@ss.hasPermission('iot:parking:gate:update')")
    public CommonResult<Boolean> updateParkingGate(@Valid @RequestBody ParkingGateSaveReqVO updateReqVO) {
        parkingGateService.updateParkingGate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除道闸设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:gate:delete')")
    public CommonResult<Boolean> deleteParkingGate(@RequestParam("id") Long id) {
        parkingGateService.deleteParkingGate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得道闸设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:gate:query')")
    public CommonResult<ParkingGateRespVO> getParkingGate(@RequestParam("id") Long id) {
        ParkingGateDO parkingGate = parkingGateService.getParkingGate(id);
        return success(BeanUtils.toBean(parkingGate, ParkingGateRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得道闸设备分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:gate:query')")
    public CommonResult<PageResult<ParkingGateRespVO>> getParkingGatePage(@Valid ParkingGatePageReqVO pageReqVO) {
        PageResult<ParkingGateDO> pageResult = parkingGateService.getParkingGatePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingGateRespVO.class));
    }

    @GetMapping("/list-by-lot")
    @Operation(summary = "获得车场的道闸设备列表")
    @Parameter(name = "lotId", description = "车场ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:gate:query')")
    public CommonResult<List<ParkingGateRespVO>> getParkingGateListByLotId(@RequestParam("lotId") Long lotId) {
        List<ParkingGateDO> list = parkingGateService.getParkingGateListByLotId(lotId);
        return success(BeanUtils.toBean(list, ParkingGateRespVO.class));
    }
}
