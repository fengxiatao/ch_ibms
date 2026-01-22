package cn.iocoder.yudao.module.iot.controller.admin.video;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.NvrChannelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.NvrRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.video.nvr.NvrCommandService;
import cn.iocoder.yudao.module.iot.service.video.nvr.NvrQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - NVR查询")
@RestController
@RequestMapping("/iot/video/nvr")
@Validated
@Slf4j
public class NvrController {

    @Resource
    private NvrQueryService nvrQueryService;

    @Resource
    private NvrCommandService nvrCommandService;

    @Resource
    private IotDeviceChannelService channelService;

    @GetMapping("/list")
    @Operation(summary = "获取NVR列表（全部）", description = "用于下拉选择等场景，不分页")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<List<NvrRespVO>> getNvrList() {
        List<IotDeviceDO> list = nvrQueryService.getNvrList();
        List<NvrRespVO> result = list.stream().map(d -> {
            NvrRespVO vo = new NvrRespVO();
            vo.setId(d.getId());
            vo.setName(StringUtils.defaultIfBlank(d.getDeviceName(), d.getNickname()));
            // 从 config 中提取 IP 地址
            vo.setIpAddress(DeviceConfigHelper.getIpAddress(d));
            vo.setState(d.getState());
            return vo;
        }).collect(Collectors.toList());
        return success(result);
    }

    @GetMapping("/page")
    @Operation(summary = "获取NVR分页列表")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<cn.iocoder.yudao.framework.common.pojo.PageResult<NvrRespVO>> getNvrPage(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name) {
        
        // 获取所有 NVR
        List<IotDeviceDO> allList = nvrQueryService.getNvrList();
        
        // 过滤搜索条件
        List<IotDeviceDO> filteredList = allList;
        if (StringUtils.isNotBlank(name)) {
            filteredList = allList.stream()
                    .filter(d -> {
                        String deviceName = StringUtils.defaultIfBlank(d.getDeviceName(), d.getNickname());
                        String deviceIp = DeviceConfigHelper.getIpAddress(d);
                        return deviceName.contains(name) || 
                               (deviceIp != null && deviceIp.contains(name));
                    })
                    .collect(Collectors.toList());
        }
        
        // 手动分页
        int total = filteredList.size();
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<NvrRespVO> result;
        if (fromIndex >= total) {
            result = java.util.Collections.emptyList();
        } else {
            result = filteredList.subList(fromIndex, toIndex).stream().map(d -> {
                NvrRespVO vo = new NvrRespVO();
                vo.setId(d.getId());
                vo.setName(StringUtils.defaultIfBlank(d.getDeviceName(), d.getNickname()));
                // 从 config 中提取 IP 地址
                vo.setIpAddress(DeviceConfigHelper.getIpAddress(d));
                vo.setState(d.getState());
                return vo;
            }).collect(Collectors.toList());
        }
        
        return success(new cn.iocoder.yudao.framework.common.pojo.PageResult<>(result, (long) total));
    }

