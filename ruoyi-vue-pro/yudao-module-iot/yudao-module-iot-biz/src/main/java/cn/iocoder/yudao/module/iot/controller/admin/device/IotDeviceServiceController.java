package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.service.IotDeviceServiceInvokeReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.service.IotDeviceServiceLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.service.IotDeviceServiceLogRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceServiceLogDO;
import cn.iocoder.yudao.module.iot.service.device.service.IotDeviceServiceInvokeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 设备服务调用控制器
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - IoT 设备服务调用")
@RestController
@RequestMapping("/iot/device/service")
@Validated
public class IotDeviceServiceController {

    @Resource
    private IotDeviceServiceInvokeService serviceInvokeService;

    @PostMapping("/invoke")
    @Operation(summary = "调用设备服务")
    public CommonResult<Map<String, String>> invokeService(@Valid @RequestBody IotDeviceServiceInvokeReqVO reqVO) {
        String requestId = serviceInvokeService.invokeDeviceService(reqVO);
        
        Map<String, String> result = new HashMap<>();
        result.put("requestId", requestId);
        result.put("message", "服务调用请求已发送");
        
        return success(result);
    }

    @GetMapping("/log/page")
    @Operation(summary = "获取服务调用日志分页")
    public CommonResult<PageResult<IotDeviceServiceLogRespVO>> getServiceLogPage(
            @Valid IotDeviceServiceLogPageReqVO pageReqVO) {
        PageResult<IotDeviceServiceLogDO> pageResult = serviceInvokeService.getServiceLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceServiceLogRespVO.class));
    }

    @GetMapping("/log/get")
    @Operation(summary = "获取服务调用日志详情")
    @Parameter(name = "id", description = "日志ID", required = true, example = "1")
    public CommonResult<IotDeviceServiceLogRespVO> getServiceLog(@RequestParam("id") Long id) {
        IotDeviceServiceLogDO serviceLog = serviceInvokeService.getServiceLog(id);
        return success(BeanUtils.toBean(serviceLog, IotDeviceServiceLogRespVO.class));
    }
}












