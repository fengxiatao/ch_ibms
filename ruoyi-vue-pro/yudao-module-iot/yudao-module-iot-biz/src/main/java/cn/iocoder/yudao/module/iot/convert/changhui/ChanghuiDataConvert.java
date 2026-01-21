package cn.iocoder.yudao.module.iot.convert.changhui;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiDataDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 长辉设备数据 Convert
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiDataConvert {

    ChanghuiDataConvert INSTANCE = Mappers.getMapper(ChanghuiDataConvert.class);

    /**
     * 保存请求VO转DO
     */
    ChanghuiDataDO convert(ChanghuiDataSaveReqVO reqVO);

    /**
     * DO转响应VO
     */
    ChanghuiDataRespVO convert(ChanghuiDataDO data);

    /**
     * DO列表转响应VO列表
     */
    List<ChanghuiDataRespVO> convertList(List<ChanghuiDataDO> list);

    /**
     * 分页结果转换
     */
    PageResult<ChanghuiDataRespVO> convertPage(PageResult<ChanghuiDataDO> page);

}