    @GetMapping("/{id}/channels")
    @Operation(summary = "获取NVR通道列表", description = "优先从数据库获取，如果没有则通过SDK获取并保存。refresh=1时强制从SDK刷新")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<List<NvrChannelRespVO>> getNvrChannels(
            @PathVariable("id") Long nvrId,
            @RequestParam(value = "refresh", required = false, defaultValue = "0") Integer refresh) {
        
        log.info("[NVR通道] 获取通道列表: nvrId={}, refresh={}", nvrId, refresh);
        
        // 1. 查询数据库中的通道
        List<IotDeviceChannelDO> dbChannels = channelService.getChannelsByDeviceId(nvrId);
        
        // 2. 判断是否需要同步
        boolean needSync = dbChannels.isEmpty() || (refresh != null && refresh == 1);
        
        if (needSync) {
            if (dbChannels.isEmpty()) {
                log.info("[NVR通道] 数据库无通道记录，首次同步: nvrId={}", nvrId);
            } else {
                log.info("[NVR通道] 强制刷新通道: nvrId={}, 原有通道数={}", nvrId, dbChannels.size());
            }
            
            // 调用同步方法，会自动保存到数据库
            Integer syncCount = channelService.syncDeviceChannels(nvrId);
            log.info("[NVR通道] 同步完成: nvrId={}, syncCount={}", nvrId, syncCount);
            
            // 重新查询
            dbChannels = channelService.getChannelsByDeviceId(nvrId);
        } else {
            log.info("[NVR通道] 从数据库获取通道: nvrId={}, count={}", nvrId, dbChannels.size());
        }

        // 3. 获取NVR设备信息（用于生成流地址）
        IotDeviceDO nvr = nvrQueryService.getNvrList().stream()
                .filter(d -> d.getId().equals(nvrId))
                .findFirst()
                .orElse(null);
        
        if (nvr == null) {
            log.warn("[NVR通道] NVR设备不存在: nvrId={}", nvrId);
            return success(java.util.Collections.emptyList());
        }
        
        // 4. 解析NVR配置
        String nvrCfg = nvr.getConfig() != null ? JSONUtil.toJsonStr(nvr.getConfig().toMap()) : null;
        // 从 config 中提取 IP 地址
        String nvrIp = DeviceConfigHelper.getIpAddress(nvr);
        String baseUser = extractStringFromConfig(nvrCfg, "username");
        String basePass = extractStringFromConfig(nvrCfg, "password");
        Integer baseRtspPort = extractIntFromConfig(nvrCfg, "rtspPort");
        Integer baseHttpPort = extractIntFromConfig(nvrCfg, "httpPort");
        
        // 设置默认值
        final String finalUser = StringUtils.defaultIfBlank(baseUser, "admin");
        final String finalPass = StringUtils.defaultIfBlank(basePass, "admin123");
        final int finalRtspPort = baseRtspPort != null ? baseRtspPort : 554;
        final int finalHttpPort = baseHttpPort != null ? baseHttpPort : 80;
        final String finalNvrIp = nvrIp;
        
        // 5. 转换为VO（基于通道表）
        List<NvrChannelRespVO> result = dbChannels.stream().map(ch -> {
            NvrChannelRespVO vo = new NvrChannelRespVO();
            
            // 基础信息（来自通道表）
            vo.setId(ch.getId());
            vo.setDeviceId(ch.getDeviceId());
            vo.setName(ch.getChannelName());
            vo.setState(ch.getOnlineStatus());
            vo.setChannelNo(ch.getChannelNo());
            
            // 扩展字段
            vo.setDeviceType(ch.getDeviceType());
            vo.setProtocol("RTSP");
            vo.setManufacturer("Dahua");
            
            // 云台支持（从通道表的ptz_support字段获取）
            Boolean ptzSupport = Boolean.TRUE.equals(ch.getPtzSupport());
            vo.setPtzSupport(ptzSupport);
            log.debug("[NVR通道] 通道{}云台支持: dbValue={}, result={}", 
                     ch.getChannelNo(), ch.getPtzSupport(), ptzSupport);
            
            // 生成视频流地址
            // 策略：统一通过NVR访问（更稳定，NVR会处理转发）
            // 使用NVR的IP + NVR的通道号
            String streamIp = finalNvrIp;  // 使用NVR的IP
            Integer streamChannelNo = ch.getChannelNo();  // 使用NVR的通道号
            
            // 设置显示用的IP（用于前端显示，显示实际IPC的IP）
            String displayIp = StringUtils.isNotBlank(ch.getTargetIp()) ? ch.getTargetIp() : finalNvrIp;
            vo.setIpAddress(displayIp);
            
            // 生成视频流地址（通过NVR访问）
            vo.setStreamUrl(generateStreamUrl(streamIp, streamChannelNo, "main", finalUser, finalPass, finalRtspPort));
            vo.setSubStreamUrl(generateStreamUrl(streamIp, streamChannelNo, "sub", finalUser, finalPass, finalRtspPort));
            vo.setSnapshotUrl(generateSnapshotUrl(streamIp, streamChannelNo, finalUser, finalPass, finalHttpPort));
            
            return vo;
        }).collect(Collectors.toList());
        
        log.info("[NVR通道] 返回通道列表: nvrId={}, count={}", nvrId, result.size());
        return success(result);
    }

