package cn.iocoder.yudao.module.iot.controller.admin.visitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.IotVisitorReasonRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorReasonDO;
import cn.iocoder.yudao.module.iot.service.visitor.IotVisitorReasonService;
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
 * 访客来访事由 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 访客来访事由")
@RestController
@RequestMapping("/iot/visitor/reason")
@Validated
public class IotVisitorReasonController {

    @Resource
    private IotVisitorReasonService visitorReasonService;

    @GetMapping("/list")
    @Operation(summary = "获取来访事由列表")
    @PreAuthorize("@ss.hasPermission('iot:visitor-apply:query')")
    public CommonResult<List<IotVisitorReasonRespVO>> getReasonList() {
        List<IotVisitorReasonDO> list = visitorReasonService.getReasonList();
        return success(BeanUtils.toBean(list, IotVisitorReasonRespVO.class));
    }

}
