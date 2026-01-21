package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.person.IotAccessPersonBatchCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.person.IotAccessPersonBatchCreateRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonCredentialMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonMapper;
import cn.iocoder.yudao.module.iot.enums.access.CredentialTypeConstants;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁人员管理 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotAccessPersonServiceImpl implements IotAccessPersonService {

    @Resource
    private IotAccessPersonMapper personMapper;

    @Resource
    private IotAccessPersonCredentialMapper credentialMapper;

    @Resource
    private IotAccessDepartmentService departmentService;

    @Resource
    private FileApi fileApi;

    /** 人员编号前缀 */
    private static final String PERSON_CODE_PREFIX = "YG";
    /** 人员编号流水号长度 */
    private static final int PERSON_CODE_SEQ_LENGTH = 6;

    // ========== 人员管理 ==========

    @Override
    public Long createPerson(IotAccessPersonDO person) {
        // 如果没有提供编号，则自动生成
        if (person.getPersonCode() == null || person.getPersonCode().isEmpty()) {
            person.setPersonCode(generatePersonCode());
        } else {
            // 校验人员编号唯一性
            if (!isPersonCodeUnique(person.getPersonCode(), null)) {
                throw exception(ACCESS_PERSON_CODE_DUPLICATE);
            }
        }
        personMapper.insert(person);
        return person.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotAccessPersonBatchCreateRespVO batchCreatePerson(IotAccessPersonBatchCreateReqVO reqVO) {
        IotAccessPersonBatchCreateRespVO respVO = new IotAccessPersonBatchCreateRespVO();
        List<Long> createdIds = new ArrayList<>();
        List<IotAccessPersonBatchCreateRespVO.ErrorItem> errors = new ArrayList<>();

        // 1. 预校验所有人员编号的唯一性
        for (IotAccessPersonBatchCreateReqVO.PersonItem item : reqVO.getPersons()) {
            if (!isPersonCodeUnique(item.getPersonCode(), null)) {
                errors.add(new IotAccessPersonBatchCreateRespVO.ErrorItem(
                        item.getPersonCode(), "人员编号已存在"));
            }
        }

        // 2. 预校验所有卡号的唯一性
        for (IotAccessPersonBatchCreateReqVO.PersonItem item : reqVO.getPersons()) {
            if (item.getCardNo() != null && !item.getCardNo().isEmpty()) {
                if (!isCardNoUnique(item.getCardNo(), null)) {
                    errors.add(new IotAccessPersonBatchCreateRespVO.ErrorItem(
                            item.getPersonCode(), "卡号 " + item.getCardNo() + " 已被使用"));
                }
            }
        }

        // 3. 如果有任何错误，直接返回（事务回滚）
        if (!errors.isEmpty()) {
            respVO.setSuccessCount(0);
            respVO.setFailCount(errors.size());
            respVO.setCreatedIds(createdIds);
            respVO.setErrors(errors);
            throw exception(ACCESS_PERSON_BATCH_CREATE_FAILED);
        }

        // 4. 批量创建人员
        for (IotAccessPersonBatchCreateReqVO.PersonItem item : reqVO.getPersons()) {
            // 创建人员记录
            IotAccessPersonDO person = IotAccessPersonDO.builder()
                    .personCode(item.getPersonCode())
                    .personName(item.getPersonName())
                    .personType(item.getPersonType() != null ? item.getPersonType() : 1)
                    .deptId(item.getDeptId())
                    .validStart(item.getValidStart())
                    .validEnd(item.getValidEnd())
                    .status(0) // 默认正常状态
                    .build();
            personMapper.insert(person);
            createdIds.add(person.getId());

            // 如果有卡号，创建卡号凭证
            if (item.getCardNo() != null && !item.getCardNo().isEmpty()) {
                IotAccessPersonCredentialDO credential = IotAccessPersonCredentialDO.builder()
                        .personId(person.getId())
                        .credentialType(CredentialTypeConstants.CARD)
                        .credentialData(item.getCardNo())
                        .cardNo(item.getCardNo())
                        .deviceSynced(false)
                        .status(0)
                        .build();
                credentialMapper.insert(credential);
            }
        }

        // 5. 返回结果
        respVO.setSuccessCount(createdIds.size());
        respVO.setFailCount(0);
        respVO.setCreatedIds(createdIds);
        respVO.setErrors(errors);

        log.info("[batchCreatePerson] 批量创建人员成功, 成功数量: {}", createdIds.size());
        return respVO;
    }

    /**
     * 生成人员编号
     * 格式：YG + 6位流水号，如 YG000001
     */
    private synchronized String generatePersonCode() {
        String maxCode = personMapper.selectMaxPersonCode();
        int nextSeq = 1;
        if (maxCode != null && maxCode.startsWith(PERSON_CODE_PREFIX)) {
            try {
                String seqStr = maxCode.substring(PERSON_CODE_PREFIX.length());
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                log.warn("解析人员编号失败: {}", maxCode);
            }
        }
        return PERSON_CODE_PREFIX + String.format("%0" + PERSON_CODE_SEQ_LENGTH + "d", nextSeq);
    }

    @Override
    public void updatePerson(IotAccessPersonDO person) {
        validatePersonExists(person.getId());
        // 校验人员编号唯一性
        if (person.getPersonCode() != null && !isPersonCodeUnique(person.getPersonCode(), person.getId())) {
            throw exception(ACCESS_PERSON_CODE_DUPLICATE);
        }
        personMapper.updateById(person);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePerson(Long id) {
        validatePersonExists(id);
        // 删除人员凭证
        credentialMapper.deleteByPersonId(id);
        // 删除人员
        personMapper.deleteById(id);
    }

    @Override
    public IotAccessPersonDO getPerson(Long id) {
        return personMapper.selectById(id);
    }

    @Override
    public PageResult<IotAccessPersonDO> getPersonPage(String personCode, String personName,
                                                        Integer personType, Long deptId, Integer status,
                                                        Integer pageNo, Integer pageSize) {
        // 获取部门及其所有子孙部门的ID列表
        List<Long> deptIds = null;
        if (deptId != null) {
            deptIds = departmentService.getDeptAndChildrenIds(deptId);
        }
        return personMapper.selectPage(personCode, personName, personType, deptIds, status, pageNo, pageSize);
    }

    @Override
    public List<IotAccessPersonDO> getPersonListByDeptId(Long deptId) {
        return personMapper.selectListByDeptId(deptId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importPersons(List<IotAccessPersonDO> persons, boolean updateSupport) {
        int successCount = 0;
        for (IotAccessPersonDO person : persons) {
            IotAccessPersonDO existing = personMapper.selectByPersonCode(person.getPersonCode());
            if (existing == null) {
                personMapper.insert(person);
                successCount++;
            } else if (updateSupport) {
                person.setId(existing.getId());
                personMapper.updateById(person);
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public boolean isPersonCodeUnique(String personCode, Long excludeId) {
        IotAccessPersonDO person = personMapper.selectByPersonCode(personCode);
        if (person == null) {
            return true;
        }
        return excludeId != null && excludeId.equals(person.getId());
    }

    @Override
    public boolean isPersonValid(Long personId) {
        IotAccessPersonDO person = personMapper.selectById(personId);
        if (person == null) {
            return false;
        }
        // 检查状态
        if (person.getStatus() != null && person.getStatus() != 0) {
            return false;
        }
        // 检查有效期
        LocalDateTime now = LocalDateTime.now();
        if (person.getValidStart() != null && now.isBefore(person.getValidStart())) {
            return false;
        }
        if (person.getValidEnd() != null && now.isAfter(person.getValidEnd())) {
            return false;
        }
        return true;
    }

    // ========== 凭证管理 ==========

    @Override
    public void setPassword(Long personId, String password) {
        validatePersonExists(personId);
        // 查找已有密码凭证
        IotAccessPersonCredentialDO existing = credentialMapper.selectByPersonIdAndType(personId, CredentialTypeConstants.PASSWORD);
        if (existing != null) {
            existing.setCredentialData(password);
            credentialMapper.updateById(existing);
        } else {
            IotAccessPersonCredentialDO credential = IotAccessPersonCredentialDO.builder()
                    .personId(personId)
                    .credentialType(CredentialTypeConstants.PASSWORD)
                    .credentialData(password)
                    .deviceSynced(false)
                    .build();
            credentialMapper.insert(credential);
        }
    }

    @Override
    public void addCard(Long personId, String cardNo) {
        validatePersonExists(personId);
        // 校验卡号唯一性
        if (!isCardNoUnique(cardNo, null)) {
            throw exception(ACCESS_CARD_NO_DUPLICATE);
        }
        IotAccessPersonCredentialDO credential = IotAccessPersonCredentialDO.builder()
                .personId(personId)
                .credentialType(CredentialTypeConstants.CARD)
                .credentialData(cardNo)
                .cardNo(cardNo)
                .issueTime(LocalDateTime.now()) // SmartPSS: 发卡时间
                .cardStatus(0) // 正常状态
                .deviceSynced(false)
                .status(0)
                .build();
        credentialMapper.insert(credential);
        log.info("[addCard] 添加卡片成功, personId={}, cardNo={}", personId, cardNo);
    }

    @Override
    public void removeCard(Long personId, String cardNo) {
        validatePersonExists(personId);
        credentialMapper.deleteByPersonIdAndTypeAndData(personId, CredentialTypeConstants.CARD, cardNo);
    }

    @Override
    public void addFingerprint(Long personId, byte[] fingerprintData, Integer fingerIndex) {
        validatePersonExists(personId);
        
        // 1. 校验指纹序号有效性（0-9，对应10个手指）
        if (fingerIndex == null || fingerIndex < 0 || fingerIndex > 9) {
            throw exception(ACCESS_FINGERPRINT_INDEX_INVALID);
        }
        
        // 2. 校验该指位是否已录入（同一人同一手指不能重复录入）
        IotAccessPersonCredentialDO existing = credentialMapper.selectByPersonIdAndFingerIndex(personId, fingerIndex);
        if (existing != null) {
            throw exception(ACCESS_FINGERPRINT_INDEX_DUPLICATE);
        }
        
        // 3. 校验指纹数量上限（一人最多10个指纹）
        Long count = credentialMapper.countByPersonIdAndType(personId, CredentialTypeConstants.FINGERPRINT);
        if (count >= 10) {
            throw exception(ACCESS_FINGERPRINT_MAX_COUNT);
        }
        
        // 4. 将指纹数据转为Base64存储
        String dataStr = java.util.Base64.getEncoder().encodeToString(fingerprintData);
        IotAccessPersonCredentialDO credential = IotAccessPersonCredentialDO.builder()
                .personId(personId)
                .credentialType(CredentialTypeConstants.FINGERPRINT)
                .credentialData(dataStr)
                .fingerIndex(fingerIndex)
                .fingerName(getFingerName(fingerIndex))
                .deviceSynced(false)
                .status(0)
                .build();
        credentialMapper.insert(credential);
        
        log.info("[addFingerprint] 添加指纹成功, personId={}, fingerIndex={}, fingerName={}", 
                personId, fingerIndex, credential.getFingerName());
    }
    
    /**
     * 根据指纹序号获取手指名称
     * 
     * @param fingerIndex 指纹序号（0-9）
     * @return 手指名称
     */
    private String getFingerName(Integer fingerIndex) {
        String[] fingerNames = {
            "右手拇指", "右手食指", "右手中指", "右手无名指", "右手小指",
            "左手拇指", "左手食指", "左手中指", "左手无名指", "左手小指"
        };
        if (fingerIndex >= 0 && fingerIndex < fingerNames.length) {
            return fingerNames[fingerIndex];
        }
        return "未知";
    }

    @Override
    public void removeFingerprint(Long personId, Integer fingerIndex) {
        validatePersonExists(personId);
        credentialMapper.deleteByPersonIdAndTypeAndFingerIndex(personId, CredentialTypeConstants.FINGERPRINT, fingerIndex);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFace(Long personId, byte[] faceData) {
        validatePersonExists(personId);
        
        try {
            // 1. 使用 infra 文件管理服务保存人脸照片
            String directory = String.format("access/faces/%d/%04d%02d%02d", personId,
                    LocalDateTime.now().getYear(), 
                    LocalDateTime.now().getMonthValue(), 
                    LocalDateTime.now().getDayOfMonth());
            String fileName = String.format("face_%d_%d.jpg", personId, System.currentTimeMillis());
            String faceUrl = fileApi.createFile(faceData, fileName, directory, "image/jpeg");
            
            // 2. 更新人员表的 face_url 字段
            IotAccessPersonDO person = personMapper.selectById(personId);
            person.setFaceUrl(faceUrl);
            personMapper.updateById(person);
            
            // 3. 删除已有人脸凭证
            credentialMapper.deleteByPersonIdAndType(personId, CredentialTypeConstants.FACE);
            
            // 4. 将人脸数据转为Base64存储到凭证表（用于设备同步）
            String dataStr = java.util.Base64.getEncoder().encodeToString(faceData);
            IotAccessPersonCredentialDO credential = IotAccessPersonCredentialDO.builder()
                    .personId(personId)
                    .credentialType(CredentialTypeConstants.FACE)
                    .credentialData(dataStr)
                    .deviceSynced(false)
                    .build();
            credentialMapper.insert(credential);
            
            log.info("[addFace] 人脸照片上传成功, personId={}, faceUrl={}", personId, faceUrl);
        } catch (Exception e) {
            log.error("[addFace] 人脸照片上传失败, personId={}", personId, e);
            throw exception(ACCESS_PERSON_FACE_UPLOAD_FAILED);
        }
    }

    @Override
    public void removeFace(Long personId) {
        validatePersonExists(personId);
        credentialMapper.deleteByPersonIdAndType(personId, CredentialTypeConstants.FACE);
    }

    @Override
    public List<IotAccessPersonCredentialDO> getPersonCredentials(Long personId) {
        return credentialMapper.selectListByPersonId(personId);
    }

    @Override
    public boolean isCardNoUnique(String cardNo, Long excludeId) {
        IotAccessPersonCredentialDO credential = credentialMapper.selectByCardNo(cardNo);
        if (credential == null) {
            return true;
        }
        return excludeId != null && excludeId.equals(credential.getId());
    }

    private void validatePersonExists(Long id) {
        if (personMapper.selectById(id) == null) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
    }

    // ========== 卡片操作（SmartPSS功能对齐） ==========

    @Override
    public void reportCardLost(Long credentialId) {
        IotAccessPersonCredentialDO credential = credentialMapper.selectById(credentialId);
        if (credential == null) {
            throw exception(ACCESS_CREDENTIAL_NOT_EXISTS);
        }
        if (!CredentialTypeConstants.isCard(credential.getCredentialType())) {
            throw exception(ACCESS_CREDENTIAL_TYPE_INVALID);
        }
        // 卡状态：0-正常，1-挂失，2-注销
        credential.setCardStatus(1);
        credentialMapper.updateById(credential);
        log.info("[reportCardLost] 卡片挂失成功, credentialId={}, cardNo={}", credentialId, credential.getCardNo());
    }

    @Override
    public void cancelCardLost(Long credentialId) {
        IotAccessPersonCredentialDO credential = credentialMapper.selectById(credentialId);
        if (credential == null) {
            throw exception(ACCESS_CREDENTIAL_NOT_EXISTS);
        }
        if (!CredentialTypeConstants.isCard(credential.getCredentialType())) {
            throw exception(ACCESS_CREDENTIAL_TYPE_INVALID);
        }
        // 只有挂失状态才能解挂
        if (credential.getCardStatus() == null || credential.getCardStatus() != 1) {
            throw exception(ACCESS_CARD_NOT_LOST);
        }
        credential.setCardStatus(0);
        credentialMapper.updateById(credential);
        log.info("[cancelCardLost] 卡片解挂成功, credentialId={}, cardNo={}", credentialId, credential.getCardNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceCard(Long credentialId, String newCardNo) {
        IotAccessPersonCredentialDO credential = credentialMapper.selectById(credentialId);
        if (credential == null) {
            throw exception(ACCESS_CREDENTIAL_NOT_EXISTS);
        }
        if (!CredentialTypeConstants.isCard(credential.getCredentialType())) {
            throw exception(ACCESS_CREDENTIAL_TYPE_INVALID);
        }
        // 校验新卡号唯一性
        if (!isCardNoUnique(newCardNo, credentialId)) {
            throw exception(ACCESS_CARD_NO_DUPLICATE);
        }
        // 记录旧卡号
        String oldCardNo = credential.getCardNo();
        credential.setOldCardNo(oldCardNo);
        credential.setCardNo(newCardNo);
        credential.setCredentialData(newCardNo);
        credential.setReplaceTime(LocalDateTime.now());
        credential.setCardStatus(0); // 换卡后状态恢复正常
        credential.setDeviceSynced(false); // 需要重新同步到设备
        credentialMapper.updateById(credential);
        log.info("[replaceCard] 换卡成功, credentialId={}, oldCardNo={}, newCardNo={}", credentialId, oldCardNo, newCardNo);
    }

    @Override
    public List<IotAccessPersonCredentialDO> getCredentialsByPersonId(Long personId) {
        return credentialMapper.selectListByPersonId(personId);
    }

    // ========== 冻结/解冻 ==========

    @Override
    public void freezePerson(Long id) {
        IotAccessPersonDO person = personMapper.selectById(id);
        if (person == null) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
        // 状态 1=正常, 0=禁用(冻结)
        if (person.getStatus() != null && person.getStatus() == 0) {
            // 已经是冻结状态
            return;
        }
        person.setStatus(0); // 设置为禁用(冻结)状态
        personMapper.updateById(person);
        log.info("[freezePerson] 冻结人员成功, personId={}, personName={}", id, person.getPersonName());
    }

    @Override
    public void unfreezePerson(Long id) {
        IotAccessPersonDO person = personMapper.selectById(id);
        if (person == null) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
        // 状态 1=正常, 0=禁用(冻结)
        if (person.getStatus() != null && person.getStatus() == 1) {
            // 已经是正常状态
            return;
        }
        person.setStatus(1); // 设置为正常状态
        personMapper.updateById(person);
        log.info("[unfreezePerson] 解冻人员成功, personId={}, personName={}", id, person.getPersonName());
    }

}
