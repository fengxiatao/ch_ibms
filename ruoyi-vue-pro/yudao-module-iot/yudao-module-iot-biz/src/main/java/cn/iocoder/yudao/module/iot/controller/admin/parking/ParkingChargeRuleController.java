package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargerule.ParkingChargeRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargerule.ParkingChargeRuleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargerule.ParkingChargeRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingChargeRuleDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingChargeRuleService;
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

@Tag(name = "管理后台 - 停车场收费规则管理")
@RestController
@RequestMapping("/iot/parking/charge-rule")
@Validated
public class ParkingChargeRuleController {

    @Resource
    private ParkingChargeRuleService parkingChargeRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建收费规则")
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule:create')")
    public CommonResult<Long> createChargeRule(@Valid @RequestBody ParkingChargeRuleSaveReqVO createReqVO) {
        return success(parkingChargeRuleService.createChargeRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新收费规则")
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule:update')")
    public CommonResult<Boolean> updateChargeRule(@Valid @RequestBody ParkingChargeRuleSaveReqVO updateReqVO) {
        parkingChargeRuleService.updateChargeRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除收费规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule:delete')")
    public CommonResult<Boolean> deleteChargeRule(@RequestParam("id") Long id) {
        parkingChargeRuleService.deleteChargeRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得收费规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule:query')")
    public CommonResult<ParkingChargeRuleRespVO> getChargeRule(@RequestParam("id") Long id) {
        ParkingChargeRuleDO chargeRule = parkingChargeRuleService.getChargeRule(id);
        return success(BeanUtils.toBean(chargeRule, ParkingChargeRuleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得收费规则分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule:query')")
    public CommonResult<PageResult<ParkingChargeRuleRespVO>> getChargeRulePage(@Valid ParkingChargeRulePageReqVO pageReqVO) {
        PageResult<ParkingChargeRuleDO> pageResult = parkingChargeRuleService.getChargeRulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingChargeRuleRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得收费规则精简列表")
    @PreAuthorize("@ss.hasPermission('iot:parking:charge-rule:query')")
    public CommonResult<List<ParkingChargeRuleRespVO>> getChargeRuleSimpleList() {
        List<ParkingChargeRuleDO> list = parkingChargeRuleService.getChargeRuleList();
        return success(BeanUtils.toBean(list, ParkingChargeRuleRespVO.class));
    }
}
