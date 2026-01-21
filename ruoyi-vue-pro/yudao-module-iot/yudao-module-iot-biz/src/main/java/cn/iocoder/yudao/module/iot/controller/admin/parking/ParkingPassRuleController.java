package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.passrule.ParkingPassRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.passrule.ParkingPassRuleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.passrule.ParkingPassRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingPassRuleDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingPassRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 放行规则管理")
@RestController
@RequestMapping("/iot/parking/pass-rule")
@Validated
public class ParkingPassRuleController {

    @Resource
    private ParkingPassRuleService parkingPassRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建放行规则")
    @PreAuthorize("@ss.hasPermission('iot:parking:pass-rule:create')")
    public CommonResult<Long> createPassRule(@Valid @RequestBody ParkingPassRuleSaveReqVO createReqVO) {
        return success(parkingPassRuleService.createPassRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新放行规则")
    @PreAuthorize("@ss.hasPermission('iot:parking:pass-rule:update')")
    public CommonResult<Boolean> updatePassRule(@Valid @RequestBody ParkingPassRuleSaveReqVO updateReqVO) {
        parkingPassRuleService.updatePassRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除放行规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:pass-rule:delete')")
    public CommonResult<Boolean> deletePassRule(@RequestParam("id") Long id) {
        parkingPassRuleService.deletePassRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得放行规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:pass-rule:query')")
    public CommonResult<ParkingPassRuleRespVO> getPassRule(@RequestParam("id") Long id) {
        ParkingPassRuleDO passRule = parkingPassRuleService.getPassRule(id);
        return success(BeanUtils.toBean(passRule, ParkingPassRuleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得放行规则分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:pass-rule:query')")
    public CommonResult<PageResult<ParkingPassRuleRespVO>> getPassRulePage(@Valid ParkingPassRulePageReqVO pageReqVO) {
        PageResult<ParkingPassRuleDO> pageResult = parkingPassRuleService.getPassRulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingPassRuleRespVO.class));
    }
}
