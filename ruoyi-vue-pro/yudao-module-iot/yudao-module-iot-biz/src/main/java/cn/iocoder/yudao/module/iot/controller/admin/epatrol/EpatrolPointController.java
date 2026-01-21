package cn.iocoder.yudao.module.iot.controller.admin.epatrol;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPointDO;
import cn.iocoder.yudao.module.iot.service.epatrol.EpatrolPointService;
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

/**
 * 管理后台 - 电子巡更点
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 电子巡更点")
@RestController
@RequestMapping("/iot/epatrol/point")
@Validated
public class EpatrolPointController {

    @Resource
    private EpatrolPointService pointService;

    @PostMapping("/create")
    @Operation(summary = "创建巡更点")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-point:create')")
    public CommonResult<Long> createPoint(@Valid @RequestBody EpatrolPointSaveReqVO createReqVO) {
        return success(pointService.createPoint(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新巡更点")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-point:update')")
    public CommonResult<Boolean> updatePoint(@Valid @RequestBody EpatrolPointSaveReqVO updateReqVO) {
        pointService.updatePoint(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除巡更点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:epatrol-point:delete')")
    public CommonResult<Boolean> deletePoint(@RequestParam("id") Long id) {
        pointService.deletePoint(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得巡更点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:epatrol-point:query')")
    public CommonResult<EpatrolPointRespVO> getPoint(@RequestParam("id") Long id) {
        EpatrolPointDO point = pointService.getPoint(id);
        return success(BeanUtils.toBean(point, EpatrolPointRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得巡更点分页")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-point:query')")
    public CommonResult<PageResult<EpatrolPointRespVO>> getPointPage(@Valid EpatrolPointPageReqVO pageReqVO) {
        PageResult<EpatrolPointDO> pageResult = pointService.getPointPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EpatrolPointRespVO.class));
    }

    @GetMapping("/list-all-enabled")
    @Operation(summary = "获得所有启用的巡更点")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-point:query')")
    public CommonResult<List<EpatrolPointRespVO>> getEnabledPointList() {
        List<EpatrolPointDO> list = pointService.getEnabledPointList();
        return success(BeanUtils.toBean(list, EpatrolPointRespVO.class));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新巡更点状态")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-point:update')")
    public CommonResult<Boolean> updatePointStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        pointService.updatePointStatus(id, status);
        return success(true);
    }

}
