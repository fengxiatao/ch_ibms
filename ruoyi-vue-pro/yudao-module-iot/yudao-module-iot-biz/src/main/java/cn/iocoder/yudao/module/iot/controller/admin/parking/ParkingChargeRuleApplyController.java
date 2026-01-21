package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargeruleapply.ParkingChargeRuleApplyPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargeruleapply.ParkingChargeRuleApplyRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargeruleapply.ParkingChargeRuleApplySaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingChargeRuleApplyDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingChargeRuleApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 收费规则应用管理")
@RestController
@RequestMapping("/iot/parking/charge-rule-apply")
@Validated
public class ParkingChargeRuleApplyController {

    @Resource
    private ParkingChargeRuleApplyService parkingChargeRuleApplyService;

    @PostMapping("/create")
    @Operation(summary = "创建收费规则应用")
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule-apply:create')")
    public CommonResult<Long> createChargeRuleApply(@Valid @RequestBody ParkingChargeRuleApplySaveReqVO createReqVO) {
        return success(parkingChargeRuleApplyService.createChargeRuleApply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新收费规则应用")
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule-apply:update')")
    public CommonResult<Boolean> updateChargeRuleApply(@Valid @RequestBody ParkingChargeRuleApplySaveReqVO updateReqVO) {
        parkingChargeRuleApplyService.updateChargeRuleApply(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除收费规则应用")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule-apply:delete')")
    public CommonResult<Boolean> deleteChargeRuleApply(@RequestParam("id") Long id) {
        parkingChargeRuleApplyService.deleteChargeRuleApply(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得收费规则应用")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule-apply:query')")
    public CommonResult<ParkingChargeRuleApplyRespVO> getChargeRuleApply(@RequestParam("id") Long id) {
        ParkingChargeRuleApplyDO chargeRuleApply = parkingChargeRuleApplyService.getChargeRuleApply(id);
        return success(BeanUtils.toBean(chargeRuleApply, ParkingChargeRuleApplyRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得收费规则应用分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule-apply:query')")
    public CommonResult<PageResult<ParkingChargeRuleApplyRespVO>> getChargeRuleApplyPage(@Valid ParkingChargeRuleApplyPageReqVO pageReqVO) {
        PageResult<ParkingChargeRuleApplyDO> pageResult = parkingChargeRuleApplyService.getChargeRuleApplyPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingChargeRuleApplyRespVO.class));
    }
}
