package cn.iocoder.yudao.module.iot.service.ibms.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductPointTypeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductPropertyDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsProductMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsProductPointTypeMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsProductPropertyMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * IBMS 产品管理 Service 实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class IbmsProductServiceImpl implements IbmsProductService {

    private final IbmsProductMapper productMapper;
    private final IbmsProductPointTypeMapper pointTypeMapper;
    private final IbmsProductPropertyMapper propertyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(IbmsProductSaveReqVO reqVO) {
        String productCode = generateProductCode(reqVO.getSystemCode(),
                reqVO.getModelCode(), reqVO.getDeviceTypeCode(), reqVO.getManufacturer());

        IbmsProductDO product = BeanUtils.toBean(reqVO, IbmsProductDO.class);
        product.setId(null);
        product.setProductCode(productCode);

        productMapper.insert(product);
        savePointTypesAndProperties(product.getId(), reqVO.getPointTypes(), reqVO.getProperties());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(IbmsProductSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception0(400, "产品 ID 不能为空");
        }
        IbmsProductDO exist = productMapper.selectById(reqVO.getId());
        if (exist == null) {
            throw ServiceExceptionUtil.exception0(404, "产品不存在");
        }
        IbmsProductDO update = BeanUtils.toBean(reqVO, IbmsProductDO.class);
        update.setProductCode(exist.getProductCode());
        if (reqVO.getExtra() == null) {
            update.setExtra(exist.getExtra());
        }
        productMapper.updateById(update);

        pointTypeMapper.deleteByProductId(exist.getId());
        propertyMapper.deleteByProductId(exist.getId());
        savePointTypesAndProperties(exist.getId(), reqVO.getPointTypes(), reqVO.getProperties());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        IbmsProductDO exist = productMapper.selectById(id);
        if (exist == null) {
            return;
        }
        // TODO 若后续有设备引用该产品，在这里增加引用校验
        pointTypeMapper.deleteByProductId(id);
        propertyMapper.deleteByProductId(id);
        productMapper.deleteById(id);
    }

    @Override
    public IbmsProductRespVO getProduct(Long id) {
        IbmsProductDO product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }
        IbmsProductRespVO vo = BeanUtils.toBean(product, IbmsProductRespVO.class);
        fillDetailFields(vo, id);
        return vo;
    }

    @Override
    public PageResult<IbmsProductRespVO> getProductPage(IbmsProductPageReqVO pageReqVO) {
        PageResult<IbmsProductDO> page = productMapper.selectPage(pageReqVO);
        PageResult<IbmsProductRespVO> result = BeanUtils.toBean(page, IbmsProductRespVO.class);
        if (CollUtil.isEmpty(result.getList())) {
            return result;
        }
        result.getList().forEach(vo -> fillDetailFields(vo, vo.getId()));
        return result;
    }

    @Override
    public IbmsProductRespVO getProductTemplateForDevice(String groupCode, String systemCode, String deviceTypeCode,
                                                         String modelNumber) {
        if (StrUtil.hasBlank(groupCode, systemCode, deviceTypeCode) || StrUtil.isBlank(modelNumber)) {
            return null;
        }
        IbmsProductDO product = productMapper.selectForDeviceTemplate(groupCode, systemCode, deviceTypeCode, modelNumber);
        if (product == null) {
            return null;
        }
        return getProduct(product.getId());
    }

    @Override
    public IbmsProductRespVO getProductByLegacyIotProductKey(String productKey) {
        IbmsProductDO product = productMapper.selectByExtraLegacyProductKey(productKey);
        if (product == null) {
            return null;
        }
        return getProduct(product.getId());
    }

    private void fillDetailFields(IbmsProductRespVO vo, Long productId) {
        List<IbmsProductPointTypeDO> pts = pointTypeMapper.selectListByProductId(productId);
        List<IbmsProductPropertyDO> props = propertyMapper.selectListByProductId(productId);

        vo.setPointTypes(convertList(pts, item -> {
            IbmsProductPointTypeVO t = new IbmsProductPointTypeVO();
            t.setId(item.getId());
            t.setPointTypeCode(item.getPointTypeCode());
            t.setName(item.getName());
            t.setCount(item.getCount());
            t.setDataType(item.getDataType());
            return t;
        }));

        vo.setProperties(convertList(props, item -> {
            IbmsProductPropertyVO p = new IbmsProductPropertyVO();
            p.setId(item.getId());
            p.setPropName(item.getPropName());
            p.setLabel(item.getLabel());
            p.setType(item.getType());
            p.setOptions(item.getOptions());
            p.setDefaultValue(item.getDefaultValue());
            p.setUnit(item.getUnit());
            return p;
        }));
    }

    private void savePointTypesAndProperties(Long productId,
                                             List<IbmsProductPointTypeVO> pointTypes,
                                             List<IbmsProductPropertyVO> properties) {
        if (CollUtil.isNotEmpty(pointTypes)) {
            pointTypes.forEach(vo -> {
                IbmsProductPointTypeDO po = new IbmsProductPointTypeDO();
                po.setProductId(productId);
                po.setPointTypeCode(vo.getPointTypeCode());
                po.setName(vo.getName());
                po.setCount(vo.getCount() != null ? vo.getCount() : 1);
                po.setDataType(vo.getDataType());
                pointTypeMapper.insert(po);
            });
        }
        if (CollUtil.isNotEmpty(properties)) {
            properties.forEach(vo -> {
                IbmsProductPropertyDO po = new IbmsProductPropertyDO();
                po.setProductId(productId);
                po.setPropName(vo.getPropName());
                po.setLabel(vo.getLabel());
                po.setType(vo.getType());
                po.setOptions(normalizeOptionsJson(vo.getOptions()));
                po.setDefaultValue(vo.getDefaultValue());
                po.setUnit(vo.getUnit());
                propertyMapper.insert(po);
            });
        }
    }

    /**
     * 生成产品编码：{系统}-{型号码}-{设备类型}-{品牌}-{流水}
     * 流水在同一 (system, model, deviceType, manufacturer) 组合下递增。
     */
    private String generateProductCode(String system, String model, String deviceType, String manufacturer) {
        String mfg = StrUtil.blankToDefault(manufacturer, "UNK").trim();
        long count = productMapper.selectCount(new LambdaQueryWrapperX<IbmsProductDO>()
                .eq(IbmsProductDO::getSystemCode, system)
                .eq(IbmsProductDO::getModelCode, model)
                .eq(IbmsProductDO::getDeviceTypeCode, deviceType)
                .eq(IbmsProductDO::getManufacturer, mfg));
        String seq = String.format("%03d", count + 1);
        return system + "-" + model + "-" + deviceType + "-" + mfg + "-" + seq;
    }

    /**
     * JSON 列不允许空字符串，统一转换为合法 JSON 文本。
     */
    private String normalizeOptionsJson(String options) {
        if (StrUtil.isBlank(options)) {
            return "[]";
        }
        try {
            return JSONUtil.parse(options).toString();
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception0(400, "产品属性 options 必须是合法 JSON");
        }
    }
}

