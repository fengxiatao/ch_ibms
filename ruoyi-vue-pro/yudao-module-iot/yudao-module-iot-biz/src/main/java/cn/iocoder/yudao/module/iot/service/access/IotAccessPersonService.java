package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.person.IotAccessPersonBatchCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.person.IotAccessPersonBatchCreateRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO;

import java.util.List;

/**
 * 门禁人员管理 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAccessPersonService {

    // ========== 人员管理 ==========

    /**
     * 创建人员
     *
     * @param person 人员信息
     * @return 人员ID
     */
    Long createPerson(IotAccessPersonDO person);

    /**
     * 批量创建人员
     *
     * @param reqVO 批量创建请求
     * @return 批量创建结果
     */
    IotAccessPersonBatchCreateRespVO batchCreatePerson(IotAccessPersonBatchCreateReqVO reqVO);

    /**
     * 更新人员
     *
     * @param person 人员信息
     */
    void updatePerson(IotAccessPersonDO person);

    /**
     * 删除人员
     *
     * @param id 人员ID
     */
    void deletePerson(Long id);

    /**
     * 获取人员
     *
     * @param id 人员ID
     * @return 人员信息
     */
    IotAccessPersonDO getPerson(Long id);

    /**
     * 获取人员分页
     *
     * @param personCode 人员编号
     * @param personName 人员姓名
     * @param personType 人员类型
     * @param deptId     部门ID
     * @param status     状态
     * @param pageNo     页码
     * @param pageSize   每页大小
     * @return 人员分页
     */
    PageResult<IotAccessPersonDO> getPersonPage(String personCode, String personName,
                                                 Integer personType, Long deptId, Integer status,
                                                 Integer pageNo, Integer pageSize);

    /**
     * 根据部门获取人员列表
     *
     * @param deptId 部门ID
     * @return 人员列表
     */
    List<IotAccessPersonDO> getPersonListByDeptId(Long deptId);

    /**
     * 批量导入人员
     *
     * @param persons 人员列表
     * @param updateSupport 是否更新已存在的人员
     * @return 导入结果（成功数量）
     */
    int importPersons(List<IotAccessPersonDO> persons, boolean updateSupport);

    /**
     * 校验人员编号是否唯一
     *
     * @param personCode 人员编号
     * @param excludeId  排除的人员ID
     * @return 是否唯一
     */
    boolean isPersonCodeUnique(String personCode, Long excludeId);

    /**
     * 检查人员是否在有效期内
     *
     * @param personId 人员ID
     * @return 是否有效
     */
    boolean isPersonValid(Long personId);

    // ========== 凭证管理 ==========

    /**
     * 设置密码
     *
     * @param personId 人员ID
     * @param password 密码
     */
    void setPassword(Long personId, String password);

    /**
     * 添加卡片
     *
     * @param personId 人员ID
     * @param cardNo   卡号
     */
    void addCard(Long personId, String cardNo);

    /**
     * 删除卡片
     *
     * @param personId 人员ID
     * @param cardNo   卡号
     */
    void removeCard(Long personId, String cardNo);

    /**
     * 录入指纹
     *
     * @param personId        人员ID
     * @param fingerprintData 指纹数据
     * @param fingerIndex     手指索引（0-9）
     */
    void addFingerprint(Long personId, byte[] fingerprintData, Integer fingerIndex);

    /**
     * 删除指纹
     *
     * @param personId    人员ID
     * @param fingerIndex 手指索引
     */
    void removeFingerprint(Long personId, Integer fingerIndex);

    /**
     * 录入人脸
     *
     * @param personId 人员ID
     * @param faceData 人脸数据
     */
    void addFace(Long personId, byte[] faceData);

    /**
     * 删除人脸
     *
     * @param personId 人员ID
     */
    void removeFace(Long personId);

    /**
     * 获取人员凭证列表
     *
     * @param personId 人员ID
     * @return 凭证列表
     */
    List<IotAccessPersonCredentialDO> getPersonCredentials(Long personId);

    /**
     * 校验卡号是否唯一
     *
     * @param cardNo    卡号
     * @param excludeId 排除的凭证ID
     * @return 是否唯一
     */
    boolean isCardNoUnique(String cardNo, Long excludeId);

    // ========== 卡片操作（SmartPSS功能对齐） ==========

    /**
     * 卡片挂失
     *
     * @param credentialId 凭证ID
     */
    void reportCardLost(Long credentialId);

    /**
     * 卡片解挂
     *
     * @param credentialId 凭证ID
     */
    void cancelCardLost(Long credentialId);

    /**
     * 换卡
     *
     * @param credentialId 凭证ID
     * @param newCardNo    新卡号
     */
    void replaceCard(Long credentialId, String newCardNo);

    /**
     * 获取人员凭证列表（包含详细信息）
     *
     * @param personId 人员ID
     * @return 凭证列表
     */
    List<IotAccessPersonCredentialDO> getCredentialsByPersonId(Long personId);

    // ========== 冻结/解冻 ==========

    /**
     * 冻结人员
     * 冻结后该用户将无法正常使用门禁系统
     *
     * @param id 人员ID
     */
    void freezePerson(Long id);

    /**
     * 解冻人员
     * 解冻后该用户恢复正常使用门禁系统
     *
     * @param id 人员ID
     */
    void unfreezePerson(Long id);

}