    @PostMapping("/{id}/ptz/move")
    @Operation(summary = "NVR通道云台移动")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<String> ptzMove(@PathVariable("id") Long nvrId, @RequestBody PtzMoveReq req) {
        // 通过消息总线发送 PTZ 控制命令
        // Requirements: 6.1
        String requestId = nvrCommandService.ptzMove(
                nvrId,
                req.getChannelNo(),
                req.getPan(),
                req.getTilt(),
                req.getZoom(),
                req.getTimeoutMs()
        );
        
        // 返回 requestId，前端可以通过 WebSocket 接收命令执行结果
        return success(requestId);
    }

    @GetMapping("/{id}/snapshot")
    @Operation(summary = "获取NVR通道截图（异步）", description = "通过消息总线发送截图命令，结果通过WebSocket推送")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<String> getSnapshotAsync(
            @PathVariable("id") Long nvrId,
            @RequestParam("channel") Integer channelNo) {
        
        log.info("[NVR截图] 发送截图命令: nvrId={}, channelNo={}", nvrId, channelNo);
        
        // 通过消息总线发送截图命令
        // Requirements: 6.2
        String requestId = nvrCommandService.captureSnapshot(nvrId, channelNo);
        
        log.info("[NVR截图] 截图命令已发送: nvrId={}, channelNo={}, requestId={}", 
                nvrId, channelNo, requestId);
        
        // 返回 requestId，前端通过 WebSocket 接收截图结果（Base64 编码的图片数据）
        return success(requestId);
    }

    @GetMapping("/{id}/snapshot/proxy")
    @Operation(summary = "获取NVR通道截图（代理）", description = "直接通过NVR HTTP接口获取截图，解决前端CORS跨域问题")
    @PermitAll  // 允许匿名访问，因为img标签无法携带token
    @TenantIgnore  // 忽略租户拦截，因为img标签无法携带租户信息
    public void getSnapshotProxy(
            @PathVariable("id") Long nvrId,
            @RequestParam("channel") Integer channelNo,
            HttpServletResponse response) {
        
        log.info("========================================");
        log.info("[NVR截图代理] ✅✅✅ Controller方法被调用！nvrId={}, channelNo={}", nvrId, channelNo);
        log.info("========================================");
        
        try {
            // 1. 获取NVR设备信息
            IotDeviceDO nvr = nvrQueryService.getNvrList().stream()
                    .filter(d -> d.getId().equals(nvrId))
                    .findFirst()
                    .orElse(null);
            
            if (nvr == null) {
                log.warn("[NVR截图代理] NVR设备不存在: nvrId={}", nvrId);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "NVR not found");
                return;
            }
            
            // 2. 解析NVR配置
            String nvrCfg = nvr.getConfig() != null ? JSONUtil.toJsonStr(nvr.getConfig().toMap()) : null;
            // 从 config 中提取 IP 地址
            String nvrIp = DeviceConfigHelper.getIpAddress(nvr);
            
            String username = extractStringFromConfig(nvrCfg, "username");
            String password = extractStringFromConfig(nvrCfg, "password");
            Integer httpPort = extractIntFromConfig(nvrCfg, "httpPort");
            
            // 设置默认值
            if (StringUtils.isBlank(username)) username = "admin";
            if (StringUtils.isBlank(password)) password = "admin123";
            if (httpPort == null) httpPort = 80;
            
            // 验证IP地址
            if (StringUtils.isBlank(nvrIp)) {
                log.error("[NVR截图代理] NVR IP地址为空: nvrId={}", nvrId);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "NVR IP address is empty");
                return;
            }
            
            log.info("[NVR截图代理] 📍 NVR IP: {}, HTTP端口: {}", nvrIp, httpPort);
            
            // 3. 直接通过NVR HTTP接口获取截图
            int channel = channelNo + 1; // 大华通道从1开始
            String snapshotUrl = String.format("http://%s:%d/cgi-bin/snapshot.cgi?channel=%d", 
                    nvrIp, httpPort, channel);
            
