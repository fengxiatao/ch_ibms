package cn.iocoder.yudao.module.iot.newgateway.remote;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.newgateway.core.config.GatewayPluginProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * IoT 设备 API 实现类（远程调用版本）
 * <p>
 * 通过 HTTP 调用 biz 模块的 RPC 接口获取设备信息。
 * </p>
 *
 * @author IoT Gateway Team
 */
@Service("iotDeviceCommonApiImpl")
@Slf4j
public class IotDeviceCommonApiImpl implements IotDeviceCommonApi {

    @Resource
    private GatewayPluginProperties gatewayProperties;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        GatewayPluginProperties.RpcProperties rpc = gatewayProperties.getRpc();
        if (rpc == null || rpc.getUrl() == null) {
            log.warn("[IotDeviceCommonApiImpl] RPC 配置未设置，将无法调用远程设备 API");
            return;
        }
        restTemplate = new RestTemplateBuilder()
                .rootUri(rpc.getUrl() + "/rpc-api/iot/device")
                .readTimeout(rpc.getReadTimeout())
                .connectTimeout(rpc.getConnectTimeout())
                .build();
        log.info("[IotDeviceCommonApiImpl] 初始化完成，RPC URL: {}", rpc.getUrl());
    }

    @Override
    public CommonResult<Boolean> authDevice(IotDeviceAuthReqDTO authReqDTO) {
        return doPost("/auth", authReqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<IotDeviceRespDTO> getDevice(IotDeviceGetReqDTO getReqDTO) {
        return doPost("/get", getReqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<List<IotDeviceRespDTO>> getOnlineDevices() {
        return doGet("/online-devices", new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<List<IotDeviceRespDTO>> getAccessDevices() {
        return doGet("/access-devices", new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<List<IotDeviceRespDTO>> getAllDevices() {
        return doGet("/all-devices", new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<List<IotDeviceRespDTO>> getDevicesByTenantId(Long tenantId) {
        if (tenantId == null) {
            log.warn("[getDevicesByTenantId] tenantId 不能为空");
            return CommonResult.success(List.of());
        }
        return doGet("/devices-by-tenant?tenantId=" + tenantId, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<Integer> batchUpdateDeviceState(List<Long> deviceIds, Integer state, String reason) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            log.warn("[batchUpdateDeviceState] deviceIds 不能为空");
            return CommonResult.success(0);
        }
        
        // 构建查询参数
        StringBuilder url = new StringBuilder("/batch-update-state?state=");
        url.append(state);
        if (reason != null && !reason.isEmpty()) {
            url.append("&reason=").append(java.net.URLEncoder.encode(reason, java.nio.charset.StandardCharsets.UTF_8));
        }
        // 添加 deviceIds 参数
        for (Long deviceId : deviceIds) {
            url.append("&deviceIds=").append(deviceId);
        }
        
        return doPost(url.toString(), null, new ParameterizedTypeReference<>() { });
    }

    private <T, R> CommonResult<R> doPost(String url, T body,
                                          ParameterizedTypeReference<CommonResult<R>> responseType) {
        if (restTemplate == null) {
            log.error("[doPost][url({}) body({}) RestTemplate 未初始化]", url, body);
            return CommonResult.error(INTERNAL_SERVER_ERROR.getCode(), "RPC 未配置");
        }
        try {
            HttpEntity<T> requestEntity = new HttpEntity<>(body);
            ResponseEntity<CommonResult<R>> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, responseType);
            CommonResult<R> result = response.getBody();
            Assert.notNull(result, "请求结果不能为空");
            return result;
        } catch (Exception e) {
            log.error("[doPost][url({}) body({}) 发生异常]", url, body, e);
            return CommonResult.error(INTERNAL_SERVER_ERROR);
        }
    }

    private <R> CommonResult<R> doGet(String url,
                                      ParameterizedTypeReference<CommonResult<R>> responseType) {
        if (restTemplate == null) {
            log.error("[doGet][url({}) RestTemplate 未初始化]", url);
            return CommonResult.error(INTERNAL_SERVER_ERROR.getCode(), "RPC 未配置");
        }
        try {
            ResponseEntity<CommonResult<R>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, responseType);
            CommonResult<R> result = response.getBody();
            Assert.notNull(result, "请求结果不能为空");
            return result;
        } catch (Exception e) {
            log.error("[doGet][url({}) 发生异常]", url, e);
            return CommonResult.error(INTERNAL_SERVER_ERROR);
        }
    }
}
