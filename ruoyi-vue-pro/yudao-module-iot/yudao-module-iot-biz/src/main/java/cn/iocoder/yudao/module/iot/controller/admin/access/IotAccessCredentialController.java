package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.credential.IotAccessCredentialVerifyReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.credential.IotAccessCredentialVerifyRespVO;
import cn.iocoder.yudao.module.iot.service.access.IotAccessCredentialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁凭证验证 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 门禁凭证验证")
@RestController
@RequestMapping("/iot/access/credential")
@Validated
public class IotAccessCredentialController {

    @Resource
    private IotAccessCredentialService credentialService;

    @PostMapping("/verify-and-open")
    @Operation(summary = "凭证验证开门", description = "验证凭证有效性、检查人员权限、调用SDK开门、记录事件")
    @PreAuthorize("@ss.hasPermission('iot:access-credential:verify')")
    public CommonResult<IotAccessCredentialVerifyRespVO> verifyAndOpen(@Valid @RequestBody IotAccessCredentialVerifyReqVO reqVO) {
        return success(credentialService.verifyAndOpen(reqVO));
    }

}
