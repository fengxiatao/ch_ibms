package cn.iocoder.yudao.module.iot.controller.admin.patrolplan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.record.PatrolRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolRecordDO;
import cn.iocoder.yudao.module.iot.service.patrolplan.PatrolRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 轮巡记录 Controller
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 轮巡记录")
@RestController
@RequestMapping("/iot/patrol-record")
@Validated
public class PatrolRecordController {

    @Resource
    private PatrolRecordService patrolRecordService;

    @GetMapping("/get")
    @Operation(summary = "获得轮巡记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-record:query')")
    public CommonResult<IotVideoPatrolRecordDO> getPatrolRecord(@RequestParam("id") Long id) {
        IotVideoPatrolRecordDO record = patrolRecordService.getPatrolRecord(id);
        return success(record);
    }

    @GetMapping("/page")
    @Operation(summary = "获得轮巡记录分页")
    @PreAuthorize("@ss.hasPermission('iot:patrol-record:query')")
    public CommonResult<PageResult<IotVideoPatrolRecordDO>> getPatrolRecordPage(@Valid PatrolRecordPageReqVO pageReqVO) {
        PageResult<IotVideoPatrolRecordDO> pageResult = patrolRecordService.getPatrolRecordPage(pageReqVO);
        return success(pageResult);
    }

    @PutMapping("/mark-handled")
    @Operation(summary = "标记为已处理")
    @PreAuthorize("@ss.hasPermission('iot:patrol-record:update')")
    public CommonResult<Boolean> markAsHandled(@RequestParam("id") Long id, 
                                                 @RequestParam(value = "remark", required = false) String remark) {
        patrolRecordService.markAsHandled(id, remark);
        return success(true);
    }

}
