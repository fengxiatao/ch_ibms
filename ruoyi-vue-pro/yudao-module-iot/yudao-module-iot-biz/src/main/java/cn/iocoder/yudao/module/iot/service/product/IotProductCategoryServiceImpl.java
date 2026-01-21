package cn.iocoder.yudao.module.iot.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategorySaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductCategoryDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductCategoryMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_CATEGORY_NOT_EXISTS;

/**
 * IoT 产品分类 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
public class IotProductCategoryServiceImpl implements IotProductCategoryService {

    @Resource
    private IotProductCategoryMapper iotProductCategoryMapper;

    @Resource
    private IotProductService productService;

    @Resource
    private IotDeviceService deviceService;

    public Long createProductCategory(IotProductCategorySaveReqVO createReqVO) {
        // 插入
        IotProductCategoryDO productCategory = BeanUtils.toBean(createReqVO, IotProductCategoryDO.class);
        iotProductCategoryMapper.insert(productCategory);
        // 返回
        return productCategory.getId();
    }

    @Override
    public void updateProductCategory(IotProductCategorySaveReqVO updateReqVO) {
        // 校验存在
        validateProductCategoryExists(updateReqVO.getId());
        // 更新
        IotProductCategoryDO updateObj = BeanUtils.toBean(updateReqVO, IotProductCategoryDO.class);
        iotProductCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductCategory(Long id) {
        // 校验存在
        validateProductCategoryExists(id);
        // 删除
        iotProductCategoryMapper.deleteById(id);
    }

    private void validateProductCategoryExists(Long id) {
        if (iotProductCategoryMapper.selectById(id) == null) {
            throw exception(PRODUCT_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public IotProductCategoryDO getProductCategory(Long id) {
        return iotProductCategoryMapper.selectById(id);
    }

    @Override
    public List<IotProductCategoryDO> getProductCategoryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return CollUtil.newArrayList();
        }
        return iotProductCategoryMapper.selectByIds(ids);
    }

    @Override
    public PageResult<IotProductCategoryDO> getProductCategoryPage(IotProductCategoryPageReqVO pageReqVO) {
        return iotProductCategoryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotProductCategoryDO> getProductCategoryListByStatus(Integer status) {
        return iotProductCategoryMapper.selectListByStatus(status);
    }

    @Override
    public Long getProductCategoryCount(LocalDateTime createTime) {
        return iotProductCategoryMapper.selectCountByCreateTime(createTime);
    }

    @Override
    public Map<String, Integer> getProductCategoryDeviceCountMap() {
        // 新架构：产品不再使用分类，已改为菜单关联
        // 返回空Map以保持接口兼容性
        return new HashMap<>();
        
        // 旧实现已注释（保留用于参考）：
        // List<IotProductCategoryDO> categories = iotProductCategoryMapper.selectList();
        // List<IotProductDO> products = productService.getProductList();
        // Map<Long, Integer> deviceCountMapByProductId = deviceService.getDeviceCountMapByProductId();
        // Map<String, Integer> categoryDeviceCountMap = new HashMap<>();
        // for (IotProductCategoryDO category : categories) {
        //     List<IotProductDO> categoryProducts = filterList(products, 
        //         product -> Objects.equals(product.getCategoryId(), category.getId()));
        //     Integer totalDeviceCount = getSumValue(categoryProducts, 
        //         product -> deviceCountMapByProductId.getOrDefault(product.getId(), 0), 
        //         Integer::sum, 0);
        //     categoryDeviceCountMap.put(category.getName(), totalDeviceCount);
        // }
        // return categoryDeviceCountMap;
    }

    @Override
    public List<IotProductCategoryDO> getProductCategoryTree() {
        // 查询所有启用的分类
        return iotProductCategoryMapper.selectListForTree();
    }

    @Override
    public List<IotProductCategoryDO> getProductCategoryTreeByModule(String moduleCode) {
        // 根据模块编码查询分类
        return iotProductCategoryMapper.selectListByModuleCode(moduleCode);
    }

    @Override
    public List<IotProductCategoryDO> getChildCategories(Long parentId) {
        // 根据父 ID 查询子分类
        return iotProductCategoryMapper.selectListByParentId(parentId);
    }

}
