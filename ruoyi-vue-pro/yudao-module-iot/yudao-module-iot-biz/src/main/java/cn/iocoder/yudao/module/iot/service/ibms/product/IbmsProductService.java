package cn.iocoder.yudao.module.iot.service.ibms.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo.IbmsProductSaveReqVO;

/**
 * IBMS 产品管理 Service 接口
 */
public interface IbmsProductService {

    /**
     * 创建产品
     */
    Long createProduct(IbmsProductSaveReqVO reqVO);

    /**
     * 更新产品
     */
    void updateProduct(IbmsProductSaveReqVO reqVO);

    /**
     * 删除产品
     */
    void deleteProduct(Long id);

    /**
     * 获取产品详情
     */
    IbmsProductRespVO getProduct(Long id);

    /**
     * 分页查询产品
     */
    PageResult<IbmsProductRespVO> getProductPage(IbmsProductPageReqVO pageReqVO);

    /**
     * 按设备维度解析产品模板（含属性定义），用于设备表单动态扩展区。
     * 未匹配到产品时返回 null。
     */
    IbmsProductRespVO getProductTemplateForDevice(String groupCode, String systemCode, String deviceTypeCode,
                                                  String modelNumber);

    /**
     * 按 {@code ibms_product.extra.productKey} 解析与历史 {@code iot_product.product_key} 对齐的模板（如报警主机 {@code ALARM_HOST_PRODUCT}）。
     *
     * @return 未配置时返回 null
     */
    IbmsProductRespVO getProductByLegacyIotProductKey(String productKey);
}

