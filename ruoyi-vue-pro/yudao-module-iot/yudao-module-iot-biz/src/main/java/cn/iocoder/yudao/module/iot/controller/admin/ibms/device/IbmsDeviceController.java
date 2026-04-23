package cn.iocoder.yudao.module.iot.controller.admin.ibms.device;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceImportExcelVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceImportRespVO;
import cn.iocoder.yudao.module.iot.enums.product.IotLocationTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.support.IbmsLegacyIotDeviceAdapterService;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceExcelVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceSimpleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo.IbmsDeviceUpdateGroupReqVO;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.UPDATE;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IBMS 设备管理
 */
@Tag(name = "管理后台 - IBMS 设备管理")
@RestController
@RequestMapping("/iot/ibms/device")
@Validated
public class IbmsDeviceController {

    @Resource
    private IbmsDeviceService ibmsDeviceService;

    /**
     * 复用 legacy Excel 导入逻辑：适配器会把 create/update 写入收口到 ibms_device / ibms_device_runtime。
     */
    @Resource
    private IbmsLegacyIotDeviceAdapterService legacyIotDeviceAdapterService;

    @PostMapping("/create")
    @Operation(summary = "创建设备")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:create')")
    public CommonResult<Long> createDevice(@Valid @RequestBody IbmsDeviceSaveReqVO reqVO) {
        return success(ibmsDeviceService.createDevice(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:update')")
    public CommonResult<Boolean> updateDevice(@Valid @RequestBody IbmsDeviceSaveReqVO reqVO) {
        ibmsDeviceService.updateDevice(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:delete')")
    public CommonResult<Boolean> deleteDevice(@RequestParam("id") Long id) {
        ibmsDeviceService.deleteDevice(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除设备")
    @Parameter(name = "ids", description = "设备主键，逗号分隔", required = true)
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:delete')")
    public CommonResult<Boolean> deleteDeviceList(@RequestParam("ids") Collection<Long> ids) {
        ibmsDeviceService.deleteDeviceList(ids);
        return success(true);
    }

    @PutMapping("/update-group")
    @Operation(summary = "批量更新设备分组", description = "写入 ibms_device.group_ids，并重推网关 Profile（如有）")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:update')")
    public CommonResult<Boolean> updateDeviceGroup(@Valid @RequestBody IbmsDeviceUpdateGroupReqVO reqVO) {
        ibmsDeviceService.updateDeviceGroup(reqVO.getIds(), reqVO.getGroupIds());
        return success(true);
    }

    @GetMapping("/count")
    @Operation(summary = "按产品统计设备数量（IBMS 单台账）", description = "仅统计 ibms_device.ibms_product_id")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:query')")
    public CommonResult<Long> countByProduct(@RequestParam("ibmsProductId") Long ibmsProductId) {
        return success(ibmsDeviceService.countDevicesByProduct(ibmsProductId));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "精简设备列表（IBMS）", description = "下拉选项等；含运行态 state")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:query')")
    public CommonResult<List<IbmsDeviceSimpleRespVO>> simpleList(
            @RequestParam(value = "deviceType", required = false) Integer deviceType,
            @RequestParam(value = "ibmsProductId", required = false) Long ibmsProductId) {
        return success(ibmsDeviceService.listSimpleDevices(deviceType, ibmsProductId));
    }

    @GetMapping("/get")
    @Operation(summary = "获取设备详情")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:query')")
    public CommonResult<IbmsDeviceRespVO> getDevice(@RequestParam("id") Long id) {
        return success(ibmsDeviceService.getDevice(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询设备")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:query')")
    public CommonResult<PageResult<IbmsDeviceRespVO>> getDevicePage(@Valid IbmsDevicePageReqVO reqVO) {
        return success(ibmsDeviceService.getDevicePage(reqVO));
    }

    @PutMapping("/sync-runtime")
    @Operation(summary = "同步设备及点位运行态")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:update')")
    public CommonResult<Boolean> syncRuntime(@RequestParam("id") Long id,
                                             @RequestParam("online") Boolean online,
                                             @RequestParam(value = "pointsAlarm", required = false) Integer pointsAlarm) {
        ibmsDeviceService.syncRuntime(id, Boolean.TRUE.equals(online), pointsAlarm);
        return success(true);
    }

    @PostMapping("/repush-gateway-profiles")
    @Operation(summary = "全量重推网关设备 Profile",
            description = "按当前登录租户，将本租户全部 IBMS 设备通过 MQ 下发给 NewGateway，用于缓存预热或冷启动后对齐。")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Integer> repushGatewayProfiles() {
        return success(ibmsDeviceService.repushAllGatewayProfiles());
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出设备 Excel")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDeviceExcel(@Valid IbmsDevicePageReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<IbmsDeviceRespVO> pageResult = ibmsDeviceService.getDevicePage(exportReqVO);
        List<IbmsDeviceExcelVO> rows = BeanUtils.toBean(pageResult.getList(), IbmsDeviceExcelVO.class);
        ExcelUtils.write(response, "IBMS设备.xls", "数据", IbmsDeviceExcelVO.class, rows);
    }

    @PostMapping("/import")
    @Operation(summary = "导入设备（IBMS）",
            description = "使用与 legacy 一致的设备 Excel 导入格式；写入收口到 ibms_device / ibms_device_runtime。")
    @PreAuthorize("@ss.hasPermission('iot:ibms-device:update')")
    public CommonResult<IotDeviceImportRespVO> importDevice(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport)
            throws Exception {
        List<IotDeviceImportExcelVO> list = ExcelUtils.read(file, IotDeviceImportExcelVO.class);
        return success(legacyIotDeviceAdapterService.importDevice(list, Boolean.TRUE.equals(updateSupport)));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入设备模板（IBMS）",
            description = "与 legacy Excel 导入模板兼容：字段用于解析产品/设备层级与分组映射。")
    public void importTemplate(HttpServletResponse response) throws IOException {
        List<IotDeviceImportExcelVO> list = Arrays.asList(
                IotDeviceImportExcelVO.builder().deviceName("温度传感器001").parentDeviceName("gateway110")
                        .productKey("1de24640dfe").groupNames("灰度分组,生产分组")
                        .locationType(IotLocationTypeEnum.IP.getType()).build(),
                IotDeviceImportExcelVO.builder().deviceName("biubiu").productKey("YzvHxd4r67sT4s2B")
                        .groupNames("").locationType(IotLocationTypeEnum.MANUAL.getType()).build());
        ExcelUtils.write(response, "IBMS设备导入模板.xls", "数据", IotDeviceImportExcelVO.class, list);
    }
}

