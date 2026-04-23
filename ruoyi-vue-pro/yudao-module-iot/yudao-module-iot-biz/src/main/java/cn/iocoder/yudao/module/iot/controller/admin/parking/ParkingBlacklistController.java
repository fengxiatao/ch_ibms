package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.blacklist.ParkingBlacklistPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.blacklist.ParkingBlacklistRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.blacklist.ParkingBlacklistSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingBlacklistDO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 停车场黑名单")
@RestController
@RequestMapping("/iot/parking/blacklist")
@Validated
public class ParkingBlacklistController {

    @Resource
    private ParkingBlacklistService parkingBlacklistService;

    @PostMapping("/create")
    @Operation(summary = "创建黑名单")
    @PreAuthorize("@ss.hasPermission('iot:parking:blacklist:create')")
    public CommonResult<Long> createBlacklist(@Valid @RequestBody ParkingBlacklistSaveReqVO createReqVO) {
        return success(parkingBlacklistService.createBlacklist(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新黑名单")
    @PreAuthorize("@ss.hasPermission('iot:parking:blacklist:update')")
    public CommonResult<Boolean> updateBlacklist(@Valid @RequestBody ParkingBlacklistSaveReqVO updateReqVO) {
        parkingBlacklistService.updateBlacklist(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除黑名单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:blacklist:delete')")
    public CommonResult<Boolean> deleteBlacklist(@RequestParam("id") Long id) {
        parkingBlacklistService.deleteBlacklist(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得黑名单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:blacklist:query-btn')")
    public CommonResult<ParkingBlacklistRespVO> getBlacklist(@RequestParam("id") Long id) {
        ParkingBlacklistDO blacklist = parkingBlacklistService.getBlacklist(id);
        return success(BeanUtils.toBean(blacklist, ParkingBlacklistRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得黑名单分页")
    @PreAuthorize("@ss.hasPermission('iot:parking:blacklist:query-btn')")
    public CommonResult<PageResult<ParkingBlacklistRespVO>> getBlacklistPage(@Valid ParkingBlacklistPageReqVO pageReqVO) {
        PageResult<ParkingBlacklistDO> pageResult = parkingBlacklistService.getBlacklistPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ParkingBlacklistRespVO.class));
    }

    @PostMapping("/release")
    @Operation(summary = "解除黑名单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:parking:blacklist:release')")
    public CommonResult<Boolean> releaseBlacklist(@RequestParam("id") Long id) {
        parkingBlacklistService.releaseBlacklist(id);
        return success(true);
    }
}
