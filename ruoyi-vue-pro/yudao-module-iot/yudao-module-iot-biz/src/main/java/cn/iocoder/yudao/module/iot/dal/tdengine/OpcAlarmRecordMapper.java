package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.opc.OpcAlarmRecordDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * OPC报警记录 Mapper
 * 
 * 注意：已从 TDEngine 迁移到 MySQL，保留原有包路径以减少代码改动
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
@InterceptorIgnore(tenantLine = "true") // 避免租户拦截
public interface OpcAlarmRecordMapper {

    /**
     * 检查表是否存在
     * 
     * @return 存在则返回表名；不存在则返回 null
     */
    String showOpcAlarmStable();

    /**
     * 创建OPC报警记录表
     * MySQL版本：空实现，表已通过SQL脚本创建
     */
    default void createOpcAlarmStable() {
        // MySQL 表已通过 SQL 脚本创建，此方法保留为兼容接口
    }

    /**
     * 插入报警记录
     *
     * @param record 报警记录
     */
    void insertAlarmRecord(@Param("record") OpcAlarmRecordDO record);
}
