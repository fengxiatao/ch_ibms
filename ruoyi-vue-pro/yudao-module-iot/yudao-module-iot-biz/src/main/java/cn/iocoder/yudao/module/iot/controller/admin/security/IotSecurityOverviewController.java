package cn.iocoder.yudao.module.iot.controller.admin.security;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.PlayUrlRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.SecurityOverviewCameraPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.SecurityOverviewCameraRespVO;
import cn.iocoder.yudao.module.iot.service.security.IotSecurityOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 安防概览 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IoT安防概览")
@RestController
@RequestMapping("/iot/security-overview")
@Validated
@Slf4j
public class IotSecurityOverviewController {

    @Resource
    private IotSecurityOverviewService securityOverviewService;

    /**
     * 获取安防概览摄像头列表
     * 
     * 功能说明：
     * 1. 查询所有配置了"安防概览"的网络摄像头设备
     * 2. 支持菜单继承机制（设备继承产品菜单配置）
     * 3. 返回设备实时状态和配置信息
     * 4. 可选返回实时抓图（base64格式）
     */
    @GetMapping("/cameras")
    @Operation(summary = "获取安防概览摄像头列表")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<SecurityOverviewCameraRespVO>> getSecurityOverviewCameras(
            @Valid SecurityOverviewCameraPageReqVO reqVO) {
        
        log.info("[安防概览] 接收请求: pageNo={}, pageSize={}, includeSnapshot={}, onlineOnly={}", 
                reqVO.getPageNo(), reqVO.getPageSize(), reqVO.getIncludeSnapshot(), reqVO.getOnlineOnly());
        
        PageResult<SecurityOverviewCameraRespVO> pageResult = 
            securityOverviewService.getSecurityOverviewCameras(reqVO);
        
        log.info("[安防概览] 返回结果: total={}, listSize={}", 
                pageResult.getTotal(), pageResult.getList().size());
        
        return success(pageResult);
    }

    /**
     * 获取单个设备的实时抓图
     */
    @GetMapping("/snapshot/{deviceId}")
    @Operation(summary = "获取设备实时抓图")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<String> getDeviceSnapshot(@PathVariable("deviceId") Long deviceId) {
        
        log.info("[安防概览] 获取设备抓图: deviceId={}", deviceId);
        
        String snapshotBase64 = securityOverviewService.getDeviceSnapshot(deviceId);
        
        if (snapshotBase64 != null) {
            return success("data:image/jpeg;base64," + snapshotBase64);
        } else {
            return success(null);
        }
    }

    /**
     * 获取设备播放地址
     * 
     * 功能说明：
     * 1. 调用 Gateway 的 getPlayUrl 服务
     * 2. 返回 HLS/FLV/RTMP 播放地址
     * 3. 自动创建流代理（VIP级别）
     */
    @GetMapping("/play-url/{deviceId}")
    @Operation(summary = "获取设备播放地址")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PlayUrlRespVO> getPlayUrl(@PathVariable("deviceId") Long deviceId) {
        
        log.info("[安防概览] 获取播放地址: deviceId={}", deviceId);
        
        PlayUrlRespVO playUrl = securityOverviewService.getPlayUrl(deviceId);
        
        log.info("[安防概览] 返回播放地址: wsFlv={}, webrtc={}, wsFmp4={}, fmp4={}, flv={}, hls={}", 
                playUrl.getWsFlvUrl(), playUrl.getWebrtcUrl(), playUrl.getWsFmp4Url(), playUrl.getFmp4Url(), playUrl.getFlvUrl(), playUrl.getHlsUrl());
        
        return success(playUrl);
    }
}

