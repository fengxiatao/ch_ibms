package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.person.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonCredentialMapper;
import cn.iocoder.yudao.module.iot.service.access.IotAccessDepartmentService;
import cn.iocoder.yudao.module.iot.service.access.IotAccessPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁人员管理 Controller
 */
@Tag(name = "管理后台 - 门禁人员管理")
@RestController
@RequestMapping("/iot/access/person")
@Validated
public class IotAccessPersonController {

    @Resource
    private IotAccessPersonService personService;

    @Resource
    private IotAccessDepartmentService departmentService;

    @Resource
    private IotAccessPersonCredentialMapper credentialMapper;

    // ========== 人员管理 ==========

    @PostMapping("/create")
    @Operation(summary = "创建人员")
    @PreAuthorize("@ss.hasPermission('iot:access-person:create')")
    public CommonResult<Long> createPerson(@Valid @RequestBody IotAccessPersonCreateReqVO createReqVO) {
        IotAccessPersonDO person = convertToDO(createReqVO);
        return success(personService.createPerson(person));
    }

    @PutMapping("/update")
    @Operation(summary = "更新人员")
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<Boolean> updatePerson(@Valid @RequestBody IotAccessPersonUpdateReqVO updateReqVO) {
        IotAccessPersonDO person = convertToDO(updateReqVO);
        person.setId(updateReqVO.getId());
        personService.updatePerson(person);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除人员")
    @Parameter(name = "id", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-person:delete')")
    public CommonResult<Boolean> deletePerson(@RequestParam("id") Long id) {
        personService.deletePerson(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取人员详情")
    @Parameter(name = "id", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-person:query')")
    public CommonResult<IotAccessPersonRespVO> getPerson(@RequestParam("id") Long id) {
        IotAccessPersonDO person = personService.getPerson(id);
        IotAccessPersonRespVO vo = convertToVO(person);
        if (vo != null) {
            // 填充凭证信息
            List<IotAccessPersonCredentialDO> credentials = personService.getPersonCredentials(id);
            vo.setCredentials(convertCredentials(credentials));
        }
        return success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "获取人员分页")
    @PreAuthorize("@ss.hasPermission('iot:access-person:query')")
    public CommonResult<PageResult<IotAccessPersonRespVO>> getPersonPage(@Valid IotAccessPersonPageReqVO pageReqVO) {
        PageResult<IotAccessPersonDO> pageResult = personService.getPersonPage(
                pageReqVO.getPersonCode(),
                pageReqVO.getPersonName(),
                pageReqVO.getPersonType(),
                pageReqVO.getDeptId(),
                pageReqVO.getStatus(),
                pageReqVO.getPageNo(),
                pageReqVO.getPageSize()
        );
        return success(convertPageResult(pageResult));
    }

    @PostMapping("/batch-create")
    @Operation(summary = "批量创建人员")
    @PreAuthorize("@ss.hasPermission('iot:access-person:create')")
    public CommonResult<IotAccessPersonBatchCreateRespVO> batchCreatePerson(
            @Valid @RequestBody IotAccessPersonBatchCreateReqVO reqVO) {
        return success(personService.batchCreatePerson(reqVO));
    }

    @PutMapping("/freeze")
    @Operation(summary = "冻结人员")
    @Parameter(name = "id", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<Boolean> freezePerson(@RequestParam("id") Long id) {
        personService.freezePerson(id);
        return success(true);
    }

    @PutMapping("/unfreeze")
    @Operation(summary = "解冻人员")
    @Parameter(name = "id", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<Boolean> unfreezePerson(@RequestParam("id") Long id) {
        personService.unfreezePerson(id);
        return success(true);
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入人员")
    @PreAuthorize("@ss.hasPermission('iot:access-person:import')")
    public CommonResult<Integer> importPersons(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", defaultValue = "false") Boolean updateSupport) {
        // TODO: 实现Excel解析逻辑
        return success(0);
    }

    @GetMapping("/export-template")
    @Operation(summary = "导出人员导入模板")
    @PreAuthorize("@ss.hasPermission('iot:access-person:export')")
    public void exportTemplate() {
        // TODO: 实现模板导出逻辑
    }

    // ========== 凭证管理 ==========

    @PostMapping("/set-password")
    @Operation(summary = "设置密码")
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<Boolean> setPassword(@Valid @RequestBody IotAccessPersonCredentialReqVO reqVO) {
        personService.setPassword(reqVO.getPersonId(), reqVO.getCredentialData());
        return success(true);
    }

    @PostMapping("/add-card")
    @Operation(summary = "添加卡片")
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<Boolean> addCard(@Valid @RequestBody IotAccessPersonCredentialReqVO reqVO) {
        personService.addCard(reqVO.getPersonId(), reqVO.getCredentialData());
        return success(true);
    }

    @PostMapping("/add-fingerprint")
    @Operation(summary = "录入指纹")
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<Boolean> addFingerprint(@Valid @RequestBody IotAccessPersonCredentialReqVO reqVO) {
        // 指纹数据需要从前端获取，这里简化处理
        byte[] fingerprintData = reqVO.getCredentialData() != null ? 
                reqVO.getCredentialData().getBytes() : new byte[0];
        personService.addFingerprint(reqVO.getPersonId(), fingerprintData, reqVO.getFingerIndex());
        return success(true);
    }

    @PostMapping("/add-face")
    @Operation(summary = "录入人脸")
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<String> addFace(
            @RequestParam("personId") Long personId,
            @RequestParam("file") MultipartFile file) {
        try {
            byte[] faceData = file.getBytes();
            personService.addFace(personId, faceData);
            // 返回更新后的人员信息中的照片URL
            IotAccessPersonDO person = personService.getPerson(personId);
            return success(person.getFaceUrl());
        } catch (Exception e) {
            return CommonResult.error(500, "人脸录入失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/remove-credential")
    @Operation(summary = "删除凭证")
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<Boolean> removeCredential(@Valid @RequestBody IotAccessPersonCredentialReqVO reqVO) {
        String credentialType = reqVO.getCredentialType();
        if ("card".equals(credentialType)) {
            personService.removeCard(reqVO.getPersonId(), reqVO.getCredentialData());
        } else if ("fingerprint".equals(credentialType)) {
            personService.removeFingerprint(reqVO.getPersonId(), reqVO.getFingerIndex());
        } else if ("face".equals(credentialType)) {
            personService.removeFace(reqVO.getPersonId());
        }
        return success(true);
    }

    // ========== 卡片操作（SmartPSS功能对齐） ==========

    @PutMapping("/card/lost")
    @Operation(summary = "卡片挂失")
    @Parameter(name = "credentialId", description = "凭证ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<Boolean> reportCardLost(@RequestParam("credentialId") Long credentialId) {
        personService.reportCardLost(credentialId);
        return success(true);
    }

    @PutMapping("/card/unlost")
    @Operation(summary = "卡片解挂")
    @Parameter(name = "credentialId", description = "凭证ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<Boolean> cancelCardLost(@RequestParam("credentialId") Long credentialId) {
        personService.cancelCardLost(credentialId);
        return success(true);
    }

    @PutMapping("/card/replace")
    @Operation(summary = "换卡")
    @PreAuthorize("@ss.hasPermission('iot:access-person:update')")
    public CommonResult<Boolean> replaceCard(
            @RequestParam("credentialId") Long credentialId,
            @RequestParam("newCardNo") String newCardNo) {
        personService.replaceCard(credentialId, newCardNo);
        return success(true);
    }

    @GetMapping("/credentials/{personId}")
    @Operation(summary = "获取人员凭证列表")
    @Parameter(name = "personId", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-person:query')")
    public CommonResult<List<IotAccessPersonCredentialRespVO>> getPersonCredentials(
            @PathVariable("personId") Long personId) {
        List<IotAccessPersonCredentialDO> credentials = personService.getCredentialsByPersonId(personId);
        return success(convertCredentialList(credentials));
    }

    // ========== 转换方法 ==========

    private IotAccessPersonDO convertToDO(IotAccessPersonCreateReqVO vo) {
        IotAccessPersonDO person = new IotAccessPersonDO();
        person.setPersonCode(vo.getPersonCode());
        person.setPersonName(vo.getPersonName());
        person.setPersonType(vo.getPersonType() != null ? vo.getPersonType() : 1);
        person.setDeptId(vo.getDeptId());
        person.setIdCard(vo.getIdCard());
        person.setPhone(vo.getPhone());
        person.setEmail(vo.getEmail());
        person.setFaceUrl(vo.getFaceUrl());
        person.setValidStart(vo.getValidStart());
        person.setValidEnd(vo.getValidEnd());
        person.setStatus(vo.getStatus() != null ? vo.getStatus() : 0);
        return person;
    }

    private IotAccessPersonRespVO convertToVO(IotAccessPersonDO person) {
        if (person == null) {
            return null;
        }
        IotAccessPersonRespVO vo = new IotAccessPersonRespVO();
        vo.setId(person.getId());
        vo.setPersonCode(person.getPersonCode());
        vo.setPersonName(person.getPersonName());
        vo.setPersonType(person.getPersonType());
        vo.setDeptId(person.getDeptId());
        vo.setIdCard(person.getIdCard());
        vo.setPhone(person.getPhone());
        vo.setEmail(person.getEmail());
        vo.setFaceUrl(person.getFaceUrl());
        vo.setValidStart(person.getValidStart());
        vo.setValidEnd(person.getValidEnd());
        vo.setStatus(person.getStatus());
        vo.setCreateTime(person.getCreateTime());
        // 填充部门名称
        if (person.getDeptId() != null) {
            var dept = departmentService.getDepartment(person.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        return vo;
    }

    private PageResult<IotAccessPersonRespVO> convertPageResult(PageResult<IotAccessPersonDO> pageResult) {
        List<IotAccessPersonRespVO> list = new ArrayList<>();
        if (pageResult.getList() != null && !pageResult.getList().isEmpty()) {
            // 批量获取指纹数量
            List<Long> personIds = pageResult.getList().stream()
                    .map(IotAccessPersonDO::getId)
                    .collect(Collectors.toList());
            Map<Long, Integer> fingerprintCountMap = getFingerprintCountMap(personIds);
            
            for (IotAccessPersonDO person : pageResult.getList()) {
                IotAccessPersonRespVO vo = convertToVO(person);
                // 设置指纹数量
                vo.setFingerprintCount(fingerprintCountMap.getOrDefault(person.getId(), 0));
                list.add(vo);
            }
        }
        return new PageResult<>(list, pageResult.getTotal());
    }

    /**
     * 批量获取人员的指纹数量
     */
    private Map<Long, Integer> getFingerprintCountMap(List<Long> personIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (personIds == null || personIds.isEmpty()) {
            return result;
        }
        List<Map<String, Object>> countList = credentialMapper.countFingerprintByPersonIds(personIds);
        if (countList != null) {
            for (Map<String, Object> item : countList) {
                Long personId = ((Number) item.get("person_id")).longValue();
                Integer count = ((Number) item.get("count")).intValue();
                result.put(personId, count);
            }
        }
        return result;
    }

    private List<IotAccessPersonRespVO.CredentialVO> convertCredentials(List<IotAccessPersonCredentialDO> credentials) {
        List<IotAccessPersonRespVO.CredentialVO> result = new ArrayList<>();
        if (credentials != null) {
            for (IotAccessPersonCredentialDO credential : credentials) {
                IotAccessPersonRespVO.CredentialVO vo = new IotAccessPersonRespVO.CredentialVO();
                vo.setId(credential.getId());
                vo.setCredentialType(credential.getCredentialType());
                vo.setCredentialData(credential.getCredentialData());
                vo.setDeviceSynced(credential.getDeviceSynced());
                result.add(vo);
            }
        }
        return result;
    }

    private List<IotAccessPersonCredentialRespVO> convertCredentialList(List<IotAccessPersonCredentialDO> credentials) {
        List<IotAccessPersonCredentialRespVO> result = new ArrayList<>();
        if (credentials != null) {
            for (IotAccessPersonCredentialDO credential : credentials) {
                IotAccessPersonCredentialRespVO vo = new IotAccessPersonCredentialRespVO();
                vo.setId(credential.getId());
                vo.setPersonId(credential.getPersonId());
                vo.setCredentialType(credential.getCredentialType());
                vo.setCredentialData(credential.getCredentialData());
                vo.setCardNo(credential.getCardNo());
                vo.setIssueTime(credential.getIssueTime());
                vo.setReplaceTime(credential.getReplaceTime());
                vo.setCardStatus(credential.getCardStatus());
                vo.setOldCardNo(credential.getOldCardNo());
                vo.setFingerIndex(credential.getFingerIndex());
                vo.setFingerName(credential.getFingerName());
                vo.setDeviceSynced(credential.getDeviceSynced());
                vo.setSyncTime(credential.getSyncTime());
                vo.setStatus(credential.getStatus());
                vo.setCreateTime(credential.getCreateTime());
                result.add(vo);
            }
        }
        return result;
    }

}
