package cn.iocoder.yudao.module.iot.convert.changhui;

import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade.ChanghuiFirmwareRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiFirmwareDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 长辉固件 Convert
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiFirmwareConvert {

    ChanghuiFirmwareConvert INSTANCE = Mappers.getMapper(ChanghuiFirmwareConvert.class);

    /**
     * DO转响应VO
     */
    ChanghuiFirmwareRespVO convert(ChanghuiFirmwareDO firmware);

    /**
     * DO列表转响应VO列表
     */
    List<ChanghuiFirmwareRespVO> convertList(List<ChanghuiFirmwareDO> list);

}
