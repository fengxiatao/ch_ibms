package cn.iocoder.yudao.module.iot.dal.mysql.security;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.security.SecurityStatisticsDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;

/**
 * 安防统计数据 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface SecurityStatisticsMapper extends BaseMapperX<SecurityStatisticsDO> {

    /**
     * 根据日期和类型查询统计数据
     *
     * @param statDate 统计日期
     * @param statType 统计类型
     * @return 统计数据
     */
    default SecurityStatisticsDO selectByDate(LocalDate statDate, String statType) {
        return selectOne(SecurityStatisticsDO::getStatDate, statDate,
                SecurityStatisticsDO::getStatType, statType);
    }

}



















