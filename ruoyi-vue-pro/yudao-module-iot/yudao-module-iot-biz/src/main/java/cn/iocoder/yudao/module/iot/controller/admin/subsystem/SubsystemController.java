package cn.iocoder.yudao.module.iot.controller.admin.subsystem;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.subsystem.vo.SubsystemRespVO;
import cn.iocoder.yudao.module.iot.service.subsystem.SubsystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT子系统管理控制器
 *
 * @author IBMS Team
 */
@Tag(name = "管理后台 - IoT子系统管理")
@RestController
@RequestMapping("/iot/subsystem")
@Slf4j
public class SubsystemController {

    @Resource
    private SubsystemService subsystemService;

    /**
     * 获取所有子系统列表（扁平）
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有子系统列表")
    @PreAuthorize("@ss.hasPermission('iot:product:query') or @ss.hasPermission('iot:device:query')")
    public CommonResult<List<SubsystemRespVO>> getSubsystemList() {
        log.info("【子系统管理】获取子系统列表");
        List<SubsystemRespVO> list = subsystemService.getSubsystemList();
        return success(list);
    }

    /**
     * 获取子系统树（两级）
     */
    @GetMapping("/tree")
    @Operation(summary = "获取子系统树形结构")
    @PreAuthorize("@ss.hasPermission('iot:product:query') or @ss.hasPermission('iot:device:query')")
    public CommonResult<List<SubsystemRespVO>> getSubsystemTree() {
        log.info("【子系统管理】获取子系统树");
        List<SubsystemRespVO> tree = subsystemService.getSubsystemTree();
        return success(tree);
    }

    /**
     * 根据代码获取子系统
     */
    @GetMapping("/get-by-code")
    @Operation(summary = "根据代码获取子系统")
    @PreAuthorize("@ss.hasPermission('iot:product:query') or @ss.hasPermission('iot:device:query')")
    public CommonResult<SubsystemRespVO> getSubsystemByCode(
            @Parameter(description = "子系统代码", required = true) @RequestParam("code") String code
    ) {
        log.info("【子系统管理】根据代码获取子系统: {}", code);
        SubsystemRespVO subsystem = subsystemService.getSubsystemByCode(code);
        return success(subsystem);
    }

    /**
     * 获取指定父系统的子系统列表
     */
    @GetMapping("/list-by-parent")
    @Operation(summary = "获取指定父系统的子系统列表")
    @PreAuthorize("@ss.hasPermission('iot:product:query') or @ss.hasPermission('iot:device:query')")
    public CommonResult<List<SubsystemRespVO>> getSubsystemListByParent(
            @Parameter(description = "父系统代码", required = true) @RequestParam("parentCode") String parentCode
    ) {
        log.info("【子系统管理】获取父系统的子系统列表: {}", parentCode);
        List<SubsystemRespVO> list = subsystemService.getSubsystemListByParent(parentCode);
        return success(list);
    }

    /**
     * 获取子系统统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取子系统统计信息（含产品数、设备数）")
    @PreAuthorize("@ss.hasPermission('iot:product:query') or @ss.hasPermission('iot:device:query')")
    public CommonResult<List<SubsystemRespVO>> getSubsystemStatistics() {
        log.info("【子系统管理】获取子系统统计信息");
        List<SubsystemRespVO> statistics = subsystemService.getSubsystemStatistics();
        return success(statistics);
    }
}


















































