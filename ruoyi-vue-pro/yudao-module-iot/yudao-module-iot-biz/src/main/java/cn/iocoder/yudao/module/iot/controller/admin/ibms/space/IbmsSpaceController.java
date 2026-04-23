package cn.iocoder.yudao.module.iot.controller.admin.ibms.space;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsSpaceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsSpaceSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsSpaceTreeNodeRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsUnassignedItemRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsUnassignedPageReqVO;
import cn.iocoder.yudao.module.iot.service.ibms.space.IbmsSpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IBMS 空间管理
 */
@Tag(name = "管理后台 - IBMS 空间管理")
@RestController
@RequestMapping("/iot/ibms/space")
@Validated
public class IbmsSpaceController {

    @Resource
    private IbmsSpaceService ibmsSpaceService;

    @PostMapping("/create")
    @Operation(summary = "创建空间")
    @PreAuthorize("@ss.hasPermission('iot:ibms-space:create')")
    public CommonResult<Long> createSpace(@Valid @RequestBody IbmsSpaceSaveReqVO reqVO) {
        return success(ibmsSpaceService.createSpace(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新空间")
    @PreAuthorize("@ss.hasPermission('iot:ibms-space:update')")
    public CommonResult<Boolean> updateSpace(@Valid @RequestBody IbmsSpaceSaveReqVO reqVO) {
        ibmsSpaceService.updateSpace(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除空间")
    @PreAuthorize("@ss.hasPermission('iot:ibms-space:delete')")
    public CommonResult<Boolean> deleteSpace(@RequestParam("id") Long id) {
        ibmsSpaceService.deleteSpace(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取空间详情")
    @PreAuthorize("@ss.hasPermission('iot:ibms-space:query')")
    public CommonResult<IbmsSpaceRespVO> getSpace(@RequestParam("id") Long id) {
        return success(ibmsSpaceService.getSpace(id));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取空间树")
    @PreAuthorize("@ss.hasPermission('iot:ibms-space:query')")
    public CommonResult<List<IbmsSpaceTreeNodeRespVO>> getSpaceTree() {
        return success(ibmsSpaceService.getSpaceTree());
    }

    @GetMapping("/unassigned-page")
    @Operation(summary = "分页查询未分配设备/通道（空间页专用）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-space:query')")
    public CommonResult<PageResult<IbmsUnassignedItemRespVO>> getUnassignedPage(@Valid IbmsUnassignedPageReqVO reqVO) {
        return success(ibmsSpaceService.getUnassignedPage(reqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出空间 Excel（预留）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-space:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExcel() {
        throw new UnsupportedOperationException("IBMS 空间导出暂未实现");
    }
}

