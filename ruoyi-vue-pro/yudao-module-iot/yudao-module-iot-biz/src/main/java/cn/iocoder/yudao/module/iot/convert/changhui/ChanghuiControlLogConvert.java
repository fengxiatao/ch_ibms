package cn.iocoder.yudao.module.iot.convert.changhui;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control.ChanghuiControlLogRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiControlLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 长辉设备控制日志 Convert
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiControlLogConvert {

    ChanghuiControlLogConvert INSTANCE = Mappers.getMapper(ChanghuiControlLogConvert.class);

    /**
     * DO转响应VO
     */
    ChanghuiControlLogRespVO convert(ChanghuiControlLogDO log);

    /**
     * DO列表转响应VO列表
     */
    List<ChanghuiControlLogRespVO> convertList(List<ChanghuiControlLogDO> list);

    /**
     * 分页结果转换
     */
    PageResult<ChanghuiControlLogRespVO> convertPage(PageResult<ChanghuiControlLogDO> page);

}
