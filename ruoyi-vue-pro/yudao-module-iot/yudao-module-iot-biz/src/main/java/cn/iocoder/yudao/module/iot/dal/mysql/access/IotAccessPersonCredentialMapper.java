package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 门禁人员凭证 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessPersonCredentialMapper extends BaseMapperX<IotAccessPersonCredentialDO> {

    default List<IotAccessPersonCredentialDO> selectListByPersonId(Long personId) {
        return selectList(IotAccessPersonCredentialDO::getPersonId, personId);
    }

    /**
     * 根据卡号查询有效凭证（排除已删除和已停用的）
     * <p>
     * 业务规则：一张卡号在同一租户下只能属于一个人
     * </p>
     *
     * @param cardNo 卡号
     * @return 凭证信息
     */
    default IotAccessPersonCredentialDO selectByCardNo(String cardNo) {
        // 只查询有效的卡片凭证（status=0 正常状态）
        return selectOne(new LambdaQueryWrapperX<IotAccessPersonCredentialDO>()
                .eq(IotAccessPersonCredentialDO::getCardNo, cardNo)
                .eq(IotAccessPersonCredentialDO::getStatus, 0)
                .last("LIMIT 1"));
    }

    /**
     * 根据卡号查询所有凭证（包括已停用的，用于完整性检查）
     *
     * @param cardNo 卡号
     * @return 凭证列表
     */
    default List<IotAccessPersonCredentialDO> selectListByCardNo(String cardNo) {
        return selectList(new LambdaQueryWrapperX<IotAccessPersonCredentialDO>()
                .eq(IotAccessPersonCredentialDO::getCardNo, cardNo));
    }

    default List<IotAccessPersonCredentialDO> selectListByPersonIdAndType(Long personId, String credentialType) {
        return selectList(new LambdaQueryWrapperX<IotAccessPersonCredentialDO>()
                .eq(IotAccessPersonCredentialDO::getPersonId, personId)
                .eq(IotAccessPersonCredentialDO::getCredentialType, credentialType));
    }

    /**
     * 根据人员ID和指纹序号查询指纹凭证
     * <p>
     * 业务规则：同一人员的同一手指位置只能录入一次指纹
     * </p>
     *
     * @param personId    人员ID
     * @param fingerIndex 指纹序号
     * @return 指纹凭证
     */
    default IotAccessPersonCredentialDO selectByPersonIdAndFingerIndex(Long personId, Integer fingerIndex) {
        return selectOne(new LambdaQueryWrapperX<IotAccessPersonCredentialDO>()
                .eq(IotAccessPersonCredentialDO::getPersonId, personId)
                .eq(IotAccessPersonCredentialDO::getCredentialType, "FINGERPRINT")
                .eq(IotAccessPersonCredentialDO::getFingerIndex, fingerIndex));
    }

    default void deleteByPersonId(Long personId) {
        delete(IotAccessPersonCredentialDO::getPersonId, personId);
    }

    default IotAccessPersonCredentialDO selectByPersonIdAndType(Long personId, String credentialType) {
        return selectOne(new LambdaQueryWrapperX<IotAccessPersonCredentialDO>()
                .eq(IotAccessPersonCredentialDO::getPersonId, personId)
                .eq(IotAccessPersonCredentialDO::getCredentialType, credentialType));
    }

    default void deleteByPersonIdAndType(Long personId, String credentialType) {
        delete(new LambdaQueryWrapperX<IotAccessPersonCredentialDO>()
                .eq(IotAccessPersonCredentialDO::getPersonId, personId)
                .eq(IotAccessPersonCredentialDO::getCredentialType, credentialType));
    }

    default void deleteByPersonIdAndTypeAndData(Long personId, String credentialType, String data) {
        delete(new LambdaQueryWrapperX<IotAccessPersonCredentialDO>()
                .eq(IotAccessPersonCredentialDO::getPersonId, personId)
                .eq(IotAccessPersonCredentialDO::getCredentialType, credentialType)
                .eq(IotAccessPersonCredentialDO::getCredentialData, data));
    }

    default void deleteByPersonIdAndTypeAndFingerIndex(Long personId, String credentialType, Integer fingerIndex) {
        delete(new LambdaQueryWrapperX<IotAccessPersonCredentialDO>()
                .eq(IotAccessPersonCredentialDO::getPersonId, personId)
                .eq(IotAccessPersonCredentialDO::getCredentialType, credentialType)
                .eq(IotAccessPersonCredentialDO::getFingerIndex, fingerIndex));
    }

    /**
     * 统计指定人员的指定类型凭证数量
     */
    default Long countByPersonIdAndType(Long personId, String credentialType) {
        return selectCount(new LambdaQueryWrapperX<IotAccessPersonCredentialDO>()
                .eq(IotAccessPersonCredentialDO::getPersonId, personId)
                .eq(IotAccessPersonCredentialDO::getCredentialType, credentialType));
    }

    /**
     * 批量统计多个人员的指纹数量
     * @param personIds 人员ID列表
     * @return Map<personId, count>
     */
    @Select("<script>" +
            "SELECT person_id, COUNT(*) as count FROM iot_access_person_credential " +
            "WHERE credential_type = 'fingerprint' AND deleted = 0 " +
            "AND person_id IN " +
            "<foreach collection='personIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " GROUP BY person_id" +
            "</script>")
    List<Map<String, Object>> countFingerprintByPersonIds(@Param("personIds") List<Long> personIds);

}
