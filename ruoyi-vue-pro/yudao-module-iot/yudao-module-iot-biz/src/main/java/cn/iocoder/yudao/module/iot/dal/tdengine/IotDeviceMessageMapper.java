package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 设备消息 {@link IotDeviceMessageDO} Mapper 接口
 * 
 * 注意：已从 TDEngine 迁移到 MySQL，保留原有包路径以减少代码改动
 */
@Mapper
@InterceptorIgnore(tenantLine = "true") // 避免租户拦截
public interface IotDeviceMessageMapper {

    /**
     * 创建设备消息表（MySQL版本：空实现，表已通过SQL脚本创建）
     */
    default void createSTable() {
        // MySQL 表已通过 SQL 脚本创建，此方法保留为兼容接口
    }

    /**
     * 查询设备消息表是否存在
     *
     * @return 存在则返回表名；不存在则返回 null
     */
    String showSTable();

    /**
     * 插入设备消息数据
     *
     * @param message 设备消息数据
     */
    void insert(IotDeviceMessageDO message);

    /**
     * 获得设备消息分页
     *
     * @param reqVO 分页查询条件
     * @return 设备消息列表
     */
    IPage<IotDeviceMessageDO> selectPage(IPage<IotDeviceMessageDO> page,
                                         @Param("reqVO") IotDeviceMessagePageReqVO reqVO);

    /**
     * 统计设备消息数量
     *
     * @param createTime 创建时间，如果为空，则统计所有消息数量
     * @return 消息数量
     */
    Long selectCountByCreateTime(@Param("createTime") Long createTime);

    /**
     * 按照 requestIds 批量查询消息
     *
     * @param deviceId 设备编号
     * @param requestIds 请求编号集合
     * @param reply 是否回复消息
     * @return 消息列表
     */
    List<IotDeviceMessageDO> selectListByRequestIdsAndReply(@Param("deviceId") Long deviceId,
                                                            @Param("requestIds") Collection<String> requestIds,
                                                            @Param("reply") Boolean reply);

    /**
     * 按照时间范围（小时），统计设备的消息数量
     */
    List<Map<String, Object>> selectDeviceMessageCountGroupByDate(@Param("startTime") Long startTime,
                                                                  @Param("endTime") Long endTime);

}
