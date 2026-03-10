package cn.iocoder.yudao.module.iot.controller.admin.visitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentApproveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAppointmentDO;
import cn.iocoder.yudao.module.iot.service.visitor.VisitorAppointmentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 新访客管理（预约/审批）")
@RestController
@RequestMapping("/iot/visitor/appointment")
@Validated
public class VisitorAppointmentController {

    @Resource
    private VisitorAppointmentService visitorAppointmentService;

    @Resource
    private ObjectMapper objectMapper;

    @PostMapping("/create")
    @Operation(summary = "创建预约")
    @PreAuthorize("@ss.hasPermission('security:visitor:create')")
    public CommonResult<Long> create(@Valid @RequestBody VisitorAppointmentSaveReqVO reqVO) {
        return success(visitorAppointmentService.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新预约")
    @PreAuthorize("@ss.hasPermission('security:visitor:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody VisitorAppointmentSaveReqVO reqVO) {
        visitorAppointmentService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除预约")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('security:visitor:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        visitorAppointmentService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得预约详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('security:visitor:query')")
    public CommonResult<VisitorAppointmentRespVO> get(@RequestParam("id") Long id) {
        VisitorAppointmentDO appointment = visitorAppointmentService.get(id);
        return success(toRespVO(appointment));
    }

    @GetMapping("/page")
    @Operation(summary = "获得预约分页")
    @PreAuthorize("@ss.hasPermission('security:visitor:query')")
    public CommonResult<PageResult<VisitorAppointmentRespVO>> page(@Valid VisitorAppointmentPageReqVO reqVO) {
        PageResult<VisitorAppointmentDO> pageResult = visitorAppointmentService.page(reqVO);
        PageResult<VisitorAppointmentRespVO> respPage = new PageResult<>();
        respPage.setTotal(pageResult.getTotal());
        respPage.setList(pageResult.getList().stream().map(this::toRespVO).toList());
        return success(respPage);
    }

    @PostMapping("/approve")
    @Operation(summary = "审批预约（通过/拒绝）")
    @PreAuthorize("@ss.hasPermission('security:visitor:approve')")
    public CommonResult<Boolean> approve(@Valid @RequestBody VisitorAppointmentApproveReqVO reqVO) {
        visitorAppointmentService.approve(reqVO);
        return success(true);
    }

    @PostMapping("/sign-out")
    @Operation(summary = "强制签离")
    @Parameter(name = "id", description = "预约编号", required = true)
    @PreAuthorize("@ss.hasPermission('security:visitor:sign-out')")
    public CommonResult<Boolean> signOut(@RequestParam("id") Long id) {
        visitorAppointmentService.signOut(id);
        return success(true);
    }

    private VisitorAppointmentRespVO toRespVO(VisitorAppointmentDO appointment) {
        VisitorAppointmentRespVO respVO = BeanUtils.toBean(appointment, VisitorAppointmentRespVO.class);
        respVO.setAreas(parseAreas(appointment != null ? appointment.getAreas() : null));
        return respVO;
    }

    private java.util.List<String> parseAreas(String json) {
        if (json == null || json.isBlank()) {
            return java.util.Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<java.util.List<String>>() {});
        } catch (Exception ignored) {
            return java.util.Collections.emptyList();
        }
    }
}

