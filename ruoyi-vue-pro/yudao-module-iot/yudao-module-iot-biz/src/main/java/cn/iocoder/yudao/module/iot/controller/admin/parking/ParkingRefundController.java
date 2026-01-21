package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.refund.ParkingRefundRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.refund.ParkingRefundRecordRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.refund.ParkingRefundReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRefundRecordDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 停车退款")
@RestController
@RequestMapping("/iot/parking/refund")
@Validated
public class ParkingRefundController {

    @Resource
    private ParkingRefundService parkingRefundService;

    @PostMapping("/apply")
    @Operation(summary = "申请退款")
    @PreAuthorize("@ss.hasPermission('iot:parking:refund:apply')")
    public CommonResult<Long> applyRefund(@Valid @RequestBody ParkingRefundReqVO reqVO) {
        Long refundId = parkingRefundService.applyRefund(reqVO, getLoginUserNickname());
        return success(refundId);
    }

    @PostMapping("/execute")
    @Operation(summary = "执行退款")
    @Parameter(name = "id", description = "退款记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:refund:execute')")
    public CommonResult<Boolean> executeRefund(@RequestParam("id") Long id) {
        parkingRefundService.executeRefund(id, getLoginUserNickname());
        return success(true);
    }

    @PostMapping("/close")
    @Operation(summary = "关闭退款")
    @PreAuthorize("@ss.hasPermission('iot:parking:refund:close')")
    public CommonResult<Boolean> closeRefund(@RequestParam("id") Long id,
                                             @RequestParam("reason") String reason) {
        parkingRefundService.closeRefund(id, reason);
        return success(true);
    }

    @PostMapping("/sync")
    @Operation(summary = "同步退款状态")
    @Parameter(name = "id", description = "退款记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:refund:query')")
    public CommonResult<Boolean> syncRefundStatus(@RequestParam("id") Long id) {
        parkingRefundService.syncRefundStatus(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获取退款记录分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:refund:query')")
    public CommonResult<PageResult<ParkingRefundRecordRespVO>> getRefundRecordPage(@Valid ParkingRefundRecordPageReqVO pageReqVO) {
        PageResult<ParkingRefundRecordDO> pageResult = parkingRefundService.getRefundRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingRefundRecordRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取退款记录详情")
    @Parameter(name = "id", description = "退款记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:refund:query')")
    public CommonResult<ParkingRefundRecordRespVO> getRefundRecord(@RequestParam("id") Long id) {
        ParkingRefundRecordDO refundRecord = parkingRefundService.getRefundRecord(id);
        return success(BeanUtils.toBean(refundRecord, ParkingRefundRecordRespVO.class));
    }

    @PostMapping("/notify")
    @Operation(summary = "微信退款回调通知")
    public String refundNotify(@RequestBody String xmlData) {
        return parkingRefundService.handleRefundNotify(xmlData);
    }
}
