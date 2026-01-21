package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.TDengineTableField;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 设备属性 Mapper 接口
 * 
 * 注意：已从 TDEngine 迁移到 MySQL，保留原有包路径以减少代码改动
 * 
 * MySQL版本存储策略：
 * - 使用统一的 iot_device_property_history 表存储所有设备的属性历史
 * - 通过 device_id、product_id 和 identifier 字段区分不同属性
 * - 属性值以原始类型存储在 value 字段中
 */
@Mapper
@InterceptorIgnore(tenantLine = "true") // 避免租户拦截
public interface IotDevicePropertyMapper {

    /**
     * 获取产品属性表字段列表
     * MySQL版本：返回空列表，因为使用统一的属性历史表
     */
    default List<TDengineTableField> getProductPropertySTableFieldList(Long productId) {
        // MySQL 使用统一表结构，不需要获取动态字段
        return new ArrayList<>();
    }

    /**
     * 创建产品属性超级表
     * MySQL版本：空实现，表已通过SQL脚本创建
     */
    default void createProductPropertySTable(Long productId, List<TDengineTableField> fields) {
        // MySQL 表已预先创建，此方法保留为兼容接口
    }

    /**
     * 修改产品属性超级表结构
     * MySQL版本：空实现，使用统一表结构
     */
    default void alterProductPropertySTable(Long productId,
                                            List<TDengineTableField> oldFields,
                                            List<TDengineTableField> newFields) {
        // MySQL 使用统一表结构，不需要动态修改表结构
    }

    /**
     * 添加字段 - MySQL版本：空实现
     */
    default void alterProductPropertySTableAddField(Long productId, TDengineTableField field) {
        // 无需实现
    }

    /**
     * 修改字段 - MySQL版本：空实现
     */
    default void alterProductPropertySTableModifyField(Long productId, TDengineTableField field) {
        // 无需实现
    }

    /**
     * 删除字段 - MySQL版本：空实现
     */
    default void alterProductPropertySTableDropField(Long productId, TDengineTableField field) {
        // 无需实现
    }

    /**
     * 插入设备属性数据
     * MySQL版本：将每个属性拆分为单独的记录插入
     * 
     * @param device 设备信息
     * @param properties 属性Map（key为属性标识符，value为属性值）
     * @param reportTime 上报时间戳（毫秒）
     */
    void insert(@Param("device") IotDeviceDO device,
                @Param("properties") Map<String, Object> properties,
                @Param("reportTime") Long reportTime);

    /**
     * 批量插入设备属性数据
     * 
     * @param deviceId 设备ID
     * @param productId 产品ID
     * @param properties 属性Map
     * @param reportTime 上报时间戳
     */
    void batchInsert(@Param("deviceId") Long deviceId,
                     @Param("productId") Long productId,
                     @Param("properties") Map<String, Object> properties,
                     @Param("reportTime") Long reportTime);

    /**
     * 查询设备属性历史数据
     * 
     * @param reqVO 查询条件
     * @return 属性历史列表
     */
    List<IotDevicePropertyRespVO> selectListByHistory(@Param("reqVO") IotDevicePropertyHistoryListReqVO reqVO);

}
