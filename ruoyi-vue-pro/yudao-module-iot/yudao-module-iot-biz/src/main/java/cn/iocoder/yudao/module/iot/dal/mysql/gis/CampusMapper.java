package cn.iocoder.yudao.module.iot.dal.mysql.gis;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus.CampusPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 园区 Mapper
 * 
 * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
 *
 * @author IBMS Team
 */
@Mapper
public interface CampusMapper extends BaseMapperX<CampusDO> {

    default PageResult<CampusDO> selectPage(CampusPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CampusDO>()
                .likeIfPresent(CampusDO::getName, reqVO.getName())
                .likeIfPresent(CampusDO::getCode, reqVO.getCode())
                .likeIfPresent(CampusDO::getProvince, reqVO.getProvince())
                .likeIfPresent(CampusDO::getCity, reqVO.getCity())
                .eqIfPresent(CampusDO::getOperationStatus, reqVO.getOperationStatus())
                .betweenIfPresent(CampusDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CampusDO::getId));
    }

    /**
     * 查询所有配置了 jobConfig 的园区
     *
     * @return 园区列表
     */
    default List<CampusDO> selectCampusWithJobConfig() {
        return selectList(new LambdaQueryWrapper<CampusDO>()
                .isNotNull(CampusDO::getJobConfig)
                .ne(CampusDO::getJobConfig, "")
                .ne(CampusDO::getJobConfig, "{}"));
    }

}

