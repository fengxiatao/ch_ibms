package cn.iocoder.yudao.module.iot.controller.admin.camera;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.camera.vo.IotCameraForGatewayRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.camera.vo.IotCameraPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.camera.vo.IotCameraRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.camera.vo.IotCameraSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraDO;
import cn.iocoder.yudao.module.iot.service.camera.IotCameraService;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 摄像头管理 Controller
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - IoT 摄像头管理")
@RestController
@RequestMapping("/iot/camera")
@Validated
public class IotCameraController {

    @Resource
    private IotCameraService cameraService;

    @PostMapping("/create")
    @Operation(summary = "创建摄像头配置")
    @PreAuthorize("@ss.hasPermission('iot:camera:create')")
    public CommonResult<Long> createCamera(@Valid @RequestBody IotCameraSaveReqVO createReqVO) {
        return success(cameraService.createCamera(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新摄像头配置")
    @PreAuthorize("@ss.hasPermission('iot:camera:update')")
    public CommonResult<Boolean> updateCamera(@Valid @RequestBody IotCameraSaveReqVO updateReqVO) {
        cameraService.updateCamera(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除摄像头配置")
    @Parameter(name = "id", description = "摄像头ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:delete')")
    public CommonResult<Boolean> deleteCamera(@RequestParam("id") Long id) {
        cameraService.deleteCamera(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得摄像头配置")
    @Parameter(name = "id", description = "摄像头ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<IotCameraRespVO> getCamera(@RequestParam("id") Long id) {
        IotCameraDO camera = cameraService.getCamera(id);
        return success(BeanUtils.toBean(camera, IotCameraRespVO.class));
    }

    @GetMapping("/get-by-device")
    @Operation(summary = "根据设备ID获取摄像头配置")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<IotCameraRespVO> getCameraByDeviceId(@RequestParam("deviceId") Long deviceId) {
        IotCameraDO camera = cameraService.getCameraByDeviceId(deviceId);
        return success(BeanUtils.toBean(camera, IotCameraRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得摄像头配置分页")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<PageResult<IotCameraRespVO>> getCameraPage(@Valid IotCameraPageReqVO pageReqVO) {
        PageResult<IotCameraDO> pageResult = cameraService.getCameraPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotCameraRespVO.class));
    }

    @PostMapping("/test-connection")
    @Operation(summary = "测试摄像头连接")
    @Parameter(name = "id", description = "摄像头ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:camera:test')")
    public CommonResult<Boolean> testConnection(@RequestParam("id") Long id) {
        return success(cameraService.testConnection(id));
    }

    @GetMapping("/stream-url")
    @Operation(summary = "获取视频流地址")
    @Parameter(name = "id", description = "摄像头ID", required = true, example = "1")
    @Parameter(name = "streamType", description = "码流类型(main:主码流, sub:子码流)", example = "main")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<String> getStreamUrl(@RequestParam("id") Long id,
                                              @RequestParam(value = "streamType", defaultValue = "main") String streamType) {
        return success(cameraService.getStreamUrl(id, streamType));
    }

    @GetMapping("/list-for-gateway")
    @Operation(summary = "获取所有摄像头设备列表（供网关使用）")
    @PermitAll // 允许网关无需登录即可访问
    @TenantIgnore // 忽略租户隔离，获取所有租户的设备（网关需要管理所有设备）
    public CommonResult<java.util.List<IotCameraForGatewayRespVO>> listForGateway() {
        return success(cameraService.listForGateway());
    }

}
