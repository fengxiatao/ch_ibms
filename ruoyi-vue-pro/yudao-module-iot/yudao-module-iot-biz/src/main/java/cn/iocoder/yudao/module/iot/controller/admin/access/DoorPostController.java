package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorpost.*;
import cn.iocoder.yudao.module.iot.service.access.DoorPostService;
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
 * 门岗 Controller
 *
 * @author 智能化系统
 */
@Tag(name = "管理后台 - 门岗")
@RestController
@RequestMapping("/iot/door-post")
@Validated
public class DoorPostController {

    @Resource
    private DoorPostService doorPostService;

    @PostMapping("/create")
    @Operation(summary = "创建门岗")
    @PreAuthorize("@ss.hasPermission('iot:door-post:create')")
    public CommonResult<Long> createDoorPost(@Valid @RequestBody DoorPostCreateReqVO createReqVO) {
        return success(doorPostService.createDoorPost(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新门岗")
    @PreAuthorize("@ss.hasPermission('iot:door-post:update')")
    public CommonResult<Boolean> updateDoorPost(@Valid @RequestBody DoorPostUpdateReqVO updateReqVO) {
        doorPostService.updateDoorPost(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门岗")
    @Parameter(name = "id", description = "门岗ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:door-post:delete')")
    public CommonResult<Boolean> deleteDoorPost(@RequestParam("id") Long id) {
        doorPostService.deleteDoorPost(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门岗")
    @Parameter(name = "id", description = "门岗ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:door-post:query')")
    public CommonResult<DoorPostRespVO> getDoorPost(@RequestParam("id") Long id) {
        DoorPostRespVO doorPost = doorPostService.getDoorPost(id);
        return success(doorPost);
    }

    @GetMapping("/page")
    @Operation(summary = "获得门岗分页")
    @PreAuthorize("@ss.hasPermission('iot:door-post:query')")
    public CommonResult<PageResult<DoorPostRespVO>> getDoorPostPage(@Valid DoorPostPageReqVO pageVO) {
        PageResult<DoorPostRespVO> pageResult = doorPostService.getDoorPostPage(pageVO);
        return success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获得门岗列表")
    @PreAuthorize("@ss.hasPermission('iot:door-post:query')")
    public CommonResult<List<DoorPostRespVO>> getDoorPostList() {
        List<DoorPostRespVO> list = doorPostService.getDoorPostList();
        return success(list);
    }

}


























