package cn.iocoder.yudao.module.iot.convert.changhui;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiAlarmDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 长辉设备报警 Convert
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiAlarmConvert {

    ChanghuiAlarmConvert INSTANCE = Mappers.getMapper(ChanghuiAlarmConvert.class);

    /**
     * 保存请求VO转DO
     */
    ChanghuiAlarmDO convert(ChanghuiAlarmSaveReqVO reqVO);

    /**
     * DO转响应VO
     */
    ChanghuiAlarmRespVO convert(ChanghuiAlarmDO alarm);

    /**
     * DO列表转响应VO列表
     */
    List<ChanghuiAlarmRespVO> convertList(List<ChanghuiAlarmDO> list);

    /**
     * 分页结果转换
     */
    PageResult<ChanghuiAlarmRespVO> convertPage(PageResult<ChanghuiAlarmDO> page);

}
