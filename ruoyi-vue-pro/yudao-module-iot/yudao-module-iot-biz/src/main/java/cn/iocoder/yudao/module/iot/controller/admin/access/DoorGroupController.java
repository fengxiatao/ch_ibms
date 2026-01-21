package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorgroup.*;
import cn.iocoder.yudao.module.iot.service.access.DoorGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门组 Controller
 *
 * @author 智能化系统
 */
@Tag(name = "管理后台 - 门组")
@RestController
@RequestMapping("/iot/door-group")
@Validated
public class DoorGroupController {

    @Resource
    private DoorGroupService doorGroupService;

    @PostMapping("/create")
    @Operation(summary = "创建门组")
    @PreAuthorize("@ss.hasPermission('iot:door-group:create')")
    public CommonResult<Long> createDoorGroup(@Valid @RequestBody DoorGroupCreateReqVO createReqVO) {
        return success(doorGroupService.createDoorGroup(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新门组")
    @PreAuthorize("@ss.hasPermission('iot:door-group:update')")
    public CommonResult<Boolean> updateDoorGroup(@Valid @RequestBody DoorGroupUpdateReqVO updateReqVO) {
        doorGroupService.updateDoorGroup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门组")
    @Parameter(name = "id", description = "门组ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:door-group:delete')")
    public CommonResult<Boolean> deleteDoorGroup(@RequestParam("id") Long id) {
        doorGroupService.deleteDoorGroup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门组")
    @Parameter(name = "id", description = "门组ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:door-group:query')")
    public CommonResult<DoorGroupRespVO> getDoorGroup(@RequestParam("id") Long id) {
        DoorGroupRespVO doorGroup = doorGroupService.getDoorGroup(id);
        return success(doorGroup);
    }

    @GetMapping("/page")
    @Operation(summary = "获得门组分页")
    @PreAuthorize("@ss.hasPermission('iot:door-group:query')")
    public CommonResult<PageResult<DoorGroupRespVO>> getDoorGroupPage(@Valid DoorGroupPageReqVO pageVO) {
        PageResult<DoorGroupRespVO> pageResult = doorGroupService.getDoorGroupPage(pageVO);
        return success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获得门组列表")
    @PreAuthorize("@ss.hasPermission('iot:door-group:query')")
    public CommonResult<List<DoorGroupRespVO>> getDoorGroupList() {
        List<DoorGroupRespVO> list = doorGroupService.getDoorGroupList();
        return success(list);
    }

}


























