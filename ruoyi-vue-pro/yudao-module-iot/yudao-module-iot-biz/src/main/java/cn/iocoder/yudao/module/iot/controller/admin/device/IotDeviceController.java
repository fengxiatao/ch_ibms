package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import jakarta.annotation.security.PermitAll;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.enums.product.IotLocationTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.util.VendorExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 设备")
@RestController
@RequestMapping("/iot/device")
@Validated
public class IotDeviceController {

    @Resource
    private IotDeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "创建设备")
    @PreAuthorize("@ss.hasPermission('iot:device:create')")
    public CommonResult<Long> createDevice(@Valid @RequestBody IotDeviceSaveReqVO createReqVO) {
        return success(deviceService.createDevice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDevice(@Valid @RequestBody IotDeviceSaveReqVO updateReqVO) {
        deviceService.updateDevice(updateReqVO);
        return success(true);
    }

    // TODO @长辉开发团队：参考阿里云：1）绑定网关；2）解绑网关

    @PutMapping("/update-group")
    @Operation(summary = "更新设备分组")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDeviceGroup(@Valid @RequestBody IotDeviceUpdateGroupReqVO updateReqVO) {
        deviceService.updateDeviceGroup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除单个设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteDevice(@RequestParam("id") Long id) {
        deviceService.deleteDevice(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "删除多个设备")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteDeviceList(@RequestParam("ids") Collection<Long> ids) {
        deviceService.deleteDeviceList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceRespVO> getDevice(@RequestParam("id") Long id) {
        IotDeviceDO device = deviceService.getDevice(id);
        IotDeviceRespVO vo = BeanUtils.toBean(device, IotDeviceRespVO.class);
        // 从 config 中提取 IP 地址和端口
        vo.setIpAddress(DeviceConfigHelper.getIpAddress(device));
        vo.setTcpPort(DeviceConfigHelper.getPort(device));
        // 将 DeviceConfig 对象序列化为 JSON 字符串
        vo.setConfig(DeviceConfigHelper.toJson(device.getConfig()));
        return success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备分页")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<IotDeviceRespVO>> getDevicePage(@Valid IotDevicePageReqVO pageReqVO) {
        PageResult<IotDeviceDO> pageResult = deviceService.getDevicePage(pageReqVO);
        // 转换为 VO 并从 config 中提取 IP 地址
        PageResult<IotDeviceRespVO> result = convertDevicePageToVO(pageResult);
        
        // ✅ 补充实时在线状态（从Gateway查询）
        deviceService.fillDeviceRealTimeStatus(result.getList());
        
        return success(result);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出设备 Excel")
    @PreAuthorize("@ss.hasPermission('iot:device:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDeviceExcel(@Valid IotDevicePageReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        CommonResult<PageResult<IotDeviceRespVO>> result = getDevicePage(exportReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "设备.xls", "数据", IotDeviceRespVO.class,
                result.getData().getList());
    }

    @GetMapping("/count")
    @Operation(summary = "获得设备数量")
    @Parameter(name = "productId", description = "产品编号", example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<Long> getDeviceCount(@RequestParam("productId") Long productId) {
        return success(deviceService.getDeviceCountByProductId(productId));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取设备的精简信息列表", description = "主要用于前端的下拉选项")
    @Parameters({
            @Parameter(name = "deviceType", description = "设备类型", example = "1"),
            @Parameter(name = "productId", description = "产品编号", example = "1024")
    })
    public CommonResult<List<IotDeviceRespVO>> getDeviceSimpleList(
            @RequestParam(value = "deviceType", required = false) Integer deviceType,
            @RequestParam(value = "productId", required = false) Long productId) {
        List<IotDeviceDO> list = deviceService.getDeviceListByCondition(deviceType, productId);
        return success(convertList(list, device ->  // 只返回 id、name、productId 字段
                new IotDeviceRespVO().setId(device.getId()).setDeviceName(device.getDeviceName())
                        .setProductId(device.getProductId()).setState(device.getState())));
    }

    @PostMapping("/import")
    @Operation(summary = "导入设备")
    @PreAuthorize("@ss.hasPermission('iot:device:import')")
    public CommonResult<IotDeviceImportRespVO> importDevice(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport)
            throws Exception {
        List<IotDeviceImportExcelVO> list = ExcelUtils.read(file, IotDeviceImportExcelVO.class);
        return success(deviceService.importDevice(list, updateSupport));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入设备模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<IotDeviceImportExcelVO> list = Arrays.asList(
                IotDeviceImportExcelVO.builder().deviceName("温度传感器001").parentDeviceName("gateway110")
                        .productKey("1de24640dfe").groupNames("灰度分组,生产分组")
                        .locationType(IotLocationTypeEnum.IP.getType()).build(),
                IotDeviceImportExcelVO.builder().deviceName("biubiu").productKey("YzvHxd4r67sT4s2B")
                        .groupNames("").locationType(IotLocationTypeEnum.MANUAL.getType()).build());
        // 输出
        ExcelUtils.write(response, "设备导入模板.xls", "数据", IotDeviceImportExcelVO.class, list);
    }

    @GetMapping("/get-auth-info")
    @Operation(summary = "获得设备连接信息")
    @PreAuthorize("@ss.hasPermission('iot:device:auth-info')")
    public CommonResult<IotDeviceAuthInfoRespVO> getDeviceAuthInfo(@RequestParam("id") Long id) {
        return success(deviceService.getDeviceAuthInfo(id));
    }

    // TODO @haohao：可以使用 @RequestParam("productKey") String productKey, @RequestParam("deviceNames") List<String> deviceNames 来接收哇？
    @GetMapping("/list-by-product-key-and-names")
    @Operation(summary = "通过产品标识和设备名称列表获取设备")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<IotDeviceRespVO>> getDevicesByProductKeyAndNames(@Valid IotDeviceByProductKeyAndNamesReqVO reqVO) {
        List<IotDeviceDO> devices = deviceService.getDeviceListByProductKeyAndNames(reqVO.getProductKey(), reqVO.getDeviceNames());
        return success(convertDeviceListToVO(devices));
    }

    /**
     * Gateway 专用接口：获取所有在线设备
     * 
     * <p>此接口供 Gateway 启动时初始化设备连接使用，无需认证
     * <p>响应中包含从设备配置中提取的 vendor 字段，用于确定设备连接方式
     */
    @GetMapping("/list-all-online")
    @Operation(summary = "获取所有在线设备（Gateway专用）", description = "供Gateway初始化使用，返回设备基本信息和连接配置")
    @PermitAll  // 允许网关无需登录即可访问
    @TenantIgnore  // 忽略租户隔离，获取所有租户的设备
    public CommonResult<List<IotDeviceRespVO>> getAllOnlineDevices() {
        List<IotDeviceDO> devices = deviceService.getOnlineDeviceList();
        // 转换为 VO 并提取 vendor 和 IP 信息
        List<IotDeviceRespVO> voList = devices.stream()
                .map(device -> {
                    IotDeviceRespVO vo = BeanUtils.toBean(device, IotDeviceRespVO.class);
                    // 从设备配置中提取 vendor 信息
                    vo.setVendor(VendorExtractor.extractVendor(device.getConfig()));
                    // 从 config 中提取 IP 地址和端口
                    vo.setIpAddress(DeviceConfigHelper.getIpAddress(device));
                    vo.setTcpPort(DeviceConfigHelper.getPort(device));
                    // 将 DeviceConfig 对象序列化为 JSON 字符串（BeanUtils.toBean 会调用 toString() 导致格式错误）
                    vo.setConfig(DeviceConfigHelper.toJson(device.getConfig()));
                    return vo;
                })
                .collect(Collectors.toList());
        return success(voList);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 将设备 DO 列表转换为 VO 列表，并从 config 中提取 IP 地址
     */
    private List<IotDeviceRespVO> convertDeviceListToVO(List<IotDeviceDO> devices) {
        return devices.stream()
                .map(this::convertDeviceToVO)
                .collect(Collectors.toList());
    }

    /**
     * 将设备分页结果转换为 VO 分页结果，并从 config 中提取 IP 地址
     */
    private PageResult<IotDeviceRespVO> convertDevicePageToVO(PageResult<IotDeviceDO> pageResult) {
        List<IotDeviceRespVO> voList = convertDeviceListToVO(pageResult.getList());
        return new PageResult<>(voList, pageResult.getTotal());
    }

    /**
     * 将单个设备 DO 转换为 VO，并从 config 中提取 IP 地址
     */
    private IotDeviceRespVO convertDeviceToVO(IotDeviceDO device) {
        IotDeviceRespVO vo = BeanUtils.toBean(device, IotDeviceRespVO.class);
        // 从 config 中提取 IP 地址和端口
        vo.setIpAddress(DeviceConfigHelper.getIpAddress(device));
        vo.setTcpPort(DeviceConfigHelper.getPort(device));
        // 将 DeviceConfig 对象序列化为 JSON 字符串
        vo.setConfig(DeviceConfigHelper.toJson(device.getConfig()));
        return vo;
    }

}