            URL url = new URL(snapshotUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            
            // 设置 Basic Auth
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 4. 返回图片数据
                response.setContentType("image/jpeg");
                response.setHeader("Cache-Control", "no-cache");
                
                try (InputStream in = conn.getInputStream();
                     OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                }
                
                log.info("[NVR截图代理] ✅ 成功获取截图: nvrId={}, channelNo={}", nvrId, channelNo);
            } else {
                log.error("[NVR截图代理] 获取截图失败: nvrId={}, channelNo={}, responseCode={}", 
                        nvrId, channelNo, responseCode);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Failed to get snapshot: HTTP " + responseCode);
            }
            
        } catch (Exception e) {
            log.error("[NVR截图代理] 获取失败: nvrId={}, channelNo={}", nvrId, channelNo, e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                 "Failed to get snapshot: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    @PostMapping("/{id}/ptz/stop")
    @Operation(summary = "NVR通道云台停止")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<String> ptzStop(@PathVariable("id") Long nvrId, @RequestBody PtzStopReq req) {
        // 通过消息总线发送 PTZ 停止命令
        // Requirements: 6.1
        String requestId = nvrCommandService.ptzStop(
                nvrId,
                req.getChannelNo(),
                Boolean.TRUE.equals(req.getPanTilt()),
                Boolean.TRUE.equals(req.getZoom())
        );
        
        // 返回 requestId，前端可以通过 WebSocket 接收命令执行结果
        return success(requestId);
    }

    @PostMapping("/{id}/ptz/control")
    @Operation(summary = "NVR通道云台控制（命令模式）", description = "支持 UP/DOWN/LEFT/RIGHT/ZOOM_IN/ZOOM_OUT 等直接命令")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<String> ptzControl(@PathVariable("id") Long nvrId, @RequestBody PtzControlReq req) {
        // 查找通道信息，获取 target_channel_no、target_ip 和认证信息
        Integer targetChannelNo = req.getChannelNo();
        String targetIp = null;
        String targetUsername = "admin";      // 默认用户名
        String targetPassword = "admin123";   // 默认密码
        
        IotDeviceChannelDO channel = channelService.getChannelByDeviceIdAndChannelNo(nvrId, req.getChannelNo());
        if (channel != null) {
            if (channel.getTargetChannelNo() != null) {
                targetChannelNo = channel.getTargetChannelNo();
            }
            targetIp = channel.getTargetIp();
            // 从通道表读取用户名密码，如果没有则使用默认值
            if (StringUtils.isNotBlank(channel.getUsername())) {
                targetUsername = channel.getUsername();
            }
            if (StringUtils.isNotBlank(channel.getPassword())) {
                targetPassword = channel.getPassword();
            }
            log.info("[PTZ控制] 映射通道: nvrChannelNo={} -> targetChannelNo={}, targetIp={}, username={}", 
                    req.getChannelNo(), targetChannelNo, targetIp, targetUsername);
        }
        
        log.info("[PTZ控制] nvrId={}, nvrChannelNo={}, targetChannelNo={}, targetIp={}, command={}, stop={}, speed={}",
                nvrId, req.getChannelNo(), targetChannelNo, targetIp, req.getCommand(), req.getStop(), req.getSpeed());
        
        // 如果有 targetIp（远程IPC），则直接连接该设备进行PTZ控制
        String requestId = nvrCommandService.ptzControl(
                nvrId,
                targetChannelNo,
                req.getCommand(),
                req.getSpeed() != null ? req.getSpeed() : 4,
                Boolean.TRUE.equals(req.getStop()),
                targetIp,           // 传递目标IP，让gateway直接连接
                targetUsername,     // 从通道表读取的用户名
                targetPassword      // 从通道表读取的密码
        );
        
        return success(requestId);
    }

    @PostMapping("/{id}/ptz/preset")
    @Operation(summary = "NVR通道预设点控制", description = "支持转到预设点(GOTO)、设置预设点(SET)、删除预设点(CLEAR)")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<String> presetControl(@PathVariable("id") Long nvrId, @RequestBody PresetControlReq req) {
        // 查找通道信息，获取 target_channel_no、target_ip 和认证信息
        Integer targetChannelNo = req.getChannelNo();
        String targetIp = null;
        String targetUsername = "admin";      // 默认用户名
        String targetPassword = "admin123";   // 默认密码
        
        IotDeviceChannelDO channel = channelService.getChannelByDeviceIdAndChannelNo(nvrId, req.getChannelNo());
        if (channel != null) {
            if (channel.getTargetChannelNo() != null) {
                targetChannelNo = channel.getTargetChannelNo();
            }
            targetIp = channel.getTargetIp();
            // 从通道表读取用户名密码，如果没有则使用默认值
            if (StringUtils.isNotBlank(channel.getUsername())) {
                targetUsername = channel.getUsername();
            }
            if (StringUtils.isNotBlank(channel.getPassword())) {
                targetPassword = channel.getPassword();
            }
            log.info("[预设点控制] 映射通道: nvrChannelNo={} -> targetChannelNo={}, targetIp={}, username={}", 
                    req.getChannelNo(), targetChannelNo, targetIp, targetUsername);
        }
        
        log.info("[预设点控制] nvrId={}, nvrChannelNo={}, targetChannelNo={}, targetIp={}, presetNo={}, action={}, name={}",
                nvrId, req.getChannelNo(), targetChannelNo, targetIp, req.getPresetNo(), req.getAction(), req.getPresetName());
        
        // 发送预设点控制命令
        String requestId = nvrCommandService.presetControl(
                nvrId,
                targetChannelNo,
                req.getPresetNo(),
                req.getAction(),
                targetIp,           // 传递目标IP，让gateway直接连接
                targetUsername,     // 从通道表读取的用户名
                targetPassword,     // 从通道表读取的密码
                req.getPresetName() // 预设点名称
        );
        
        return success(requestId);
    }

    @PostMapping("/{id}/ptz/area-zoom")
    @Operation(summary = "NVR通道区域放大（3D定位）", description = "在视频画面上框选区域进行快速定位放大")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<String> areaZoom(@PathVariable("id") Long nvrId, @RequestBody AreaZoomReq req) {
        // 查找通道信息，获取 target_channel_no、target_ip 和认证信息
        Integer targetChannelNo = req.getChannelNo();
        String targetIp = null;
        String targetUsername = "admin";      // 默认用户名
        String targetPassword = "admin123";   // 默认密码
        
        IotDeviceChannelDO channel = channelService.getChannelByDeviceIdAndChannelNo(nvrId, req.getChannelNo());
        if (channel != null) {
            if (channel.getTargetChannelNo() != null) {
                targetChannelNo = channel.getTargetChannelNo();
            }
            targetIp = channel.getTargetIp();
            // 从通道表读取用户名密码，如果没有则使用默认值
            if (StringUtils.isNotBlank(channel.getUsername())) {
                targetUsername = channel.getUsername();
            }
            if (StringUtils.isNotBlank(channel.getPassword())) {
                targetPassword = channel.getPassword();
            }
            log.info("[区域放大] 映射通道: nvrChannelNo={} -> targetChannelNo={}, targetIp={}, username={}", 
                    req.getChannelNo(), targetChannelNo, targetIp, targetUsername);
        }
        
        // 区域放大需要直连 IPC
        if (StringUtils.isBlank(targetIp)) {
            log.warn("[区域放大] 未配置 targetIp，无法执行区域放大: nvrId={}, channelNo={}", nvrId, req.getChannelNo());
            return CommonResult.error(500, "区域放大需要配置 IPC 的 IP 地址");
        }
        
        log.info("[区域放大] nvrId={}, targetChannelNo={}, targetIp={}, area=({},{}) -> ({},{})",
                nvrId, targetChannelNo, targetIp, req.getStartX(), req.getStartY(), req.getEndX(), req.getEndY());
        
        // 发送区域放大命令
        String requestId = nvrCommandService.areaZoom(
                nvrId,
                targetChannelNo,
                req.getStartX(),
                req.getStartY(),
                req.getEndX(),
                req.getEndY(),
                targetIp,
                targetUsername,
                targetPassword
        );
        
        return success(requestId);
    }

    @PostMapping("/{id}/ptz/3d-position")
    @Operation(summary = "NVR通道3D定位", description = "直接指定中心点和放大倍数进行3D定位")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<String> position3D(@PathVariable("id") Long nvrId, @RequestBody Position3DReq req) {
        // 查找通道信息，获取 target_channel_no、target_ip 和认证信息
        Integer targetChannelNo = req.getChannelNo();
        String targetIp = null;
        String targetUsername = "admin";      // 默认用户名
        String targetPassword = "admin123";   // 默认密码
        
        IotDeviceChannelDO channel = channelService.getChannelByDeviceIdAndChannelNo(nvrId, req.getChannelNo());
        if (channel != null) {
            if (channel.getTargetChannelNo() != null) {
                targetChannelNo = channel.getTargetChannelNo();
            }
            targetIp = channel.getTargetIp();
            // 从通道表读取用户名密码，如果没有则使用默认值
            if (StringUtils.isNotBlank(channel.getUsername())) {
                targetUsername = channel.getUsername();
            }
            if (StringUtils.isNotBlank(channel.getPassword())) {
                targetPassword = channel.getPassword();
            }
            log.info("[3D定位] 映射通道: nvrChannelNo={} -> targetChannelNo={}, targetIp={}, username={}", 
                    req.getChannelNo(), targetChannelNo, targetIp, targetUsername);
        }
        
        // 3D定位需要直连 IPC
        if (StringUtils.isBlank(targetIp)) {
            log.warn("[3D定位] 未配置 targetIp，无法执行3D定位: nvrId={}, channelNo={}", nvrId, req.getChannelNo());
            return CommonResult.error(500, "3D定位需要配置 IPC 的 IP 地址");
        }
        
        log.info("[3D定位] nvrId={}, targetChannelNo={}, targetIp={}, position=({},{},{})",
                nvrId, targetChannelNo, targetIp, req.getX(), req.getY(), req.getZoom());
        
        // 发送3D定位命令
        String requestId = nvrCommandService.position3D(
                nvrId,
                targetChannelNo,
                req.getX(),
                req.getY(),
                req.getZoom() != null ? req.getZoom() : 4,
                targetIp,
                targetUsername,
                targetPassword
        );
        
        return success(requestId);
    }

    private Integer extractChannelNo(String config) {
        if (StringUtils.isBlank(config)) return null;
        try {
            // quick parse without extra dependency
            Integer v = tryGetInt(config, "channel");
            if (v != null) return v;
            v = tryGetInt(config, "channelno");
            if (v != null) return v;
            v = tryGetInt(config, "channo");
            return v;
        } catch (Exception ignored) {}
        return null;
    }

    private Integer tryGetInt(String json, String key) {
        try {
            int idx = json.indexOf('"' + key + '"');
            if (idx < 0) return null;
            int colon = json.indexOf(':', idx);
            if (colon < 0) return null;
            int i = colon + 1;
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            int j = i;
            while (j < json.length() && (Character.isDigit(json.charAt(j)) || json.charAt(j) == '-')) j++;
            if (j > i) return Integer.parseInt(json.substring(i, j));
        } catch (Exception ignored) {}
        return null;
    }

    private Integer extractIntFromConfig(String config, String key) {
        if (StringUtils.isBlank(config)) return null;
        return tryGetInt(config, key);
    }

    private String extractStringFromConfig(String config, String key) {
        if (StringUtils.isBlank(config)) return null;
        try {
            int idx = config.indexOf('"' + key + '"');
            if (idx < 0) return null;
            int colon = config.indexOf(':', idx);
            if (colon < 0) return null;
            int i = colon + 1;
            while (i < config.length() && Character.isWhitespace(config.charAt(i))) i++;
            if (i >= config.length()) return null;
            
            if (config.charAt(i) == '"') {
                // 字符串值
                i++; // 跳过开始的引号
                int j = i;
                while (j < config.length() && config.charAt(j) != '"') j++;
                if (j > i) return config.substring(i, j);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Boolean extractBooleanFromConfig(String config, String key) {
        if (StringUtils.isBlank(config)) return null;
        try {
            int idx = config.indexOf('"' + key + '"');
            if (idx < 0) return null;
            int colon = config.indexOf(':', idx);
            if (colon < 0) return null;
            int i = colon + 1;
            while (i < config.length() && Character.isWhitespace(config.charAt(i))) i++;
            if (i >= config.length()) return null;
            if (config.startsWith("true", i)) return true;
            if (config.startsWith("false", i)) return false;
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * 生成视频流地址
     * @param nvrIp NVR IP地址
     * @param channelNo 通道号
     * @param streamType 流类型：main(主码流) 或 sub(子码流)
     * @return RTSP流地址
     */
    private String generateStreamUrl(String nvrIp, Integer channelNo, String streamType, String username, String password, Integer rtspPort) {
        if (StringUtils.isBlank(nvrIp) || channelNo == null) {
            return null;
        }
        
        // 大华NVR的RTSP流地址格式
        // 主码流：rtsp://admin:password@ip:554/cam/realmonitor?channel=1&subtype=0
        // 子码流：rtsp://admin:password@ip:554/cam/realmonitor?channel=1&subtype=1
        int subtype = "sub".equals(streamType) ? 1 : 0;
        int channel = channelNo + 1; // 大华通道从1开始
        int port = (rtspPort != null ? rtspPort : 554);
        String user = StringUtils.defaultIfBlank(username, "admin");
        String pass = StringUtils.defaultIfBlank(password, "admin123");
        return String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=%d&subtype=%d", 
                           user, pass, nvrIp, port, channel, subtype);
    }

    /**
     * 生成快照地址
     * @param nvrIp NVR IP地址
     * @param channelNo 通道号
     * @return HTTP快照地址
     */
    private String generateSnapshotUrl(String nvrIp, Integer channelNo, String username, String password, Integer httpPort) {
        if (StringUtils.isBlank(nvrIp) || channelNo == null) {
            return null;
        }
        
        // 大华NVR的快照地址格式
        // http://admin:password@ip/cgi-bin/snapshot.cgi?channel=1
        int channel = channelNo + 1; // 大华通道从1开始
        int port = (httpPort != null ? httpPort : 80);
        String user = StringUtils.defaultIfBlank(username, "admin");
        String pass = StringUtils.defaultIfBlank(password, "admin123");
        if (port == 80) {
            return String.format("http://%s:%s@%s/cgi-bin/snapshot.cgi?channel=%d", user, pass, nvrIp, channel);
        }
        return String.format("http://%s:%s@%s:%d/cgi-bin/snapshot.cgi?channel=%d", user, pass, nvrIp, port, channel);
    }

    public static class PtzMoveReq {
        private Integer channelNo;
        private Double pan;
        private Double tilt;
        private Double zoom;
        private Integer timeoutMs;
        public Integer getChannelNo() { return channelNo; }
        public void setChannelNo(Integer channelNo) { this.channelNo = channelNo; }
        public Double getPan() { return pan == null ? 0.0 : pan; }
        public void setPan(Double pan) { this.pan = pan; }
        public Double getTilt() { return tilt == null ? 0.0 : tilt; }
        public void setTilt(Double tilt) { this.tilt = tilt; }
        public Double getZoom() { return zoom == null ? 0.0 : zoom; }
        public void setZoom(Double zoom) { this.zoom = zoom; }
        public Integer getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }
    }

    public static class PtzStopReq {
        private Integer channelNo;
        private Boolean panTilt;
        private Boolean zoom;
        public Integer getChannelNo() { return channelNo; }
        public void setChannelNo(Integer channelNo) { this.channelNo = channelNo; }
        public Boolean getPanTilt() { return panTilt; }
        public void setPanTilt(Boolean panTilt) { this.panTilt = panTilt; }
        public Boolean getZoom() { return zoom; }
        public void setZoom(Boolean zoom) { this.zoom = zoom; }
    }

    /**
     * PTZ 控制请求（命令模式）
     * 支持的命令：UP, DOWN, LEFT, RIGHT, LEFT_UP, RIGHT_UP, LEFT_DOWN, RIGHT_DOWN,
     *           ZOOM_IN, ZOOM_OUT, FOCUS_NEAR, FOCUS_FAR, IRIS_OPEN, IRIS_CLOSE, AUTO_FOCUS
     */
    public static class PtzControlReq {
        private Integer channelNo;
        private String command;  // PTZ 命令：UP, DOWN, LEFT, RIGHT, ZOOM_IN 等
        private Integer speed;   // 速度 1-8，默认 4
        private Boolean stop;    // true=停止，false=开始
        
        public Integer getChannelNo() { return channelNo; }
        public void setChannelNo(Integer channelNo) { this.channelNo = channelNo; }
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        public Integer getSpeed() { return speed; }
        public void setSpeed(Integer speed) { this.speed = speed; }
        public Boolean getStop() { return stop; }
        public void setStop(Boolean stop) { this.stop = stop; }
    }

    /**
     * 预设点控制请求
     * 支持的操作：GOTO（转到预设点）、SET（设置预设点）、CLEAR（删除预设点）
     */
    public static class PresetControlReq {
        private Integer channelNo;   // 通道号
        private Integer presetNo;    // 预设点编号（1-255）
        private String action;       // 操作：GOTO, SET, CLEAR
        private String presetName;   // 预设点名称（SET操作时使用）
        
        public Integer getChannelNo() { return channelNo; }
        public void setChannelNo(Integer channelNo) { this.channelNo = channelNo; }
        public Integer getPresetNo() { return presetNo; }
        public void setPresetNo(Integer presetNo) { this.presetNo = presetNo; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getPresetName() { return presetName; }
        public void setPresetName(String presetName) { this.presetName = presetName; }
    }

    /**
     * 区域放大请求（3D定位 - 框选模式）
     * 坐标为归一化坐标（0-8192），(0,0) 为画面左上角，(8192,8192) 为画面右下角
     */
    public static class AreaZoomReq {
        private Integer channelNo;   // 通道号
        private Integer startX;      // 框选起始点 X
        private Integer startY;      // 框选起始点 Y
        private Integer endX;        // 框选结束点 X
        private Integer endY;        // 框选结束点 Y
        
        public Integer getChannelNo() { return channelNo; }
        public void setChannelNo(Integer channelNo) { this.channelNo = channelNo; }
        public Integer getStartX() { return startX; }
        public void setStartX(Integer startX) { this.startX = startX; }
        public Integer getStartY() { return startY; }
        public void setStartY(Integer startY) { this.startY = startY; }
        public Integer getEndX() { return endX; }
        public void setEndX(Integer endX) { this.endX = endX; }
        public Integer getEndY() { return endY; }
        public void setEndY(Integer endY) { this.endY = endY; }
    }

    /**
     * 3D定位请求（直接指定中心点和放大倍数）
     * 坐标为归一化坐标（0-8192），(0,0) 为画面左上角，(8192,8192) 为画面右下角
     */
    public static class Position3DReq {
        private Integer channelNo;   // 通道号
        private Integer x;           // 中心点 X
        private Integer y;           // 中心点 Y
        private Integer zoom;        // 放大倍数（1-128）
        
        public Integer getChannelNo() { return channelNo; }
        public void setChannelNo(Integer channelNo) { this.channelNo = channelNo; }
        public Integer getX() { return x; }
        public void setX(Integer x) { this.x = x; }
        public Integer getY() { return y; }
        public void setY(Integer y) { this.y = y; }
        public Integer getZoom() { return zoom; }
        public void setZoom(Integer zoom) { this.zoom = zoom; }
    }

    /**
     * 将数据库通道转换为设备对象（用于兼容现有逻辑）
     */
    private List<IotDeviceDO> convertChannelsToDevices(List<IotDeviceChannelDO> channels) {
        return channels.stream().map(ch -> {
            IotDeviceDO device = new IotDeviceDO();
            device.setId(ch.getId());
            device.setDeviceName(ch.getChannelName());
            device.setNickname(ch.getChannelName());
            device.setState(ch.getOnlineStatus());
            
            // 构建config - 使用 AccessDeviceConfig 作为通用配置，包含 IP 地址
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("channel", ch.getChannelNo() != null ? ch.getChannelNo() : 0);
            if (ch.getTargetIp() != null) {
                configMap.put("ipAddress", ch.getTargetIp());
            }
            AccessDeviceConfig config = new AccessDeviceConfig();
            config.fromMap(configMap);
            device.setConfig(config);
            
            return device;
        }).collect(Collectors.toList());
    }
    
    /**
     * 从 config JSON 中提取 IP 地址
     * 
     * @param config JSON 配置字符串
     * @return IP 地址，如果提取失败返回 null
     */
    private String extractIpFromConfig(String config) {
        try {
            if (StrUtil.isBlank(config)) {
                return null;
            }
            
            JSONObject configJson = JSONUtil.parseObj(config);
            return configJson.getStr("ip");
        } catch (Exception e) {
            log.trace("[NVR] 解析 config JSON 失败: {}", e.getMessage());
            return null;
        }
    }
}
