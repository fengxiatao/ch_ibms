package cn.iocoder.yudao.module.iot.controller.admin.epatrol;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPersonDO;
import cn.iocoder.yudao.module.iot.service.epatrol.EpatrolPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 电子巡更人员
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 电子巡更人员")
@RestController
@RequestMapping("/iot/epatrol/person")
@Validated
public class EpatrolPersonController {

    @Resource
    private EpatrolPersonService personService;

    @PostMapping("/create")
    @Operation(summary = "创建巡更人员")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-person:create')")
    public CommonResult<Long> createPerson(@Valid @RequestBody EpatrolPersonSaveReqVO createReqVO) {
        return success(personService.createPerson(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新巡更人员")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-person:update')")
    public CommonResult<Boolean> updatePerson(@Valid @RequestBody EpatrolPersonSaveReqVO updateReqVO) {
        personService.updatePerson(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除巡更人员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:epatrol-person:delete')")
    public CommonResult<Boolean> deletePerson(@RequestParam("id") Long id) {
        personService.deletePerson(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得巡更人员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:epatrol-person:query')")
    public CommonResult<EpatrolPersonRespVO> getPerson(@RequestParam("id") Long id) {
        EpatrolPersonDO person = personService.getPerson(id);
        return success(BeanUtils.toBean(person, EpatrolPersonRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得巡更人员分页")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-person:query')")
    public CommonResult<PageResult<EpatrolPersonRespVO>> getPersonPage(@Valid EpatrolPersonPageReqVO pageReqVO) {
        PageResult<EpatrolPersonDO> pageResult = personService.getPersonPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EpatrolPersonRespVO.class));
    }

    @GetMapping("/list-all-enabled")
    @Operation(summary = "获得所有启用的巡更人员")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-person:query')")
    public CommonResult<List<EpatrolPersonRespVO>> getEnabledPersonList() {
        List<EpatrolPersonDO> list = personService.getEnabledPersonList();
        return success(BeanUtils.toBean(list, EpatrolPersonRespVO.class));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新巡更人员状态")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-person:update')")
    public CommonResult<Boolean> updatePersonStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        personService.updatePersonStatus(id, status);
        return success(true);
    }

}
