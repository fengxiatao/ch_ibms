package cn.iocoder.yudao.module.iot.controller.admin.changhui;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataHistoryReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataQueryReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataSaveReqVO;
import cn.iocoder.yudao.module.iot.service.changhui.data.ChanghuiDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 长辉设备数据 Controller
 * 
 * <p>提供设备数据的保存、查询、历史数据查询和导出功能
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - 长辉设备数据")
@RestController
@RequestMapping("/iot/changhui/data")
@Validated
public class ChanghuiDataController {

    @Resource
    private ChanghuiDataService dataService;

    @PostMapping("/save")
    @Operation(summary = "保存数据")
    @PreAuthorize("@ss.hasPermission('iot:changhui-data:create')")
    public CommonResult<Long> saveData(@Valid @RequestBody ChanghuiDataSaveReqVO reqVO) {
        return success(dataService.saveData(reqVO));
    }

    @PostMapping("/save-batch")
    @Operation(summary = "批量保存数据")
    @PreAuthorize("@ss.hasPermission('iot:changhui-data:create')")
    public CommonResult<Boolean> saveBatchData(@Valid @RequestBody List<ChanghuiDataSaveReqVO> reqVOList) {
        dataService.saveBatchData(reqVOList);
        return success(true);
    }

    @GetMapping("/query")
    @Operation(summary = "查询单个指标数据（最新）")
    @Parameter(name = "stationCode", description = "测站编码", required = true)
    @Parameter(name = "indicator", description = "指标类型", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-data:query')")
    public CommonResult<ChanghuiDataRespVO> queryData(
            @RequestParam("stationCode") String stationCode,
            @RequestParam("indicator") String indicator) {
        return success(dataService.queryData(stationCode, indicator));
    }

    @PostMapping("/query-multiple")
    @Operation(summary = "查询多个指标数据（最新）")
    @PreAuthorize("@ss.hasPermission('iot:changhui-data:query')")
    public CommonResult<List<ChanghuiDataRespVO>> queryMultipleData(@Valid @RequestBody ChanghuiDataQueryReqVO reqVO) {
        return success(dataService.queryMultipleData(reqVO.getStationCode(), reqVO.getIndicators()));
    }

    @GetMapping("/latest")
    @Operation(summary = "获取设备最新数据（所有指标）")
    @Parameter(name = "stationCode", description = "测站编码", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-data:query')")
    public CommonResult<Map<String, ChanghuiDataRespVO>> getLatestData(
            @RequestParam("stationCode") String stationCode) {
        return success(dataService.getLatestData(stationCode));
    }

    @GetMapping("/latest-by-device")
    @Operation(summary = "根据设备ID获取最新数据")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-data:query')")
    public CommonResult<Map<String, ChanghuiDataRespVO>> getLatestDataByDeviceId(
            @RequestParam("deviceId") Long deviceId) {
        return success(dataService.getLatestDataByDeviceId(deviceId));
    }

    @GetMapping("/history")
    @Operation(summary = "获取历史数据（分页）")
    @PreAuthorize("@ss.hasPermission('iot:changhui-data:query')")
    public CommonResult<PageResult<ChanghuiDataRespVO>> getHistoryData(@Valid ChanghuiDataHistoryReqVO reqVO) {
        return success(dataService.getHistoryData(reqVO));
    }

    @GetMapping("/export")
    @Operation(summary = "导出历史数据")
    @PreAuthorize("@ss.hasPermission('iot:changhui-data:export')")
    public void exportHistoryData(@Valid ChanghuiDataHistoryReqVO reqVO, HttpServletResponse response) throws IOException {
        byte[] data = dataService.exportHistoryData(reqVO);
        
        // 设置响应头
        response.setContentType("text/csv;charset=UTF-8");
        String fileName = URLEncoder.encode("长辉设备数据.csv", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setHeader("Content-Length", String.valueOf(data.length));
        
        // 写入响应
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    @DeleteMapping("/delete-by-device")
    @Operation(summary = "删除设备的所有数据")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-data:delete')")
    public CommonResult<Boolean> deleteDataByDeviceId(@RequestParam("deviceId") Long deviceId) {
        dataService.deleteDataByDeviceId(deviceId);
        return success(true);
    }

    @DeleteMapping("/delete-by-station-code")
    @Operation(summary = "删除设备的所有数据（根据测站编码）")
    @Parameter(name = "stationCode", description = "测站编码", required = true)
    @PreAuthorize("@ss.hasPermission('iot:changhui-data:delete')")
    public CommonResult<Boolean> deleteDataByStationCode(@RequestParam("stationCode") String stationCode) {
        dataService.deleteDataByStationCode(stationCode);
        return success(true);
    }

}
