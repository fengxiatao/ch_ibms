package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.AccessControllerDetailVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.AccessControllerTreeVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.AuthRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.DoorControlReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.DoorControlRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.PersonDeviceAuthVO;
import cn.iocoder.yudao.module.iot.service.access.AccessManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁管理 Controller
 * 
 * 提供门禁控制器+门通道的组合视图和门控操作接口
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 门禁管理")
@RestController
@RequestMapping("/iot/access/management")
@Validated
@Slf4j
public class AccessManagementController {

    @Resource
    private AccessManagementService accessManagementService;

    @GetMapping("/tree")
    @Operation(summary = "获取门禁控制器树形结构")
    @PreAuthorize("@ss.hasPermission('iot:access-management:query')")
    public CommonResult<List<AccessControllerTreeVO>> getControllerTree() {
        List<AccessControllerTreeVO> tree = accessManagementService.getControllerTree();
        return success(tree);
    }

    @GetMapping("/detail/{deviceId}")
    @Operation(summary = "获取门禁控制器详情")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-management:query')")
    public CommonResult<AccessControllerDetailVO> getControllerDetail(@PathVariable("deviceId") Long deviceId) {
        AccessControllerDetailVO detail = accessManagementService.getControllerDetail(deviceId);
        return success(detail);
    }

    @PostMapping("/door-control")
    @Operation(summary = "门控操作")
    @PreAuthorize("@ss.hasPermission('iot:access-management:control')")
    public CommonResult<DoorControlRespVO> doorControl(@Valid @RequestBody DoorControlReqVO reqVO) {
        // 委托给Service层处理，Service层实现了：
        // 1. 检查设备在线状态
        // 2. 使用连接池句柄执行操作
        // 3. 无句柄时尝试即时登录（Requirements 4.1, 4.4）
        DoorControlRespVO result = accessManagementService.doorControl(reqVO);
        return success(result);
    }

    @PostMapping("/refresh/{deviceId}")
    @Operation(summary = "刷新控制器状态")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-management:control')")
    public CommonResult<Boolean> refreshControllerStatus(@PathVariable("deviceId") Long deviceId) {
        accessManagementService.refreshControllerStatus(deviceId);
        return success(true);
    }

    @GetMapping("/online-tree")
    @Operation(summary = "获取在线门禁控制器树形结构")
    @PreAuthorize("@ss.hasPermission('iot:access-management:query')")
    public CommonResult<List<AccessControllerTreeVO>> getOnlineControllerTree() {
        List<AccessControllerTreeVO> tree = accessManagementService.getOnlineControllerTree();
        return success(tree);
    }

    // ========== 授权记录查询接口 (Requirements: 7.1, 7.2) ==========

    @GetMapping("/auth-record/page")
    @Operation(summary = "获取授权记录分页")
    @PreAuthorize("@ss.hasPermission('iot:access-management:query')")
    public CommonResult<PageResult<PersonDeviceAuthVO>> getAuthRecordPage(@Valid AuthRecordPageReqVO reqVO) {
        PageResult<PersonDeviceAuthVO> pageResult = accessManagementService.getAuthRecordPage(reqVO);
        return success(pageResult);
    }

    @PostMapping("/auth-record/retry/{authId}")
    @Operation(summary = "重试失败的授权")
    @Parameter(name = "authId", description = "授权记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-management:control')")
    public CommonResult<Boolean> retryFailedAuth(@PathVariable("authId") Long authId) {
        accessManagementService.retryFailedAuth(authId);
        return success(true);
    }

}
