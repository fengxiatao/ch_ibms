package cn.iocoder.yudao.module.iot.convert.changhui;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceRegisterReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiDeviceDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 长辉设备 Convert
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiDeviceConvert {

    ChanghuiDeviceConvert INSTANCE = Mappers.getMapper(ChanghuiDeviceConvert.class);

    /**
     * 注册请求VO转DO
     */
    ChanghuiDeviceDO convert(ChanghuiDeviceRegisterReqVO reqVO);

    /**
     * 更新请求VO转DO
     */
    ChanghuiDeviceDO convert(ChanghuiDeviceUpdateReqVO reqVO);

    /**
     * DO转响应VO
     */
    ChanghuiDeviceRespVO convert(ChanghuiDeviceDO device);

    /**
     * DO列表转响应VO列表
     */
    List<ChanghuiDeviceRespVO> convertList(List<ChanghuiDeviceDO> list);

    /**
     * 分页结果转换
     */
    PageResult<ChanghuiDeviceRespVO> convertPage(PageResult<ChanghuiDeviceDO> page);

}
