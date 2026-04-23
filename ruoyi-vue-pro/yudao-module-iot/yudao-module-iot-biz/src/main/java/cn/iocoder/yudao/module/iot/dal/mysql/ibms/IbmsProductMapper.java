package cn.iocoder.yudao.module.iot.dal.mysql.ibms;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductPageReqVO;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IBMS 产品定义 Mapper
 */
@Mapper
public interface IbmsProductMapper extends BaseMapperX<IbmsProductDO> {

    default PageResult<IbmsProductDO> selectPage(IbmsProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsProductDO>()
                .likeIfPresent(IbmsProductDO::getProductName, reqVO.getProductName())
                .eqIfPresent(IbmsProductDO::getGroupCode, reqVO.getGroupCode())
                .eqIfPresent(IbmsProductDO::getSystemCode, reqVO.getSystemCode())
                .eqIfPresent(IbmsProductDO::getModelCode, reqVO.getModelCode())
                .eqIfPresent(IbmsProductDO::getDeviceTypeCode, reqVO.getDeviceTypeCode())
                .likeIfPresent(IbmsProductDO::getManufacturer, reqVO.getManufacturer())
                .eqIfPresent(IbmsProductDO::getModelNumber, reqVO.getModelNumber())
                .betweenIfPresent(IbmsProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IbmsProductDO::getId));
    }

    /**
     * 按设备录入维度匹配唯一产品（用于加载扩展属性模板）。
     * 匹配键：分组 + 系统 + 设备类型 + 产品型号（与 ibms_product.model_number 一致）。
     */
    default IbmsProductDO selectForDeviceTemplate(String groupCode, String systemCode, String deviceTypeCode,
                                                  String modelNumber) {
        if (modelNumber == null || modelNumber.isBlank()) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IbmsProductDO>()
                .eq(IbmsProductDO::getGroupCode, groupCode)
                .eq(IbmsProductDO::getSystemCode, systemCode)
                .eq(IbmsProductDO::getDeviceTypeCode, deviceTypeCode)
                .eq(IbmsProductDO::getModelNumber, modelNumber.trim())
                .orderByDesc(IbmsProductDO::getId)
                .last("LIMIT 1"));
    }

    /**
     * 按 {@code ibms_product.extra.productKey} 匹配历史 {@code iot_product.product_key}（如 {@code ALARM_HOST_PRODUCT}）。
     */
    default IbmsProductDO selectByExtraLegacyProductKey(String productKey) {
        if (StrUtil.isBlank(productKey)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IbmsProductDO>()
                .apply("JSON_UNQUOTE(JSON_EXTRACT(extra, '$.productKey')) = {0}", productKey.trim())
                .orderByDesc(IbmsProductDO::getId)
                .last("LIMIT 1"));
    }
}

