package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.presentvehicle.ParkingPresentVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.presentvehicle.ParkingPresentVehicleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.record.ParkingRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.record.ParkingRecordRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingPresentVehicleDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRecordDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 停车记录管理")
@RestController
@RequestMapping("/iot/parking/record")
@Validated
public class ParkingRecordController {

    @Resource
    private ParkingRecordService parkingRecordService;

    // ========== 在场车辆 ==========

    @GetMapping("/present/page")
    @Operation(summary = "获得在场车辆分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:present-vehicle:query')")
    public CommonResult<PageResult<ParkingPresentVehicleRespVO>> getPresentVehiclePage(@Valid ParkingPresentVehiclePageReqVO pageReqVO) {
        PageResult<ParkingPresentVehicleDO> pageResult = parkingRecordService.getPresentVehiclePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingPresentVehicleRespVO.class));
    }

    @GetMapping("/present/get")
    @Operation(summary = "获得在场车辆")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:present-vehicle:query')")
    public CommonResult<ParkingPresentVehicleRespVO> getPresentVehicle(@RequestParam("id") Long id) {
        ParkingPresentVehicleDO presentVehicle = parkingRecordService.getPresentVehicle(id);
        return success(BeanUtils.toBean(presentVehicle, ParkingPresentVehicleRespVO.class));
    }

    @PostMapping("/present/force-exit")
    @Operation(summary = "强制出场")
    @Parameters({
            @Parameter(name = "id", description = "在场车辆ID", required = true),
            @Parameter(name = "remark", description = "备注")
    })
    @PreAuthorize("@ss.hasPermission('iot:parking:present-vehicle:force-exit')")
    public CommonResult<Boolean> forceExit(@RequestParam("id") Long id,
                                           @RequestParam(value = "remark", required = false) String remark) {
        parkingRecordService.forceExit(id, remark);
        return success(true);
    }

    // ========== 进出记录 ==========

    @GetMapping("/page")
    @Operation(summary = "获得进出记录分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:record:query')")
    public CommonResult<PageResult<ParkingRecordRespVO>> getRecordPage(@Valid ParkingRecordPageReqVO pageReqVO) {
        PageResult<ParkingRecordDO> pageResult = parkingRecordService.getRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingRecordRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得进出记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:record:query')")
    public CommonResult<ParkingRecordRespVO> getRecord(@RequestParam("id") Long id) {
        ParkingRecordDO record = parkingRecordService.getRecord(id);
        return success(BeanUtils.toBean(record, ParkingRecordRespVO.class));
    }

    // ========== 停车费用 ==========

    @GetMapping("/calculate-fee")
    @Operation(summary = "计算停车费用")
    @Parameters({
            @Parameter(name = "plateNumber", description = "车牌号", required = true),
            @Parameter(name = "lotId", description = "车场ID", required = true)
    })
    @PreAuthorize("@ss.hasPermission('iot:parking:record:query')")
    public CommonResult<BigDecimal> calculateParkingFee(@RequestParam("plateNumber") String plateNumber,
                                                        @RequestParam("lotId") Long lotId) {
        return success(parkingRecordService.calculateParkingFee(plateNumber, lotId));
    }

    // ========== 车辆入场出场 ==========

    @PostMapping("/entry")
    @Operation(summary = "车辆入场")
    @Parameters({
            @Parameter(name = "plateNumber", description = "车牌号", required = true),
            @Parameter(name = "lotId", description = "车场ID", required = true),
            @Parameter(name = "laneId", description = "车道ID"),
            @Parameter(name = "gateId", description = "道闸ID"),
            @Parameter(name = "photoUrl", description = "照片URL")
    })
    @PreAuthorize("@ss.hasPermission('iot:parking:record:entry')")
    public CommonResult<Long> vehicleEntry(@RequestParam("plateNumber") String plateNumber,
                                           @RequestParam("lotId") Long lotId,
                                           @RequestParam(value = "laneId", required = false) Long laneId,
                                           @RequestParam(value = "gateId", required = false) Long gateId,
                                           @RequestParam(value = "photoUrl", required = false) String photoUrl) {
        return success(parkingRecordService.vehicleEntry(plateNumber, lotId, laneId, gateId, photoUrl));
    }

    @PostMapping("/exit")
    @Operation(summary = "车辆出场")
    @Parameters({
            @Parameter(name = "plateNumber", description = "车牌号", required = true),
            @Parameter(name = "lotId", description = "车场ID", required = true),
            @Parameter(name = "laneId", description = "车道ID"),
            @Parameter(name = "gateId", description = "道闸ID"),
            @Parameter(name = "photoUrl", description = "照片URL"),
            @Parameter(name = "paidAmount", description = "实收金额"),
            @Parameter(name = "paymentMethod", description = "支付方式")
    })
    @PreAuthorize("@ss.hasPermission('iot:parking:record:exit')")
    public CommonResult<Boolean> vehicleExit(@RequestParam("plateNumber") String plateNumber,
                                             @RequestParam("lotId") Long lotId,
                                             @RequestParam(value = "laneId", required = false) Long laneId,
                                             @RequestParam(value = "gateId", required = false) Long gateId,
                                             @RequestParam(value = "photoUrl", required = false) String photoUrl,
                                             @RequestParam(value = "paidAmount", required = false) BigDecimal paidAmount,
                                             @RequestParam(value = "paymentMethod", required = false) String paymentMethod) {
        parkingRecordService.vehicleExit(plateNumber, lotId, laneId, gateId, photoUrl, paidAmount, paymentMethod);
        return success(true);
    }
}
