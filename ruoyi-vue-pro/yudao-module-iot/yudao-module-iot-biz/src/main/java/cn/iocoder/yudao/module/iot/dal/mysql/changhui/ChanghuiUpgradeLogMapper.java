package cn.iocoder.yudao.module.iot.dal.mysql.changhui;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiUpgradeLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 长辉设备升级日志 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiUpgradeLogMapper extends BaseMapperX<ChanghuiUpgradeLogDO> {

    /**
     * 根据任务ID查询日志列表
     *
     * @param taskId 任务ID
     * @return 日志列表
     */
    default List<ChanghuiUpgradeLogDO> selectListByTaskId(Long taskId) {
        return selectList(ChanghuiUpgradeLogDO::getTaskId, taskId);
    }

    /**
     * 根据设备ID查询日志列表
     *
     * @param deviceId 设备ID
     * @return 日志列表
     */
    default List<ChanghuiUpgradeLogDO> selectListByDeviceId(Long deviceId) {
        return selectList(ChanghuiUpgradeLogDO::getDeviceId, deviceId);
    }
}
