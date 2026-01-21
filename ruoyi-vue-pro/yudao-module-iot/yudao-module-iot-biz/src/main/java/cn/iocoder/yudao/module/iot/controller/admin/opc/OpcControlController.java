package cn.iocoder.yudao.module.iot.controller.admin.opc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.service.opc.OpcControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.*;

/**
 * OPC 控制 Controller
 * 
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - OPC控制")
@RestController
@RequestMapping("/iot/opc/control")
public class OpcControlController {

    @Resource
    private OpcControlService opcControlService;

    @PostMapping("/arm")
    @Operation(summary = "布防")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PermitAll
    public CommonResult<Boolean> arm(@RequestParam("deviceId") Long deviceId) {
        Boolean result = opcControlService.arm(deviceId);
        return CommonResult.success(result);
    }

    @PostMapping("/disarm")
    @Operation(summary = "撤防")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PermitAll
    public CommonResult<Boolean> disarm(@RequestParam("deviceId") Long deviceId) {
        Boolean result = opcControlService.disarm(deviceId);
        return CommonResult.success(result);
    }

    @GetMapping("/query-status")
    @Operation(summary = "查询状态")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PermitAll
    public CommonResult<Boolean> queryStatus(@RequestParam("deviceId") Long deviceId) {
        Boolean result = opcControlService.queryStatus(deviceId);
        return CommonResult.success(result);
    }
}
