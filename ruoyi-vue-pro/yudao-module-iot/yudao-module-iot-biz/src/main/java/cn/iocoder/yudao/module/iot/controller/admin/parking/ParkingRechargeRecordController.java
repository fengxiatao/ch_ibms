package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.rechargerecord.ParkingRechargeRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.rechargerecord.ParkingRechargeRecordRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.rechargerecord.ParkingRechargeStatisticsVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRechargeRecordDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingRechargeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 月卡充值记录")
@RestController
@RequestMapping("/iot/parking/recharge-record")
@Validated
public class ParkingRechargeRecordController {

    @Resource
    private ParkingRechargeRecordService parkingRechargeRecordService;

    @GetMapping("/page")
    @Operation(summary = "获得月卡充值记录分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:recharge-record:query')")
    public CommonResult<PageResult<ParkingRechargeRecordRespVO>> getRechargeRecordPage(@Valid ParkingRechargeRecordPageReqVO pageReqVO) {
        PageResult<ParkingRechargeRecordDO> pageResult = parkingRechargeRecordService.getRechargeRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingRechargeRecordRespVO.class));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获得充值统计")
    @PreAuthorize("@ss.hasPermission('iot:parking:recharge-record:query')")
    public CommonResult<ParkingRechargeStatisticsVO> getRechargeStatistics() {
        return success(parkingRechargeRecordService.getRechargeStatistics());
    }
}
