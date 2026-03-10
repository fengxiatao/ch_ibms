package cn.iocoder.yudao.module.iot.controller.admin.visitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.abnormal.VisitorAbnormalEventPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.abnormal.VisitorAbnormalEventRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.overview.VisitorDashboardRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.overview.VisitorOverviewStatsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAbnormalEventDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAppointmentDO;
import cn.iocoder.yudao.module.iot.service.visitor.VisitorOverviewService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 新访客管理（概览/异常）")
@RestController
@RequestMapping("/iot/visitor/overview")
@Validated
public class VisitorOverviewController {

    @Resource
    private VisitorOverviewService visitorOverviewService;

    @Resource
    private ObjectMapper objectMapper;

    @GetMapping("/stats")
    @Operation(summary = "获取统计卡片数据")
    @PreAuthorize("@ss.hasPermission('security:visitor:query')")
    public CommonResult<VisitorOverviewStatsRespVO> getStats() {
        return success(visitorOverviewService.getStats());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表盘图表数据（被访人排行、事由分布、趋势、异常分布、时段分布）")
    @PreAuthorize("@ss.hasPermission('security:visitor:query')")
    public CommonResult<VisitorDashboardRespVO> getDashboard(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        return success(visitorOverviewService.getDashboard(dateFrom, dateTo));
    }

    @GetMapping("/today/page")
    @Operation(summary = "今日在访分页")
    @PreAuthorize("@ss.hasPermission('security:visitor:query')")
    public CommonResult<PageResult<VisitorAppointmentRespVO>> pageToday(@Valid VisitorAppointmentPageReqVO reqVO) {
        PageResult<VisitorAppointmentDO> pageResult = visitorOverviewService.pageTodayVisiting(reqVO);
        PageResult<VisitorAppointmentRespVO> resp = new PageResult<>();
        resp.setTotal(pageResult.getTotal());
        resp.setList(pageResult.getList().stream().map(this::toAppointmentResp).toList());
        return success(resp);
    }

    @GetMapping("/history/page")
    @Operation(summary = "来访记录分页")
    @PreAuthorize("@ss.hasPermission('security:visitor:query')")
    public CommonResult<PageResult<VisitorAppointmentRespVO>> pageHistory(@Valid VisitorAppointmentPageReqVO reqVO) {
        PageResult<VisitorAppointmentDO> pageResult = visitorOverviewService.pageHistory(reqVO);
        PageResult<VisitorAppointmentRespVO> resp = new PageResult<>();
        resp.setTotal(pageResult.getTotal());
        resp.setList(pageResult.getList().stream().map(this::toAppointmentResp).toList());
        return success(resp);
    }

    @GetMapping("/abnormal/page")
    @Operation(summary = "异常监控分页")
    @PreAuthorize("@ss.hasPermission('security:visitor:query')")
    public CommonResult<PageResult<VisitorAbnormalEventRespVO>> pageAbnormal(@Valid VisitorAbnormalEventPageReqVO reqVO) {
        PageResult<VisitorAbnormalEventDO> pageResult = visitorOverviewService.pageAbnormal(reqVO);
        return success(BeanUtils.toBean(pageResult, VisitorAbnormalEventRespVO.class));
    }

    private VisitorAppointmentRespVO toAppointmentResp(VisitorAppointmentDO appointment) {
        VisitorAppointmentRespVO respVO = BeanUtils.toBean(appointment, VisitorAppointmentRespVO.class);
        respVO.setAreas(parseAreas(appointment != null ? appointment.getAreas() : null));
        return respVO;
    }

    private List<String> parseAreas(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}

