package cn.iocoder.yudao.module.iot.controller.admin.changhui;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade.*;
import cn.iocoder.yudao.module.iot.service.changhui.upgrade.ChanghuiUpgradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 长辉设备升级 Controller
 * 
 * <p>管理设备固件和升级任务，支持TCP帧传输和HTTP URL下载两种升级模式
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - 长辉设备升级")
@RestController
@RequestMapping("/iot/changhui/upgrade")
@Validated
public class ChanghuiUpgradeController {

    @Resource
    private ChanghuiUpgradeService upgradeService;

    // ==================== 固件管理 ====================

    @PostMapping("/firmware/upload")
    @Operation(summary = "上传固件")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:create')")
    public CommonResult<Long> uploadFirmware(@Valid ChanghuiFirmwareUploadReqVO reqVO,
                                              @RequestParam("file") MultipartFile file) {
        return success(upgradeService.uploadFirmware(reqVO, file));
    }

    @GetMapping("/firmware/list")
    @Operation(summary = "获取固件列表")
    @Parameter(name = "deviceType", description = "设备类型（可选）", example = "1")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:query')")
    public CommonResult<List<ChanghuiFirmwareRespVO>> getFirmwareList(
            @RequestParam(value = "deviceType", required = false) Integer deviceType) {
        return success(upgradeService.getFirmwareList(deviceType));
    }

    @GetMapping("/firmware/get")
    @Operation(summary = "获取固件详情")
    @Parameter(name = "id", description = "固件ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:query')")
    public CommonResult<ChanghuiFirmwareRespVO> getFirmware(@RequestParam("id") Long id) {
        return success(upgradeService.getFirmware(id));
    }

    @DeleteMapping("/firmware/delete")
    @Operation(summary = "删除固件")
    @Parameter(name = "id", description = "固件ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:delete')")
    public CommonResult<Boolean> deleteFirmware(@RequestParam("id") Long id) {
        upgradeService.deleteFirmware(id);
        return success(true);
    }

    // ==================== 升级任务管理 ====================

    @PostMapping("/task/create")
    @Operation(summary = "创建升级任务")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:create')")
    public CommonResult<Long> createUpgradeTask(@Valid @RequestBody ChanghuiUpgradeTaskCreateReqVO reqVO) {
        return success(upgradeService.createUpgradeTask(reqVO));
    }

    @PostMapping("/task/batch-create")
    @Operation(summary = "批量创建升级任务")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:create')")
    public CommonResult<ChanghuiBatchUpgradeResultVO> createBatchUpgradeTasks(
            @Valid @RequestBody ChanghuiBatchUpgradeCreateReqVO reqVO) {
        return success(upgradeService.createBatchUpgradeTasks(reqVO));
    }

    @PutMapping("/task/cancel")
    @Operation(summary = "取消升级任务")
    @Parameter(name = "id", description = "任务ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:update')")
    public CommonResult<Boolean> cancelUpgradeTask(@RequestParam("id") Long id) {
        upgradeService.cancelUpgradeTask(id);
        return success(true);
    }

    @GetMapping("/task/get")
    @Operation(summary = "获取升级任务详情")
    @Parameter(name = "id", description = "任务ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:query')")
    public CommonResult<ChanghuiUpgradeTaskRespVO> getUpgradeTask(@RequestParam("id") Long id) {
        return success(upgradeService.getUpgradeTask(id));
    }

    @GetMapping("/task/page")
    @Operation(summary = "获取升级任务分页")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:query')")
    public CommonResult<PageResult<ChanghuiUpgradeTaskRespVO>> getUpgradeTaskPage(
            @Valid ChanghuiUpgradeTaskPageReqVO reqVO) {
        return success(upgradeService.getUpgradeTaskPage(reqVO));
    }

    @GetMapping("/task/batch-progress")
    @Operation(summary = "获取批量升级进度")
    @Parameter(name = "taskIds", description = "任务ID列表", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:query')")
    public CommonResult<ChanghuiBatchUpgradeProgressVO> getBatchUpgradeProgress(
            @RequestParam("taskIds") List<Long> taskIds) {
        return success(upgradeService.getBatchUpgradeProgress(taskIds));
    }

    @PutMapping("/task/retry")
    @Operation(summary = "重试升级任务", description = "对失败或待执行的升级任务进行重试")
    @Parameter(name = "id", description = "任务ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:update')")
    public CommonResult<Boolean> retryUpgradeTask(@RequestParam("id") Long id) {
        upgradeService.retryUpgradeTask(id);
        return success(true);
    }

    @PostMapping("/task/cleanup-timeout")
    @Operation(summary = "清理超时任务", description = "将超时的待执行任务标记为失败（超过24小时未完成）")
    @PreAuthorize("@ss.hasPermission('iot:changhui-upgrade:update')")
    public CommonResult<Integer> cleanupTimeoutTasks() {
        int cleanedCount = upgradeService.cleanupTimeoutTasks();
        return success(cleanedCount);
    }

}
