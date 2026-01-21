package cn.iocoder.yudao.module.iot.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductCategoryDO;
import org.apache.ibatis.annotations.Mapper;

import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 产品分类 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface IotProductCategoryMapper extends BaseMapperX<IotProductCategoryDO> {

    default PageResult<IotProductCategoryDO> selectPage(IotProductCategoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotProductCategoryDO>()
                .likeIfPresent(IotProductCategoryDO::getName, reqVO.getName())
                .betweenIfPresent(IotProductCategoryDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(IotProductCategoryDO::getSort));
    }

    default List<IotProductCategoryDO> selectListByStatus(Integer status) {
        return selectList(IotProductCategoryDO::getStatus, status);
    }

    default Long selectCountByCreateTime(@Nullable LocalDateTime createTime) {
        return selectCount(new LambdaQueryWrapperX<IotProductCategoryDO>()
                .geIfPresent(IotProductCategoryDO::getCreateTime, createTime));
    }

    /**
     * 查询所有启用的分类（用于构建树形结构）
     */
    default List<IotProductCategoryDO> selectListForTree() {
        return selectList(new LambdaQueryWrapperX<IotProductCategoryDO>()
                .eq(IotProductCategoryDO::getStatus, 0) // 只查询启用的分类
                .orderByAsc(IotProductCategoryDO::getSort));
    }

    /**
     * 根据模块编码查询分类列表
     */
    default List<IotProductCategoryDO> selectListByModuleCode(String moduleCode) {
        return selectList(new LambdaQueryWrapperX<IotProductCategoryDO>()
                .eq(IotProductCategoryDO::getModuleCode, moduleCode)
                .eq(IotProductCategoryDO::getStatus, 0)
                .orderByAsc(IotProductCategoryDO::getSort));
    }

    /**
     * 根据父分类 ID 查询子分类列表
     */
    default List<IotProductCategoryDO> selectListByParentId(Long parentId) {
        return selectList(new LambdaQueryWrapperX<IotProductCategoryDO>()
                .eq(IotProductCategoryDO::getParentId, parentId)
                .eq(IotProductCategoryDO::getStatus, 0)
                .orderByAsc(IotProductCategoryDO::getSort));
    }

}