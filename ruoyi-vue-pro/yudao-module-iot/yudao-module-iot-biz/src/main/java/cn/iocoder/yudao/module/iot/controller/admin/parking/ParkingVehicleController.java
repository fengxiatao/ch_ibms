package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.freevehicle.ParkingFreeVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.freevehicle.ParkingFreeVehicleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.freevehicle.ParkingFreeVehicleSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle.ParkingMonthlyVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle.ParkingMonthlyVehicleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle.ParkingMonthlyVehicleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingFreeVehicleDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingMonthlyVehicleDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingFreeVehicleService;
import cn.iocoder.yudao.module.iot.service.parking.ParkingMonthlyVehicleService;
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

@Tag(name = "管理后台 - 停车场车辆管理")
@RestController
@RequestMapping("/iot/parking/vehicle")
@Validated
public class ParkingVehicleController {

    @Resource
    private ParkingFreeVehicleService parkingFreeVehicleService;

    @Resource
    private ParkingMonthlyVehicleService parkingMonthlyVehicleService;

    // ========== 免费车管理 ==========

    @PostMapping("/free/create")
    @Operation(summary = "创建免费车")
    @PreAuthorize("@ss.hasPermission('iot:parking:free-vehicle:create')")
    public CommonResult<Long> createFreeVehicle(@Valid @RequestBody ParkingFreeVehicleSaveReqVO createReqVO) {
        return success(parkingFreeVehicleService.createFreeVehicle(createReqVO));
    }

    @PutMapping("/free/update")
    @Operation(summary = "更新免费车")
    @PreAuthorize("@ss.hasPermission('iot:parking:free-vehicle:update')")
    public CommonResult<Boolean> updateFreeVehicle(@Valid @RequestBody ParkingFreeVehicleSaveReqVO updateReqVO) {
        parkingFreeVehicleService.updateFreeVehicle(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/free/delete")
    @Operation(summary = "删除免费车")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:free-vehicle:delete')")
    public CommonResult<Boolean> deleteFreeVehicle(@RequestParam("id") Long id) {
        parkingFreeVehicleService.deleteFreeVehicle(id);
        return success(true);
    }

    @GetMapping("/free/get")
    @Operation(summary = "获得免费车")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:free-vehicle:query')")
    public CommonResult<ParkingFreeVehicleRespVO> getFreeVehicle(@RequestParam("id") Long id) {
        ParkingFreeVehicleDO freeVehicle = parkingFreeVehicleService.getFreeVehicle(id);
        return success(BeanUtils.toBean(freeVehicle, ParkingFreeVehicleRespVO.class));
    }

    @GetMapping("/free/page")
    @Operation(summary = "获得免费车分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:free-vehicle:query')")
    public CommonResult<PageResult<ParkingFreeVehicleRespVO>> getFreeVehiclePage(@Valid ParkingFreeVehiclePageReqVO pageReqVO) {
        PageResult<ParkingFreeVehicleDO> pageResult = parkingFreeVehicleService.getFreeVehiclePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingFreeVehicleRespVO.class));
    }

    // ========== 月租车管理 ==========

    @PostMapping("/monthly/create")
    @Operation(summary = "创建月租车")
    @PreAuthorize("@ss.hasPermission('iot:parking:monthly-vehicle:create')")
    public CommonResult<Long> createMonthlyVehicle(@Valid @RequestBody ParkingMonthlyVehicleSaveReqVO createReqVO) {
        return success(parkingMonthlyVehicleService.createMonthlyVehicle(createReqVO));
    }

    @PutMapping("/monthly/update")
    @Operation(summary = "更新月租车")
    @PreAuthorize("@ss.hasPermission('iot:parking:monthly-vehicle:update')")
    public CommonResult<Boolean> updateMonthlyVehicle(@Valid @RequestBody ParkingMonthlyVehicleSaveReqVO updateReqVO) {
        parkingMonthlyVehicleService.updateMonthlyVehicle(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/monthly/delete")
    @Operation(summary = "删除月租车")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:monthly-vehicle:delete')")
    public CommonResult<Boolean> deleteMonthlyVehicle(@RequestParam("id") Long id) {
        parkingMonthlyVehicleService.deleteMonthlyVehicle(id);
        return success(true);
    }

    @GetMapping("/monthly/get")
    @Operation(summary = "获得月租车")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:monthly-vehicle:query')")
    public CommonResult<ParkingMonthlyVehicleRespVO> getMonthlyVehicle(@RequestParam("id") Long id) {
        ParkingMonthlyVehicleDO monthlyVehicle = parkingMonthlyVehicleService.getMonthlyVehicle(id);
        return success(BeanUtils.toBean(monthlyVehicle, ParkingMonthlyVehicleRespVO.class));
    }

    @GetMapping("/monthly/page")
    @Operation(summary = "获得月租车分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:monthly-vehicle:query')")
    public CommonResult<PageResult<ParkingMonthlyVehicleRespVO>> getMonthlyVehiclePage(@Valid ParkingMonthlyVehiclePageReqVO pageReqVO) {
        PageResult<ParkingMonthlyVehicleDO> pageResult = parkingMonthlyVehicleService.getMonthlyVehiclePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingMonthlyVehicleRespVO.class));
    }

    @PostMapping("/monthly/recharge")
    @Operation(summary = "月卡续费")
    @Parameters({
            @Parameter(name = "id", description = "月租车ID", required = true),
            @Parameter(name = "months", description = "续费月数", required = true),
            @Parameter(name = "paidAmount", description = "实收金额", required = true),
            @Parameter(name = "paymentMethod", description = "支付方式", required = true)
    })
    @PreAuthorize("@ss.hasPermission('iot:parking:monthly-vehicle:recharge')")
    public CommonResult<Boolean> rechargeMonthlyVehicle(@RequestParam("id") Long id,
                                                        @RequestParam("months") Integer months,
                                                        @RequestParam("paidAmount") BigDecimal paidAmount,
                                                        @RequestParam("paymentMethod") String paymentMethod) {
        parkingMonthlyVehicleService.rechargeMonthlyVehicle(id, months, paidAmount, paymentMethod);
        return success(true);
    }
}
