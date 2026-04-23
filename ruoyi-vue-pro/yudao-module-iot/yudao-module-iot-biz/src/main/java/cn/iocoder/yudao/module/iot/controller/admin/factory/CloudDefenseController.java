package cn.iocoder.yudao.module.iot.controller.admin.factory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.CloudDefenseOverviewRespVO;
import cn.iocoder.yudao.module.iot.service.factory.CloudDefenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 立体化云防")
@RestController
@RequestMapping("/iot/cloud-defense")
@Validated
public class CloudDefenseController {

    @Resource
    private CloudDefenseService cloudDefenseService;

    @GetMapping("/overview")
    @Operation(summary = "获取立体化云防总览数据")
    @PermitAll
    public CommonResult<CloudDefenseOverviewRespVO> getOverview() {
        return success(cloudDefenseService.getOverview());
    }
}
