package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessTimeTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门禁时间模板 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAccessTimeTemplateMapper extends BaseMapperX<IotAccessTimeTemplateDO> {

    default PageResult<IotAccessTimeTemplateDO> selectPage(String templateName, Integer status,
                                                            Integer pageNo, Integer pageSize) {
        cn.iocoder.yudao.framework.common.pojo.PageParam pageParam = new cn.iocoder.yudao.framework.common.pojo.PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam,
                new LambdaQueryWrapperX<IotAccessTimeTemplateDO>()
                .likeIfPresent(IotAccessTimeTemplateDO::getTemplateName, templateName)
                .eqIfPresent(IotAccessTimeTemplateDO::getStatus, status)
                .orderByDesc(IotAccessTimeTemplateDO::getId));
    }

    default List<IotAccessTimeTemplateDO> selectListByStatus(Integer status) {
        return selectList(IotAccessTimeTemplateDO::getStatus, status);
    }

}
