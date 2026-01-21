package cn.iocoder.yudao.module.iot.controller.admin.patrolplan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.scene.PatrolSceneSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolSceneChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolSceneDO;
import cn.iocoder.yudao.module.iot.service.patrolplan.PatrolSceneService;
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
 * 管理后台 - 轮巡场景 Controller
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 轮巡场景")
@RestController
@RequestMapping("/iot/patrol-scene")
@Validated
public class PatrolSceneController {

    @Resource
    private PatrolSceneService patrolSceneService;

    @PostMapping("/create")
    @Operation(summary = "创建轮巡场景")
    @PreAuthorize("@ss.hasPermission('iot:patrol-scene:create')")
    public CommonResult<Long> createPatrolScene(@Valid @RequestBody PatrolSceneSaveReqVO createReqVO) {
        return success(patrolSceneService.createPatrolScene(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新轮巡场景")
    @PreAuthorize("@ss.hasPermission('iot:patrol-scene:update')")
    public CommonResult<Boolean> updatePatrolScene(@Valid @RequestBody PatrolSceneSaveReqVO updateReqVO) {
        patrolSceneService.updatePatrolScene(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除轮巡场景")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-scene:delete')")
    public CommonResult<Boolean> deletePatrolScene(@RequestParam("id") Long id) {
        patrolSceneService.deletePatrolScene(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得轮巡场景")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-scene:query')")
    public CommonResult<IotVideoPatrolSceneDO> getPatrolScene(@RequestParam("id") Long id) {
        IotVideoPatrolSceneDO scene = patrolSceneService.getPatrolScene(id);
        return success(scene);
    }

    @GetMapping("/list")
    @Operation(summary = "获得场景列表")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-scene:query')")
    public CommonResult<List<IotVideoPatrolSceneDO>> getPatrolSceneList(@RequestParam("taskId") Long taskId) {
        List<IotVideoPatrolSceneDO> list = patrolSceneService.getPatrolSceneListByTaskId(taskId);
        return success(list);
    }

    @GetMapping("/channels")
    @Operation(summary = "获得场景通道列表")
    @Parameter(name = "sceneId", description = "场景ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-scene:query')")
    public CommonResult<List<IotVideoPatrolSceneChannelDO>> getSceneChannels(@RequestParam("sceneId") Long sceneId) {
        List<IotVideoPatrolSceneChannelDO> list = patrolSceneService.getSceneChannels(sceneId);
        return success(list);
    }

    @PutMapping("/update-order")
    @Operation(summary = "更新场景顺序")
    @PreAuthorize("@ss.hasPermission('iot:patrol-scene:update')")
    public CommonResult<Boolean> updateSceneOrder(@RequestBody List<Long> sceneIds) {
        patrolSceneService.updateSceneOrder(sceneIds);
        return success(true);
    }

    @GetMapping("/get-by-task")
    @Operation(summary = "根据任务ID获取场景（包含通道）")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-scene:query')")
    public CommonResult<PatrolSceneSaveReqVO> getPatrolSceneByTaskId(@RequestParam("taskId") Long taskId) {
        PatrolSceneSaveReqVO scene = patrolSceneService.getPatrolSceneWithChannelsByTaskId(taskId);
        return success(scene);
    }

    @PostMapping("/save-with-channels")
    @Operation(summary = "保存场景（包含通道）")
    @PreAuthorize("@ss.hasPermission('iot:patrol-scene:create')")
    public CommonResult<Long> savePatrolSceneWithChannels(@Valid @RequestBody PatrolSceneSaveReqVO saveReqVO) {
        return success(patrolSceneService.savePatrolSceneWithChannels(saveReqVO));
    }

}
