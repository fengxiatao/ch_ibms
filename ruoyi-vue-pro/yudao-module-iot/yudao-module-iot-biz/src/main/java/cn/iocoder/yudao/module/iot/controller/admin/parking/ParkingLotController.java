package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lot.ParkingLotPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lot.ParkingLotRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lot.ParkingLotSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLotDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingLotService;
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

@Tag(name = "管理后台 - 停车场/车场管理")
@RestController
@RequestMapping("/iot/parking/lot")
@Validated
public class ParkingLotController {

    @Resource
    private ParkingLotService parkingLotService;

    @PostMapping("/create")
    @Operation(summary = "创建车场")
    @PreAuthorize("@ss.hasPermission('iot:parking:lot:create')")
    public CommonResult<Long> createParkingLot(@Valid @RequestBody ParkingLotSaveReqVO createReqVO) {
        return success(parkingLotService.createParkingLot(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新车场")
    @PreAuthorize("@ss.hasPermission('iot:parking:lot:update')")
    public CommonResult<Boolean> updateParkingLot(@Valid @RequestBody ParkingLotSaveReqVO updateReqVO) {
        parkingLotService.updateParkingLot(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除车场")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:lot:delete')")
    public CommonResult<Boolean> deleteParkingLot(@RequestParam("id") Long id) {
        parkingLotService.deleteParkingLot(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得车场")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:lot:query')")
    public CommonResult<ParkingLotRespVO> getParkingLot(@RequestParam("id") Long id) {
        ParkingLotDO parkingLot = parkingLotService.getParkingLot(id);
        return success(BeanUtils.toBean(parkingLot, ParkingLotRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得车场分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:lot:query')")
    public CommonResult<PageResult<ParkingLotRespVO>> getParkingLotPage(@Valid ParkingLotPageReqVO pageReqVO) {
        PageResult<ParkingLotDO> pageResult = parkingLotService.getParkingLotPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingLotRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得车场精简列表")
    @PreAuthorize("@ss.hasPermission('iot:parking:lot:query')")
    public CommonResult<List<ParkingLotRespVO>> getParkingLotSimpleList() {
        List<ParkingLotDO> list = parkingLotService.getParkingLotList();
        return success(BeanUtils.toBean(list, ParkingLotRespVO.class));
    }
}
