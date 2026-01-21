package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessTimeTemplateDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessTimeTemplateMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁时间模板 Controller
 */
@Tag(name = "管理后台 - 门禁时间模板")
@RestController
@RequestMapping("/iot/access/time-template")
@Validated
public class IotAccessTimeTemplateController {

    @Resource
    private IotAccessTimeTemplateMapper timeTemplateMapper;

    @GetMapping("/list")
    @Operation(summary = "获取时间模板列表")
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:query')")
    public CommonResult<List<IotAccessTimeTemplateDO>> getTimeTemplateList() {
        // 获取所有启用的时间模板
        List<IotAccessTimeTemplateDO> list = timeTemplateMapper.selectListByStatus(0);
        return success(list);
    }

}
