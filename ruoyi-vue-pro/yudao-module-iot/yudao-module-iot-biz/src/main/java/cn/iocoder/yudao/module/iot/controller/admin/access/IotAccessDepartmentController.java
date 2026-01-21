package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.department.IotAccessDepartmentCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.department.IotAccessDepartmentRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.department.IotAccessDepartmentUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessDepartmentDO;
import cn.iocoder.yudao.module.iot.service.access.IotAccessDepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁组织架构 Controller
 */
@Tag(name = "管理后台 - 门禁组织架构")
@RestController
@RequestMapping("/iot/access/department")
@Validated
public class IotAccessDepartmentController {

    @Resource
    private IotAccessDepartmentService departmentService;

    @PostMapping("/create")
    @Operation(summary = "创建部门")
    @PreAuthorize("@ss.hasPermission('iot:access-department:create')")
    public CommonResult<Long> createDepartment(@Valid @RequestBody IotAccessDepartmentCreateReqVO createReqVO) {
        IotAccessDepartmentDO department = convertToDO(createReqVO);
        return success(departmentService.createDepartment(department));
    }

    @PutMapping("/update")
    @Operation(summary = "更新部门")
    @PreAuthorize("@ss.hasPermission('iot:access-department:update')")
    public CommonResult<Boolean> updateDepartment(@Valid @RequestBody IotAccessDepartmentUpdateReqVO updateReqVO) {
        IotAccessDepartmentDO department = convertToDO(updateReqVO);
        department.setId(updateReqVO.getId());
        departmentService.updateDepartment(department);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除部门")
    @Parameter(name = "id", description = "部门ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-department:delete')")
    public CommonResult<Boolean> deleteDepartment(@RequestParam("id") Long id) {
        departmentService.deleteDepartment(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取部门详情")
    @Parameter(name = "id", description = "部门ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-department:query')")
    public CommonResult<IotAccessDepartmentRespVO> getDepartment(@RequestParam("id") Long id) {
        IotAccessDepartmentDO department = departmentService.getDepartment(id);
        return success(convertToVO(department));
    }

    @GetMapping("/list")
    @Operation(summary = "获取部门列表")
    @PreAuthorize("@ss.hasPermission('iot:access-department:query')")
    public CommonResult<List<IotAccessDepartmentRespVO>> getDepartmentList(
            @RequestParam(value = "deptName", required = false) String deptName,
            @RequestParam(value = "status", required = false) Integer status) {
        List<IotAccessDepartmentDO> list = departmentService.getDepartmentList(deptName, status);
        return success(convertToVOList(list));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取组织树")
    @PreAuthorize("@ss.hasPermission('iot:access-department:query')")
    public CommonResult<List<IotAccessDepartmentRespVO>> getDepartmentTree() {
        List<IotAccessDepartmentDO> tree = departmentService.getDepartmentTree();
        return success(convertToTreeVO(tree));
    }

    // ========== 转换方法 ==========

    private IotAccessDepartmentDO convertToDO(IotAccessDepartmentCreateReqVO vo) {
        IotAccessDepartmentDO department = new IotAccessDepartmentDO();
        department.setParentId(vo.getParentId() != null ? vo.getParentId() : 0L);
        department.setDeptName(vo.getDeptName());
        department.setDeptCode(vo.getDeptCode());
        department.setSort(vo.getSort() != null ? vo.getSort() : 0);
        department.setLeader(vo.getLeader());
        department.setPhone(vo.getPhone());
        department.setStatus(vo.getStatus() != null ? vo.getStatus() : 0);
        return department;
    }

    private IotAccessDepartmentRespVO convertToVO(IotAccessDepartmentDO department) {
        if (department == null) {
            return null;
        }
        IotAccessDepartmentRespVO vo = new IotAccessDepartmentRespVO();
        vo.setId(department.getId());
        vo.setParentId(department.getParentId());
        vo.setDeptName(department.getDeptName());
        vo.setDeptCode(department.getDeptCode());
        vo.setSort(department.getSort());
        vo.setLeader(department.getLeader());
        vo.setPhone(department.getPhone());
        vo.setStatus(department.getStatus());
        vo.setCreateTime(department.getCreateTime());
        return vo;
    }

    private List<IotAccessDepartmentRespVO> convertToVOList(List<IotAccessDepartmentDO> list) {
        List<IotAccessDepartmentRespVO> result = new ArrayList<>();
        if (list != null) {
            for (IotAccessDepartmentDO department : list) {
                result.add(convertToVO(department));
            }
        }
        return result;
    }

    private List<IotAccessDepartmentRespVO> convertToTreeVO(List<IotAccessDepartmentDO> tree) {
        List<IotAccessDepartmentRespVO> result = new ArrayList<>();
        if (tree != null) {
            for (IotAccessDepartmentDO department : tree) {
                IotAccessDepartmentRespVO vo = convertToVO(department);
                if (department.getChildren() != null && !department.getChildren().isEmpty()) {
                    vo.setChildren(convertToTreeVO(department.getChildren()));
                }
                result.add(vo);
            }
        }
        return result;
    }

}
