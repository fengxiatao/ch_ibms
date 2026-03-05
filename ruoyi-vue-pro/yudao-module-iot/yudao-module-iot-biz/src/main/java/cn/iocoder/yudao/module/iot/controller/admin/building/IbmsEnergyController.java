package cn.iocoder.yudao.module.iot.controller.admin.building;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.*;
import cn.iocoder.yudao.module.iot.service.building.IbmsEnergyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 能耗计量 Controller
 *
 * @author 智慧楼宇系统
 */
@Tag(name = "管理后台 - 能耗计量")
@RestController
@RequestMapping("/iot/building/energy")
@Validated
public class IbmsEnergyController {

    @Resource
    private IbmsEnergyService energyService;

    // ======================= 仪表管理 =======================

    @GetMapping("/meter/page")
    @Operation(summary = "获得能耗仪表分页")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<PageResult<IbmsEnergyMeterRespVO>> getMeterPage(@Valid IbmsEnergyMeterPageReqVO pageReqVO) {
        PageResult<IbmsEnergyMeterDO> pageResult = energyService.getMeterPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsEnergyMeterRespVO.class));
    }

    @GetMapping("/meter/get")
    @Operation(summary = "获得能耗仪表")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<IbmsEnergyMeterRespVO> getMeter(@RequestParam("id") Long id) {
        IbmsEnergyMeterDO meter = energyService.getMeter(id);
        return success(BeanUtils.toBean(meter, IbmsEnergyMeterRespVO.class));
    }

    @GetMapping("/meter/list-by-type")
    @Operation(summary = "获得仪表列表（按类型）")
    @Parameter(name = "meterType", description = "仪表类型", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<List<IbmsEnergyMeterRespVO>> getMeterListByType(@RequestParam("meterType") Integer meterType) {
        List<IbmsEnergyMeterDO> list = energyService.getMeterListByType(meterType);
        return success(BeanUtils.toBean(list, IbmsEnergyMeterRespVO.class));
    }

    @GetMapping("/meter/list")
    @Operation(summary = "获得全部仪表列表")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<List<IbmsEnergyMeterRespVO>> getMeterList() {
        List<IbmsEnergyMeterDO> list = energyService.getMeterList();
        return success(BeanUtils.toBean(list, IbmsEnergyMeterRespVO.class));
    }

    @PostMapping("/meter/create")
    @Operation(summary = "创建能耗仪表")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:create')")
    public CommonResult<Long> createMeter(@Valid @RequestBody IbmsEnergyMeterSaveReqVO createReqVO) {
        return success(energyService.createMeter(createReqVO));
    }

    @PutMapping("/meter/update")
    @Operation(summary = "更新能耗仪表")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:update')")
    public CommonResult<Boolean> updateMeter(@Valid @RequestBody IbmsEnergyMeterSaveReqVO updateReqVO) {
        energyService.updateMeter(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/meter/delete")
    @Operation(summary = "删除能耗仪表")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-energy:delete')")
    public CommonResult<Boolean> deleteMeter(@RequestParam("id") Long id) {
        energyService.deleteMeter(id);
        return success(true);
    }

    // ======================= 采集记录 =======================

    @GetMapping("/record/page")
    @Operation(summary = "获得能耗采集记录分页")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<PageResult<IbmsEnergyRecordRespVO>> getRecordPage(@Valid IbmsEnergyRecordPageReqVO pageReqVO) {
        PageResult<IbmsEnergyRecordDO> pageResult = energyService.getRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsEnergyRecordRespVO.class));
    }

    @GetMapping("/record/latest")
    @Operation(summary = "获得仪表最新采集记录")
    @Parameter(name = "meterId", description = "仪表ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<IbmsEnergyRecordRespVO> getLatestRecord(@RequestParam("meterId") Long meterId) {
        IbmsEnergyRecordDO record = energyService.getLatestRecord(meterId);
        return success(BeanUtils.toBean(record, IbmsEnergyRecordRespVO.class));
    }

    // ======================= 统计分析 =======================

    /** DO 字段 statDate/consumption 与 VO 字段 statisticsDate/dailyUsage 映射 */
    private List<IbmsEnergyStatisticsRespVO> convertStatisticsToResp(List<IbmsEnergyStatisticsDailyDO> list) {
        return list.stream().map(d -> {
            IbmsEnergyStatisticsRespVO vo = BeanUtils.toBean(d, IbmsEnergyStatisticsRespVO.class);
            vo.setStatisticsDate(d.getStatDate());
            vo.setDailyUsage(d.getConsumption());
            return vo;
        }).collect(Collectors.toList());
    }

    @GetMapping("/statistics/page")
    @Operation(summary = "获得能耗日统计分页")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<PageResult<IbmsEnergyStatisticsRespVO>> getStatisticsPage(@Valid IbmsEnergyStatisticsPageReqVO pageReqVO) {
        PageResult<IbmsEnergyStatisticsDailyDO> pageResult = energyService.getStatisticsPage(pageReqVO);
        return success(new PageResult<>(convertStatisticsToResp(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/statistics/by-date-range")
    @Operation(summary = "获得指定日期范围的能耗统计")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<List<IbmsEnergyStatisticsRespVO>> getStatisticsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<IbmsEnergyStatisticsDailyDO> list = energyService.getStatisticsByDateRange(startDate, endDate);
        return success(convertStatisticsToResp(list));
    }

    @GetMapping("/statistics/meter-by-date-range")
    @Operation(summary = "获得仪表指定日期范围的能耗统计")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<List<IbmsEnergyStatisticsRespVO>> getMeterStatisticsByDateRange(
            @RequestParam("meterId") Long meterId,
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<IbmsEnergyStatisticsDailyDO> list = energyService.getMeterStatisticsByDateRange(meterId, startDate, endDate);
        return success(convertStatisticsToResp(list));
    }

    @GetMapping("/statistics/by-type-and-date-range")
    @Operation(summary = "按类型获得指定日期范围的能耗统计")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<List<IbmsEnergyStatisticsRespVO>> getStatisticsByTypeAndDateRange(
            @RequestParam("meterType") Integer meterType,
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<IbmsEnergyStatisticsDailyDO> list = energyService.getStatisticsByTypeAndDateRange(meterType, startDate, endDate);
        return success(convertStatisticsToResp(list));
    }

    // ======================= 告警管理 =======================

    @GetMapping("/alarm/page")
    @Operation(summary = "获得能耗告警分页")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<PageResult<IbmsEnergyAlarmRespVO>> getAlarmPage(@Valid IbmsEnergyAlarmPageReqVO pageReqVO) {
        PageResult<IbmsEnergyAlarmDO> pageResult = energyService.getAlarmPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsEnergyAlarmRespVO.class));
    }

    @GetMapping("/alarm/get")
    @Operation(summary = "获得告警详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<IbmsEnergyAlarmRespVO> getAlarm(@RequestParam("id") Long id) {
        IbmsEnergyAlarmDO alarm = energyService.getAlarm(id);
        return success(BeanUtils.toBean(alarm, IbmsEnergyAlarmRespVO.class));
    }

    @PutMapping("/alarm/handle")
    @Operation(summary = "处理告警（确认）")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:update')")
    public CommonResult<Boolean> handleAlarm(
            @RequestParam("id") Long id,
            @RequestParam("handler") String handler,
            @RequestParam(value = "handleRemark", required = false) String handleRemark) {
        energyService.handleAlarm(id, handler, handleRemark);
        return success(true);
    }

    @PutMapping("/alarm/ignore")
    @Operation(summary = "忽略告警")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:update')")
    public CommonResult<Boolean> ignoreAlarm(
            @RequestParam("id") Long id,
            @RequestParam("handler") String handler,
            @RequestParam(value = "handleRemark", required = false) String handleRemark) {
        energyService.ignoreAlarm(id, handler, handleRemark);
        return success(true);
    }

    @GetMapping("/alarm/today-count")
    @Operation(summary = "获得今日告警数量")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<Long> getTodayAlarmCount() {
        return success(energyService.getTodayAlarmCount());
    }

    @GetMapping("/alarm/unhandled-count")
    @Operation(summary = "获得未处理告警数量")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<Long> getUnhandledAlarmCount() {
        return success(energyService.getUnhandledAlarmCount());
    }

    // ======================= 费率设置 =======================

    @GetMapping("/rate/page")
    @Operation(summary = "获得费率设置分页")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<PageResult<IbmsEnergyRateRespVO>> getRatePage(@Valid IbmsEnergyRatePageReqVO pageReqVO) {
        PageResult<IbmsEnergyRateDO> pageResult = energyService.getRatePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsEnergyRateRespVO.class));
    }

    @GetMapping("/rate/get")
    @Operation(summary = "获得费率设置详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<IbmsEnergyRateRespVO> getRate(@RequestParam("id") Long id) {
        IbmsEnergyRateDO rate = energyService.getRate(id);
        return success(BeanUtils.toBean(rate, IbmsEnergyRateRespVO.class));
    }

    @GetMapping("/rate/list-by-energy-type")
    @Operation(summary = "获得费率列表（按能源类型）")
    @Parameter(name = "energyType", description = "能源类型", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<List<IbmsEnergyRateRespVO>> getRateListByEnergyType(@RequestParam("energyType") Integer energyType) {
        List<IbmsEnergyRateDO> list = energyService.getRateListByEnergyType(energyType);
        return success(BeanUtils.toBean(list, IbmsEnergyRateRespVO.class));
    }

    @PostMapping("/rate/create")
    @Operation(summary = "创建费率设置")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:create')")
    public CommonResult<Long> createRate(@Valid @RequestBody IbmsEnergyRateSaveReqVO createReqVO) {
        return success(energyService.createRate(createReqVO));
    }

    @PutMapping("/rate/update")
    @Operation(summary = "更新费率设置")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:update')")
    public CommonResult<Boolean> updateRate(@Valid @RequestBody IbmsEnergyRateSaveReqVO updateReqVO) {
        energyService.updateRate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/rate/delete")
    @Operation(summary = "删除费率设置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-energy:delete')")
    public CommonResult<Boolean> deleteRate(@RequestParam("id") Long id) {
        energyService.deleteRate(id);
        return success(true);
    }

    // ======================= 人工抄表 =======================

    @GetMapping("/manual-reading/page")
    @Operation(summary = "获得人工抄表记录分页")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<PageResult<IbmsEnergyManualReadingRespVO>> getManualReadingPage(@Valid IbmsEnergyManualReadingPageReqVO pageReqVO) {
        PageResult<IbmsEnergyManualReadingDO> pageResult = energyService.getManualReadingPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IbmsEnergyManualReadingRespVO.class));
    }

    @GetMapping("/manual-reading/today")
    @Operation(summary = "获得今日人工抄表记录")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<List<IbmsEnergyManualReadingRespVO>> getTodayManualReadings() {
        List<IbmsEnergyManualReadingDO> list = energyService.getTodayManualReadings();
        return success(BeanUtils.toBean(list, IbmsEnergyManualReadingRespVO.class));
    }

    @GetMapping("/manual-reading/latest")
    @Operation(summary = "获得仪表最新人工抄表记录")
    @Parameter(name = "meterId", description = "仪表ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<IbmsEnergyManualReadingRespVO> getLatestManualReading(@RequestParam("meterId") Long meterId) {
        IbmsEnergyManualReadingDO reading = energyService.getLatestManualReading(meterId);
        return success(BeanUtils.toBean(reading, IbmsEnergyManualReadingRespVO.class));
    }

    @PostMapping("/manual-reading/create")
    @Operation(summary = "创建人工抄表记录")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:create')")
    public CommonResult<Long> createManualReading(@Valid @RequestBody IbmsEnergyManualReadingSaveReqVO createReqVO) {
        return success(energyService.createManualReading(createReqVO));
    }

    @PutMapping("/manual-reading/review")
    @Operation(summary = "复核人工抄表记录")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:update')")
    public CommonResult<Boolean> reviewManualReading(
            @RequestParam("id") Long id,
            @RequestParam("reviewer") String reviewer) {
        energyService.reviewManualReading(id, reviewer);
        return success(true);
    }

    @PutMapping("/manual-reading/void")
    @Operation(summary = "作废人工抄表记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building-energy:update')")
    public CommonResult<Boolean> voidManualReading(@RequestParam("id") Long id) {
        energyService.voidManualReading(id);
        return success(true);
    }

    // ======================= 总览 =======================

    @GetMapping("/overview")
    @Operation(summary = "获得能耗总览数据")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<IbmsEnergyOverviewVO> getOverview() {
        return success(energyService.getOverview());
    }

    @GetMapping("/overview-by-range")
    @Operation(summary = "按日期范围获得能耗总览数据")
    @PreAuthorize("@ss.hasPermission('iot:building-energy:query')")
    public CommonResult<IbmsEnergyOverviewVO> getOverviewByRange(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return success(energyService.getOverviewByRange(startDate, endDate));
    }

}
