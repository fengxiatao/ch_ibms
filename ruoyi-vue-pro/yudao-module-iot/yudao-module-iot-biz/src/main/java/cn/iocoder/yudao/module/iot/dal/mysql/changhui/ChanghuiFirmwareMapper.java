package cn.iocoder.yudao.module.iot.dal.mysql.changhui;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiFirmwareDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 长辉固件 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiFirmwareMapper extends BaseMapperX<ChanghuiFirmwareDO> {

    /**
     * 根据设备类型查询固件列表
     *
     * @param deviceType 设备类型（可选，为null时返回所有）
     * @return 固件列表
     */
    default List<ChanghuiFirmwareDO> selectListByDeviceType(Integer deviceType) {
        return selectList(new LambdaQueryWrapperX<ChanghuiFirmwareDO>()
                .eqIfPresent(ChanghuiFirmwareDO::getDeviceType, deviceType)
                .orderByDesc(ChanghuiFirmwareDO::getCreateTime));
    }

    /**
     * 根据版本号和设备类型查询固件
     *
     * @param version    版本号
     * @param deviceType 设备类型
     * @return 固件信息
     */
    default ChanghuiFirmwareDO selectByVersionAndDeviceType(String version, Integer deviceType) {
        return selectOne(new LambdaQueryWrapperX<ChanghuiFirmwareDO>()
                .eq(ChanghuiFirmwareDO::getVersion, version)
                .eq(ChanghuiFirmwareDO::getDeviceType, deviceType));
    }

}
